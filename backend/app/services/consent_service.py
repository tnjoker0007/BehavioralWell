from sqlalchemy.orm import Session
from app.models.domain import Consent
from app.schemas.dto import ConsentUpdate

class ConsentService:
    @staticmethod
    def get_consent(db: Session, user_id: str) -> Consent:
        consent = db.query(Consent).filter(Consent.user_id == user_id).first()
        if not consent:
            consent = Consent(user_id=user_id)
            db.add(consent)
            db.commit()
            db.refresh(consent)
        return consent

    @staticmethod
    def update_consent(db: Session, user_id: str, consent_in: ConsentUpdate) -> Consent:
        consent = ConsentService.get_consent(db, user_id)
        consent.keyboard_enabled = consent_in.keyboard_enabled
        consent.usage_enabled = consent_in.usage_enabled
        consent.motion_enabled = consent_in.motion_enabled
        consent.work_enabled = consent_in.work_enabled
        consent.mobility_enabled = consent_in.mobility_enabled
        db.commit()
        db.refresh(consent)
        return consent
