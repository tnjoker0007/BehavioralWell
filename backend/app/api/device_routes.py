from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from datetime import datetime
from typing import List, Dict, Any, Optional

from app.database import get_db
from app.models.domain import DeviceRegistration, User
from app.schemas.dto import DeviceRegistrationRequest, DeviceRegistrationResponse
from app.api.auth_routes import get_current_user_id

router = APIRouter(prefix="/device", tags=["Device Management"])

@router.post("/register", response_model=DeviceRegistrationResponse)
def register_device(
    request: DeviceRegistrationRequest,
    user_id: Optional[str] = Depends(get_current_user_id),
    db: Session = Depends(get_db)
):
    """
    Automatically registers or updates a device's identity and metadata.
    Associates the device with the authenticated user if available.
    """
    device = db.query(DeviceRegistration).filter(DeviceRegistration.device_id == request.deviceId).first()
    
    if not device:
        device = DeviceRegistration(
            device_id=request.deviceId,
            user_id=user_id,
            device_model=request.deviceModel,
            android_version=request.androidVersion,
            app_version=request.appVersion,
            registered_at=datetime.utcnow(),
            last_seen_at=datetime.utcnow()
        )
        db.add(device)
    else:
        if user_id:
            device.user_id = user_id
        if request.deviceModel:
            device.device_model = request.deviceModel
        if request.androidVersion:
            device.android_version = request.androidVersion
        if request.appVersion:
            device.app_version = request.appVersion
        device.last_seen_at = datetime.utcnow()

    db.commit()
    db.refresh(device)
    return device

@router.get("/list", response_model=List[DeviceRegistrationResponse])
def list_user_devices(
    user_id: str = Depends(get_current_user_id),
    db: Session = Depends(get_db)
):
    """
    Returns all registered devices associated with the authenticated user.
    """
    devices = db.query(DeviceRegistration).filter(DeviceRegistration.user_id == user_id).all()
    return devices
