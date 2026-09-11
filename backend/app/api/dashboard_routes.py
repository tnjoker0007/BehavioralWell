from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from app.database import get_db
from app.schemas.dto import DashboardResponse, UserResponse, ConsentResponse, SelfReportResponse
from app.models.domain import User, SelfReport, RiskAssessment
from app.api.consent_routes import get_consent
from app.api.baseline_routes import get_baseline_progress
from app.api.risk_routes import get_current_risk
from app.api.intervention_routes import get_recommendations

from app.api.auth_routes import get_current_user_id

router = APIRouter(prefix="/dashboard", tags=["Unified Mobile/Web Dashboard"])

@router.get("", response_model=DashboardResponse)
def get_unified_dashboard(user_id: str = Depends(get_current_user_id), db: Session = Depends(get_db)):
    user = db.query(User).filter(User.id == user_id).first()
    if not user:
        user = db.query(User).filter(User.email == "demo@behavioralwell.ai").first()

    consent = get_consent(user_id=user.id, db=db)
    baseline_prog = get_baseline_progress(user_id=user.id, db=db)
    current_risk = get_current_risk(user_id=user.id, db=db)
    interventions_resp = get_recommendations(user_id=user.id, db=db)

    latest_self = db.query(SelfReport).filter(
        SelfReport.user_id == user.id
    ).order_by(SelfReport.timestamp.desc()).first()

    self_report_dto = SelfReportResponse.from_orm(latest_self) if latest_self else None

    return DashboardResponse(
        user=UserResponse.from_orm(user),
        consent=ConsentResponse.from_orm(consent),
        baseline_progress=baseline_prog,
        current_risk=current_risk,
        recommended_interventions=interventions_resp.get("recommendations", []),
        last_self_report=self_report_dto
    )
