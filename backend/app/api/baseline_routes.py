from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from typing import Optional
from app.database import get_db
from app.schemas.dto import BaselineResponse, BaselineProgressResponse, BaselineItem
from app.models.domain import User, UserBaseline, BehavioralTelemetry
from app.api.auth_routes import get_current_user_obj

router = APIRouter(prefix="/baseline", tags=["Personal Baseline"])

@router.get("", response_model=BaselineResponse)
def get_baseline(user_id: Optional[str] = None, current_user: User = Depends(get_current_user_obj), db: Session = Depends(get_db)):
    target_id = user_id or current_user.id
    if target_id != current_user.id and current_user.role != "admin":
        raise HTTPException(status_code=403, detail="Forbidden: Cannot access baseline of another user")
    user = db.query(User).filter(User.id == target_id).first()
    baselines = db.query(UserBaseline).filter(UserBaseline.user_id == target_id).all()
    telemetry_count = db.query(BehavioralTelemetry).filter(BehavioralTelemetry.user_id == target_id).count()

    items = [
        BaselineItem(
            feature_name=b.feature_name,
            mean=b.mean,
            std=b.std,
            min_val=b.min_val,
            max_val=b.max_val,
            samples_count=b.samples_count
        ) for b in baselines
    ]

    completion = min(100.0, round((telemetry_count / 14.0) * 100.0, 1))

    return BaselineResponse(
        user_id=target_id,
        baseline_completed=user.baseline_completed if user else False,
        completion_percent=completion,
        features=items
    )

@router.get("/progress", response_model=BaselineProgressResponse)
def get_baseline_progress(user_id: Optional[str] = None, current_user: User = Depends(get_current_user_obj), db: Session = Depends(get_db)):
    target_id = user_id or current_user.id
    if target_id != current_user.id and current_user.role != "admin":
        raise HTTPException(status_code=403, detail="Forbidden: Cannot access baseline progress of another user")
    user = db.query(User).filter(User.id == target_id).first()
    telemetry_count = db.query(BehavioralTelemetry).filter(BehavioralTelemetry.user_id == target_id).count()
    
    completion = min(100.0, round((telemetry_count / 14.0) * 100.0, 1))
    status = "established" if (user and user.baseline_completed) or completion >= 100.0 else "learning"

    return BaselineProgressResponse(
        baseline_status=status,
        completion_percent=completion,
        days_collected=min(telemetry_count, 14),
        days_required=14,
        modalities_status={
            "keyboard": "stable" if completion > 20 else "learning",
            "phone_usage": "stable" if completion > 20 else "learning",
            "movement": "stable" if completion > 20 else "learning",
            "work": "stable" if completion > 20 else "learning",
            "mobility": "stable" if completion > 20 else "learning"
        }
    )
