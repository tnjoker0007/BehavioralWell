import uuid
import hashlib
import jwt
from datetime import datetime, timedelta
from typing import Optional, Dict, Any
from sqlalchemy.orm import Session
from app.models.domain import User, Consent
from app.schemas.dto import UserCreate, UserLogin, TokenRefreshRequest
from app.config import settings

def hash_password(password: str) -> str:
    return hashlib.sha256(password.encode()).hexdigest()

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
        if not user or user.hashed_password != hash_password(user_in.password):
            return None
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
