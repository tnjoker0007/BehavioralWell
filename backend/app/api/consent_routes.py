from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from app.database import get_db
from app.schemas.dto import ConsentUpdate, ConsentResponse
from app.services.consent_service import ConsentService
from app.api.auth_routes import get_current_user_obj
from app.models.domain import User
from typing import Optional

router = APIRouter(prefix="/consent", tags=["Consent & Privacy"])

@router.get("", response_model=ConsentResponse)
@router.get("/{user_id}", response_model=ConsentResponse)
def get_consent(user_id: Optional[str] = None, current_user: User = Depends(get_current_user_obj), db: Session = Depends(get_db)):
    target_id = user_id or current_user.id
    if target_id != current_user.id and current_user.role != "admin":
        raise HTTPException(status_code=403, detail="Forbidden: Cannot access consent settings of another user")
    consent = ConsentService.get_consent(db, target_id)
    return ConsentResponse.from_orm(consent)

@router.put("", response_model=ConsentResponse)
@router.put("/{user_id}", response_model=ConsentResponse)
def update_consent(consent_in: ConsentUpdate, user_id: Optional[str] = None, current_user: User = Depends(get_current_user_obj), db: Session = Depends(get_db)):
    target_id = user_id or current_user.id
    if target_id != current_user.id and current_user.role != "admin":
        raise HTTPException(status_code=403, detail="Forbidden: Cannot update consent settings of another user")
    consent = ConsentService.update_consent(db, target_id, consent_in)
    return ConsentResponse.from_orm(consent)
