from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from datetime import datetime
from app.database import get_db
from app.schemas.dto import SelfReportInput, InterventionStartInput, InterventionCompleteInput
from app.models.domain import SelfReport, InterventionSession, RiskAssessment
from app.services.intervention_engine import InterventionEngine
from app.services.escalation_service import EscalationService

router = APIRouter(prefix="/interventions", tags=["Interventions & Check-ins"])

@router.get("/{user_id}/recommendations")
def get_recommendations(user_id: str, db: Session = Depends(get_db)):
    latest_assessment = db.query(RiskAssessment).filter(
        RiskAssessment.user_id == user_id
    ).order_by(RiskAssessment.timestamp.desc()).first()

    stage = latest_assessment.stage if latest_assessment else 0
    contributors = latest_assessment.top_contributors if latest_assessment else []
    
    from app.schemas.dto import ContributorFactor
    contributor_objs = [ContributorFactor(**c) for c in contributors] if contributors else []

    recommendations = InterventionEngine.recommend_interventions(contributor_objs, stage)
    escalation = EscalationService.get_escalation_resources(stage)

    return {
        "stage": stage,
        "recommendations": recommendations,
        "escalation": escalation
    }

@router.post("/{user_id}/self-check")
def submit_self_check(user_id: str, report_in: SelfReportInput, db: Session = Depends(get_db)):
    report = SelfReport(
        user_id=user_id,
        mood=report_in.mood,
        stress_level=report_in.stress_level,
        note=report_in.note,
        timestamp=datetime.utcnow()
    )
    db.add(report)
    db.commit()
    return {"message": "Self-check recorded successfully", "id": report.id}

@router.post("/{user_id}/start")
def start_intervention(user_id: str, input_in: InterventionStartInput, db: Session = Depends(get_db)):
    session = InterventionSession(
        user_id=user_id,
        activity_type=input_in.activity_type,
        started_at=datetime.utcnow()
    )
    db.add(session)
    db.commit()
    return {"session_id": session.id, "status": "started"}

@router.post("/sessions/{session_id}/complete")
def complete_intervention(session_id: int, input_in: InterventionCompleteInput, db: Session = Depends(get_db)):
    session = db.query(InterventionSession).filter(InterventionSession.id == session_id).first()
    if not session:
        raise HTTPException(status_code=404, detail="Session not found")

    session.completed_at = datetime.utcnow()
    session.completed = True
    session.feedback_score = input_in.feedback_score
    session.result_metrics = input_in.result_metrics
    db.commit()
    return {"message": "Intervention session marked complete", "session_id": session.id}
