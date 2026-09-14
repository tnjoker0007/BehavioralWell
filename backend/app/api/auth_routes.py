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

def get_current_user_obj(authorization: str = Header(None), db: Session = Depends(get_db)) -> User:
    if not authorization or not authorization.startswith("Bearer "):
        raise HTTPException(status_code=401, detail="Authentication credentials were not provided")
    
    token = authorization.split(" ")[1] if " " in authorization else ""
    payload = AuthService.decode_token(token)
    if not payload or payload.get("type") != "access" or not payload.get("sub"):
        raise HTTPException(status_code=401, detail="Invalid or expired authentication token")

    user = db.query(User).filter(User.id == payload.get("sub")).first()
    if not user:
        raise HTTPException(status_code=401, detail="User associated with token not found")

    return user

def get_current_user_id(user: User = Depends(get_current_user_obj)) -> str:
    return user.id

@router.get("/me", response_model=UserResponse)
def get_current_user(current_user: User = Depends(get_current_user_obj)):
    return UserResponse.from_orm(current_user)

@router.post("/logout")
def logout():
    return {"message": "Successfully logged out. Client should discard tokens."}

@router.delete("/users/{user_id}/data")
def delete_data(user_id: str, current_user: User = Depends(get_current_user_obj), db: Session = Depends(get_db)):
    if current_user.id != user_id and current_user.role != "admin":
        raise HTTPException(status_code=403, detail="Forbidden: You are not authorized to delete another user's data")
        
    success = AuthService.delete_user_data(db, user_id)
    if not success:
        raise HTTPException(status_code=404, detail="User not found")
    return {"message": "All behavioral telemetry and baselines deleted successfully."}
