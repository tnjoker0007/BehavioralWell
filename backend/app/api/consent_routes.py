from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from app.database import get_db
from app.schemas.dto import ConsentUpdate, ConsentResponse
from app.services.consent_service import ConsentService

router = APIRouter(prefix="/consent", tags=["Consent & Privacy"])

@router.get("", response_model=ConsentResponse)
@router.get("/{user_id}", response_model=ConsentResponse)
def get_consent(user_id: str = "usr_demo12345", db: Session = Depends(get_db)):
    consent = ConsentService.get_consent(db, user_id)
    return ConsentResponse.from_orm(consent)

@router.put("", response_model=ConsentResponse)
@router.put("/{user_id}", response_model=ConsentResponse)
def update_consent(consent_in: ConsentUpdate, user_id: str = "usr_demo12345", db: Session = Depends(get_db)):
    consent = ConsentService.update_consent(db, user_id, consent_in)
    return ConsentResponse.from_orm(consent)
