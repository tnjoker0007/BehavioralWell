from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from typing import List, Dict, Any
from app.database import get_db
from app.models.domain import User, RiskAssessment, SelfReport, InterventionSession, BehavioralTelemetry, UserBaseline
from app.services.llm_interpreter import LLMInterpreter

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

        telemetry_count = db.query(BehavioralTelemetry).filter(
            BehavioralTelemetry.user_id == user.id
        ).count()

        anonymous_id = f"PAT-{user.id[-5:].upper()}"
        
        patient_list.append({
            "user_id": user.id,
            "name": user.name,
            "email": user.email,
            "role": user.role or "user",
            "anonymous_id": anonymous_id,
            "age_group": user.age_group,
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
                "stress_level": latest_self.stress_level if latest_self else 1,
                "note": latest_self.note if latest_self else ""
            },
            "telemetries_count": telemetry_count,
            "interventions_completed": interventions_count,
            "updated_at": latest_risk.timestamp if latest_risk else user.created_at
        })

    return patient_list

@router.get("/user-activity/{user_id}")
def get_user_activity_details(user_id: str, db: Session = Depends(get_db)):
    user = db.query(User).filter(User.id == user_id).first()
    if not user:
        raise HTTPException(status_code=404, detail="User not found")

    telemetries = db.query(BehavioralTelemetry).filter(
        BehavioralTelemetry.user_id == user_id
    ).order_by(BehavioralTelemetry.timestamp.desc()).limit(15).all()

    risks = db.query(RiskAssessment).filter(
        RiskAssessment.user_id == user_id
    ).order_by(RiskAssessment.timestamp.desc()).limit(10).all()

    self_reports = db.query(SelfReport).filter(
        SelfReport.user_id == user_id
    ).order_by(SelfReport.timestamp.desc()).limit(10).all()

    interventions = db.query(InterventionSession).filter(
        InterventionSession.user_id == user_id
    ).order_by(InterventionSession.started_at.desc()).limit(10).all()

    latest_risk = risks[0] if risks else None
    
    # Generate supportive LLM risk interpretation summary if risk exists
    llm_summary = None
    if latest_risk:
        try:
            risk_dto_dict = {
                "risk_score": latest_risk.risk_score,
                "stage": latest_risk.stage,
                "stage_label": latest_risk.stage_label,
                "confidence": latest_risk.confidence,
                "trend": latest_risk.trend,
                "persistence_days": latest_risk.persistence_days,
                "top_contributors": latest_risk.top_contributors,
                "modality_scores": latest_risk.modality_scores,
                "timestamp": latest_risk.timestamp.isoformat()
            }
            llm_summary = LLMInterpreter.interpret_risk(risk_dto_dict)
        except Exception as e:
            llm_summary = "Risk summary available. All telemetry parameters within evaluated ranges."

    return {
        "user": {
            "id": user.id,
            "name": user.name,
            "email": user.email,
            "role": user.role or "user",
            "age_group": user.age_group,
            "occupation_category": user.occupation_category,
            "created_at": user.created_at,
            "baseline_completed": user.baseline_completed
        },
        "latest_risk": latest_risk,
        "llm_interpretation": llm_summary,
        "recent_telemetries": telemetries,
        "self_reports": self_reports,
        "interventions": interventions
    }

