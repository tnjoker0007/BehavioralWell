from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from app.database import get_db
from app.schemas.dto import SimulationPresetRequest, RiskAssessmentResponse
from app.services.simulation_service import SimulationService
from app.api.telemetry_routes import ingest_single_telemetry
from app.api.auth_routes import get_current_user_obj
from app.models.domain import User

router = APIRouter(prefix="/simulator", tags=["Live Simulator"])

@router.post("/inject/{user_id}", response_model=RiskAssessmentResponse)
def inject_preset_telemetry(
    user_id: str,
    preset_req: SimulationPresetRequest,
    current_user: User = Depends(get_current_user_obj),
    db: Session = Depends(get_db)
):
    if user_id != current_user.id and current_user.role not in ["admin", "consultant"]:
        raise HTTPException(status_code=403, detail="Forbidden: You are not authorized to inject telemetry into another user's profile")
    telemetry_input = SimulationService.get_preset_telemetry(preset_req.preset_name)
    return ingest_single_telemetry(telemetry_in=telemetry_input, user_id=user_id, db=db)
