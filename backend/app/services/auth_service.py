import uuid
import hashlib
import jwt
from datetime import datetime, timedelta
from typing import Optional, Dict, Any
from sqlalchemy.orm import Session
from app.models.domain import User, Consent
from app.schemas.dto import UserCreate, UserLogin, TokenRefreshRequest
from app.config import settings

import secrets

def hash_password(password: str, salt: Optional[str] = None) -> str:
    if not salt:
        salt = secrets.token_hex(16)
    key = hashlib.pbkdf2_hmac('sha256', password.encode('utf-8'), salt.encode('utf-8'), 100000)
    return f"pbkdf2_sha256$100000${salt}${key.hex()}"

def verify_password(password: str, hashed_password: str) -> bool:
    if not hashed_password:
        return False
    if hashed_password.startswith("pbkdf2_sha256$"):
        try:
            parts = hashed_password.split("$")
            if len(parts) == 4:
                salt = parts[2]
                return hash_password(password, salt=salt) == hashed_password
        except Exception:
            return False
    # Legacy SHA-256 fallback check for existing seeded accounts
    legacy_hash = hashlib.sha256(password.encode('utf-8')).hexdigest()
    return hashed_password == legacy_hash

class AuthService:
    @staticmethod
    def create_access_token(data: dict, expires_delta: Optional[timedelta] = None) -> str:
        to_encode = data.copy()
        expire = datetime.utcnow() + (expires_delta or timedelta(minutes=settings.ACCESS_TOKEN_EXPIRE_MINUTES))
        to_encode.update({"exp": expire, "type": "access"})
        return jwt.encode(to_encode, settings.SECRET_KEY, algorithm=settings.ALGORITHM)

    @staticmethod
    def create_refresh_token(data: dict) -> str:
        to_encode = data.copy()
        expire = datetime.utcnow() + timedelta(days=30)
        to_encode.update({"exp": expire, "type": "refresh"})
        return jwt.encode(to_encode, settings.SECRET_KEY, algorithm=settings.ALGORITHM)

    @staticmethod
    def decode_token(token: str) -> Optional[Dict[str, Any]]:
        try:
            payload = jwt.decode(token, settings.SECRET_KEY, algorithms=[settings.ALGORITHM])
            return payload
        except Exception:
            return None

    @staticmethod
    def create_user(db: Session, user_in: UserCreate) -> User:
        user_id = f"usr_{uuid.uuid4().hex[:10]}"
        user = User(
            id=user_id,
            name=user_in.name,
            email=user_in.email,
            hashed_password=hash_password(user_in.password),
            role=user_in.role or "user",
            age_group=user_in.age_group or "25-34",
            occupation_category=user_in.occupation_category or "Technology"
        )
        db.add(user)
        db.commit()

        # Initialize default consent
        consent = Consent(
            user_id=user_id,
            keyboard_enabled=True,
            usage_enabled=True,
            motion_enabled=True,
            work_enabled=True,
            mobility_enabled=True
        )
        db.add(consent)
        db.commit()
        db.refresh(user)
        return user

    @staticmethod
    def authenticate_user(db: Session, user_in: UserLogin) -> User:
        user = db.query(User).filter(User.email == user_in.email).first()
        if not user:
            return None
        if not verify_password(user_in.password, user.hashed_password):
            return None
        # Auto-upgrade legacy SHA-256 hash to salted PBKDF2
        if not user.hashed_password.startswith("pbkdf2_sha256$"):
            user.hashed_password = hash_password(user_in.password)
            db.commit()
        return user

    @staticmethod
    def delete_user_data(db: Session, user_id: str) -> bool:
        user = db.query(User).filter(User.id == user_id).first()
        if not user:
            return False
        
        from app.models.domain import BehavioralTelemetry, UserBaseline, RiskAssessment, InterventionSession, SelfReport
        db.query(BehavioralTelemetry).filter(BehavioralTelemetry.user_id == user_id).delete()
        db.query(UserBaseline).filter(UserBaseline.user_id == user_id).delete()
        db.query(RiskAssessment).filter(RiskAssessment.user_id == user_id).delete()
        db.query(InterventionSession).filter(InterventionSession.user_id == user_id).delete()
        db.query(SelfReport).filter(SelfReport.user_id == user_id).delete()
        user.baseline_completed = False
        db.commit()
        return True
