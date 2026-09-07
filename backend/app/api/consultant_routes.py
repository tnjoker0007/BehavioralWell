from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from typing import List, Dict, Any
from app.database import get_db
from app.models.domain import User, RiskAssessment, SelfReport, InterventionSession

router = APIRouter(prefix="/consultant", tags=["Consultant Dashboard"])

@router.get("/patients")
def get_consultant_patients(db: Session = Depends(get_db)):
    users = db.query(User).all()
    patient_list = []

    for user in users:
        latest_risk = db.query(RiskAssessment).filter(
            RiskAssessment.user_id == user.id
        ).order_by(RiskAssessment.timestamp.desc()).first()

        latest_self = db.query(SelfReport).filter(
            SelfReport.user_id == user.id
        ).order_by(SelfReport.timestamp.desc()).first()

        interventions_count = db.query(InterventionSession).filter(
            InterventionSession.user_id == user.id,
            InterventionSession.completed == True
        ).count()

        anonymous_id = f"PAT-{user.id[-5:].upper()}"
        
        patient_list.append({
            "user_id": user.id,
            "anonymous_id": anonymous_id,
            "occupation_category": user.occupation_category,
            "baseline_completed": user.baseline_completed,
            "risk_score": latest_risk.risk_score if latest_risk else 15.0,
            "stage": latest_risk.stage if latest_risk else 0,
            "stage_label": latest_risk.stage_label if latest_risk else "Stage 0 — Stable",
            "trend": latest_risk.trend if latest_risk else "stable",
            "persistence_days": latest_risk.persistence_days if latest_risk else 1,
            "modality_scores": latest_risk.modality_scores if latest_risk else {},
            "top_contributors": latest_risk.top_contributors if latest_risk else [],
            "last_self_report": {
                "mood": latest_self.mood if latest_self else "Not submitted",
                "stress_level": latest_self.stress_level if latest_self else 1
            },
            "interventions_completed": interventions_count,
            "updated_at": latest_risk.timestamp if latest_risk else user.created_at
        })

    return patient_list
