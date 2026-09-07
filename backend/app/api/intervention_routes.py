from fastapi import APIRouter, Depends, HTTPException, Header
from sqlalchemy.orm import Session
from datetime import datetime
from typing import Optional, List, Dict, Any

from app.database import get_db
from app.schemas.dto import SelfReportInput, SelfReportResponse, InterventionStartInput, InterventionCompleteInput
from app.models.domain import SelfReport, InterventionSession, RiskAssessment, User
from app.services.intervention_engine import InterventionEngine
from app.services.escalation_service import EscalationService
from app.services.auth_service import AuthService

router = APIRouter(prefix="/interventions", tags=["Interventions"])
self_check_router = APIRouter(prefix="/self-check", tags=["Self Check"])

def get_current_user_id(authorization: str = Header(None), db: Session = Depends(get_db)) -> str:
    if authorization and authorization.startswith("Bearer "):
        token = authorization.split(" ")[1]
        payload = AuthService.decode_token(token)
        if payload and payload.get("type") == "access" and payload.get("sub"):
            user = db.query(User).filter(User.id == payload.get("sub")).first()
            if user:
                return user.id
    
    # Fallback to demo user if unauthenticated or invalid token
    demo_user = db.query(User).filter(User.email == "demo@behavioralwell.ai").first()
    if demo_user:
        return demo_user.id
    return "usr_demo12345"

# --- Recommendations ---

@router.get("/recommendations")
@router.get("/{user_id}/recommendations")
def get_recommendations(
    user_id: Optional[str] = None,
    current_user_id: str = Depends(get_current_user_id),
    db: Session = Depends(get_db)
):
    target_user_id = user_id if user_id else current_user_id
    latest_assessment = db.query(RiskAssessment).filter(
        RiskAssessment.user_id == target_user_id
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

# --- Interventions Start & Complete ---

@router.post("/start")
@router.post("/{user_id}/start")
def start_intervention(
    input_in: InterventionStartInput,
    user_id: Optional[str] = None,
    current_user_id: str = Depends(get_current_user_id),
    db: Session = Depends(get_db)
):
    target_user_id = user_id if user_id else current_user_id
    session = InterventionSession(
        user_id=target_user_id,
        activity_type=input_in.activity_type,
        started_at=datetime.utcnow()
    )
    db.add(session)
    db.commit()
    db.refresh(session)
    return {"session_id": session.id, "status": "started", "user_id": target_user_id}

@router.post("/complete")
@router.post("/sessions/{session_id}/complete")
def complete_intervention(
    input_in: InterventionCompleteInput,
    session_id: Optional[int] = None,
    current_user_id: str = Depends(get_current_user_id),
    db: Session = Depends(get_db)
):
    target_session_id = input_in.session_id if input_in and input_in.session_id is not None else session_id
    
    session = None
    if target_session_id is not None:
        session = db.query(InterventionSession).filter(InterventionSession.id == target_session_id).first()
    
    if not session:
        session = db.query(InterventionSession).filter(
            InterventionSession.user_id == current_user_id,
            InterventionSession.completed == False
        ).order_by(InterventionSession.started_at.desc()).first()

    if not session:
        raise HTTPException(status_code=404, detail="Intervention session not found")

    session.completed_at = datetime.utcnow()
    session.completed = True
    session.feedback_score = input_in.feedback_score
    session.result_metrics = input_in.result_metrics
    db.commit()
    return {"message": "Intervention session marked complete", "session_id": session.id}

# --- Self Check Routes ---

@self_check_router.post("")
@self_check_router.post("/")
def submit_self_check(
    report_in: SelfReportInput,
    current_user_id: str = Depends(get_current_user_id),
    db: Session = Depends(get_db)
):
    report = SelfReport(
        user_id=current_user_id,
        mood=report_in.mood,
        stress_level=report_in.stress_level,
        note=report_in.note,
        timestamp=datetime.utcnow()
    )
    db.add(report)
    db.commit()
    db.refresh(report)
    return {"message": "Self-check recorded successfully", "id": report.id, "user_id": current_user_id}

@self_check_router.get("/history", response_model=List[SelfReportResponse])
def get_self_check_history(
    current_user_id: str = Depends(get_current_user_id),
    db: Session = Depends(get_db)
):
    reports = db.query(SelfReport).filter(
        SelfReport.user_id == current_user_id
    ).order_by(SelfReport.timestamp.desc()).all()
    
    return [SelfReportResponse.from_orm(r) for r in reports]

@router.post("/{user_id}/self-check")
def submit_self_check_legacy(
    user_id: str,
    report_in: SelfReportInput,
    db: Session = Depends(get_db)
):
    report = SelfReport(
        user_id=user_id,
        mood=report_in.mood,
        stress_level=report_in.stress_level,
        note=report_in.note,
        timestamp=datetime.utcnow()
    )
    db.add(report)
    db.commit()
    db.refresh(report)
    return {"message": "Self-check recorded successfully", "id": report.id}
