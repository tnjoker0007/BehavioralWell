from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from app.database import get_db
from app.schemas.dto import SimulationPresetRequest, RiskAssessmentResponse
from app.services.simulation_service import SimulationService
from app.api.telemetry_routes import ingest_single_telemetry

router = APIRouter(prefix="/simulator", tags=["Live Simulator"])

@router.post("/inject/{user_id}", response_model=RiskAssessmentResponse)
def inject_preset_telemetry(
    user_id: str,
    preset_req: SimulationPresetRequest,
    db: Session = Depends(get_db)
):
    telemetry_input = SimulationService.get_preset_telemetry(preset_req.preset_name)
    return ingest_single_telemetry(telemetry_in=telemetry_input, user_id=user_id, db=db)
