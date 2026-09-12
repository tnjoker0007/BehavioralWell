from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from datetime import datetime
from typing import List
from app.database import get_db
from app.schemas.dto import RiskAssessmentResponse, RiskHistoryResponse
from app.models.domain import RiskAssessment

from app.api.auth_routes import get_current_user_id
from app.services.llm_interpreter import LLMInterpreter

router = APIRouter(prefix="/risk", tags=["Risk Staging & Engine"])

@router.get("/current", response_model=RiskAssessmentResponse)
@router.get("/{user_id}/latest", response_model=RiskAssessmentResponse)
def get_current_risk(user_id: str = Depends(get_current_user_id), db: Session = Depends(get_db)):
    assessment = db.query(RiskAssessment).filter(
        RiskAssessment.user_id == user_id
    ).order_by(RiskAssessment.timestamp.desc()).first()

    if not assessment:
        return RiskAssessmentResponse(
            risk_score=15.0,
            stage=0,
            stage_label="Stage 0 — Stable",
            confidence=0.95,
            trend="stable",
            persistence_days=1,
            top_contributors=[],
            modality_scores={"keyboard": 10.0, "usage": 15.0, "motion": 10.0, "work": 12.0, "mobility": 5.0},
            timestamp=datetime.utcnow()
        )

    return RiskAssessmentResponse(
        risk_score=assessment.risk_score,
        stage=assessment.stage,
        stage_label=assessment.stage_label,
        confidence=assessment.confidence,
        trend=assessment.trend,
        persistence_days=assessment.persistence_days,
        top_contributors=assessment.top_contributors,
        modality_scores=assessment.modality_scores,
        timestamp=assessment.timestamp
    )

@router.get("/history", response_model=RiskHistoryResponse)
def get_risk_history(user_id: str = Depends(get_current_user_id), limit: int = 30, db: Session = Depends(get_db)):

    records = db.query(RiskAssessment).filter(
        RiskAssessment.user_id == user_id
    ).order_by(RiskAssessment.timestamp.desc()).limit(limit).all()

    history = [
        RiskAssessmentResponse(
            risk_score=r.risk_score,
            stage=r.stage,
            stage_label=r.stage_label,
            confidence=r.confidence,
            trend=r.trend,
            persistence_days=r.persistence_days,
            top_contributors=r.top_contributors,
            modality_scores=r.modality_scores,
            timestamp=r.timestamp
        ) for r in records
    ]

    return RiskHistoryResponse(history=history)

@router.get("/interpret")
def get_risk_interpretation(user_id: str = Depends(get_current_user_id), db: Session = Depends(get_db)):
    """
    Returns a short, supportive, non-diagnostic natural-language explanation
    of the user's latest risk assessment via OpenRouter. Stage 5 (crisis) is
    excluded — that uses static resource copy instead of LLM interpretation.
    """
    assessment = get_current_risk(user_id=user_id, db=db)

    if assessment.stage >= 5:
        raise HTTPException(
            status_code=400,
            detail="Stage 5 uses static crisis-resource routing, not LLM interpretation."
        )

    text = LLMInterpreter.interpret_risk(assessment.dict())
    return {
        "stage": assessment.stage,
        "stage_label": assessment.stage_label,
        "interpretation": text
    }

@router.get("/explanation")
def get_risk_explanation(user_id: str = "usr_demo12345", db: Session = Depends(get_db)):
    assessment = get_current_risk(user_id=user_id, db=db)
    return {
        "score": assessment.risk_score,
        "stage": assessment.stage,
        "stage_label": assessment.stage_label,
        "factors": assessment.top_contributors
    }

