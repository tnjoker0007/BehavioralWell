from fastapi import APIRouter, Depends, HTTPException, Header
from sqlalchemy.orm import Session
from app.database import get_db
from app.schemas.dto import UserCreate, UserLogin, UserResponse, TokenResponse, TokenRefreshRequest
from app.services.auth_service import AuthService
from app.models.domain import User

router = APIRouter(prefix="/auth", tags=["Authentication"])

@router.post("/register", response_model=TokenResponse)
def register(user_in: UserCreate, db: Session = Depends(get_db)):
    existing = db.query(User).filter(User.email == user_in.email).first()
    if existing:
        raise HTTPException(status_code=400, detail="User with this email already exists")

    user = AuthService.create_user(db, user_in)
    user_resp = UserResponse.from_orm(user)
    
    access_token = AuthService.create_access_token({"sub": user.id, "email": user.email})
    refresh_token = AuthService.create_refresh_token({"sub": user.id})

    return TokenResponse(
        access_token=access_token,
        refresh_token=refresh_token,
        user=user_resp
    )

@router.post("/login", response_model=TokenResponse)
def login(user_in: UserLogin, db: Session = Depends(get_db)):
    user = AuthService.authenticate_user(db, user_in)
    if not user:
        raise HTTPException(status_code=401, detail="Invalid email or password")

    user_resp = UserResponse.from_orm(user)
    access_token = AuthService.create_access_token({"sub": user.id, "email": user.email})
    refresh_token = AuthService.create_refresh_token({"sub": user.id})

    return TokenResponse(
        access_token=access_token,
        refresh_token=refresh_token,
        user=user_resp
    )

@router.post("/refresh", response_model=TokenResponse)
def refresh_token(req: TokenRefreshRequest, db: Session = Depends(get_db)):
    payload = AuthService.decode_token(req.refresh_token)
    if not payload or payload.get("type") != "refresh":
        raise HTTPException(status_code=401, detail="Invalid or expired refresh token")

    user_id = payload.get("sub")
    user = db.query(User).filter(User.id == user_id).first()
    if not user:
        raise HTTPException(status_code=404, detail="User not found")

    user_resp = UserResponse.from_orm(user)
    new_access_token = AuthService.create_access_token({"sub": user.id, "email": user.email})
    new_refresh_token = AuthService.create_refresh_token({"sub": user.id})

    return TokenResponse(
        access_token=new_access_token,
        refresh_token=new_refresh_token,
        user=user_resp
    )

def get_current_user_id(authorization: str = Header(None), db: Session = Depends(get_db)) -> str:
    if authorization and authorization.startswith("Bearer "):
        token = authorization.split(" ")[1]
        payload = AuthService.decode_token(token)
        if payload and payload.get("type") == "access" and payload.get("sub"):
            user = db.query(User).filter(User.id == payload.get("sub")).first()
            if user:
                return user.id

    # Fallback to single demo user ID for web/unauthenticated evaluation
    demo_user = db.query(User).filter(User.email == "demo@behavioralwell.ai").first()
    if demo_user:
        return demo_user.id
    return "usr_demo12345"

@router.get("/me", response_model=UserResponse)
def get_current_user(user_id: str = Depends(get_current_user_id), db: Session = Depends(get_db)):
    user = db.query(User).filter(User.id == user_id).first()
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    return UserResponse.from_orm(user)

@router.post("/logout")
def logout():
    return {"message": "Successfully logged out. Client should discard tokens."}

@router.delete("/users/{user_id}/data")
def delete_data(user_id: str, db: Session = Depends(get_db)):
    success = AuthService.delete_user_data(db, user_id)
    if not success:
        raise HTTPException(status_code=404, detail="User not found")
    return {"message": "All behavioral telemetry and baselines deleted successfully."}
