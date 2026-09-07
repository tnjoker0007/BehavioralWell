from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from datetime import datetime
from typing import List
from app.database import get_db
from app.schemas.dto import TelemetryInput, BatchTelemetryInput, RiskAssessmentResponse, TelemetryStatusResponse
from app.models.domain import BehavioralTelemetry, RiskAssessment, User
from app.services.consent_service import ConsentService
from app.services.feature_engine import FeatureEngine
from app.services.baseline_engine import BaselineEngine
from app.services.temporal_engine import TemporalEngine
from app.services.ml_risk_engine import MLRiskEngine
from app.services.explainability import ExplainabilityEngine

router = APIRouter(prefix="/telemetry", tags=["Telemetry & Processing"])

@router.post("", response_model=RiskAssessmentResponse)
@router.post("/ingest", response_model=RiskAssessmentResponse)
def ingest_single_telemetry(telemetry_in: TelemetryInput, user_id: str = "usr_demo12345", db: Session = Depends(get_db)):
    return process_telemetry_payload(user_id, telemetry_in, db)

@router.post("/batch")
def ingest_batch_telemetry(batch_in: BatchTelemetryInput, user_id: str = "usr_demo12345", db: Session = Depends(get_db)):
    """
    Offline-first batch ingestion endpoint with idempotency deduplication.
    Allows Android client to queue local telemetry and upload upon reconnecting.
    """
    processed_count = 0
    skipped_count = 0
    latest_assessment = None

    for item in batch_in.batch:
        # Check idempotency deduplication
        if item.idempotency_key:
            recent_telemetries = db.query(BehavioralTelemetry).filter(
                BehavioralTelemetry.user_id == user_id
            ).order_by(BehavioralTelemetry.id.desc()).limit(200).all()
            if any(r.raw_features_json and isinstance(r.raw_features_json, dict) and r.raw_features_json.get("idempotency_key") == item.idempotency_key for r in recent_telemetries):
                skipped_count += 1
                continue

        latest_assessment = process_telemetry_payload(user_id, item, db)
        processed_count += 1

    return {
        "status": "success",
        "processed_count": processed_count,
        "skipped_duplicates": skipped_count,
        "latest_risk": latest_assessment
    }

@router.get("/status", response_model=TelemetryStatusResponse)
def get_telemetry_status(user_id: str = "usr_demo12345", db: Session = Depends(get_db)):
    records = db.query(BehavioralTelemetry).filter(BehavioralTelemetry.user_id == user_id).all()
    last_record = db.query(BehavioralTelemetry).filter(
        BehavioralTelemetry.user_id == user_id
    ).order_by(BehavioralTelemetry.timestamp.desc()).first()

    collected_modalities = set()
    for r in records:
        if r.typing_speed is not None: collected_modalities.add("keyboard")
        if r.screen_time is not None: collected_modalities.add("usage")
        if r.movement_intensity is not None: collected_modalities.add("motion")
        if r.task_accuracy is not None: collected_modalities.add("work")
        if r.speed_variance is not None: collected_modalities.add("mobility")

    return TelemetryStatusResponse(
        total_records=len(records),
        last_telemetry_at=last_record.timestamp if last_record else None,
        modalities_collected=list(collected_modalities)
    )

def process_telemetry_payload(user_id: str, telemetry_in: TelemetryInput, db: Session) -> RiskAssessmentResponse:
    consent = ConsentService.get_consent(db, user_id)
    
    # 1. Filter raw features with privacy consent rules
    sanitized_features = FeatureEngine.sanitize_and_filter_telemetry(telemetry_in, consent)
    raw_meta = {}
    if telemetry_in.idempotency_key:
        raw_meta["idempotency_key"] = telemetry_in.idempotency_key

    # 2. Store sanitized telemetry record
    telemetry_record = BehavioralTelemetry(
        user_id=user_id,
        timestamp=telemetry_in.timestamp or datetime.utcnow(),
        raw_features_json=raw_meta,
        **sanitized_features
    )
    db.add(telemetry_record)
    db.commit()

    # 3. Update personal baseline
    BaselineEngine.recalculate_user_baseline(db, user_id)

    # 4. Compute Z-scores relative to personal baseline
    z_scores = BaselineEngine.compute_feature_z_scores(db, user_id, sanitized_features)

    # 5. Evaluate temporal persistence (3-7 day trend)
    persistence_days, trend = TemporalEngine.evaluate_persistence(db, user_id, z_scores)

    # 6. Evaluate Risk Score & Stage using Hybrid ML + Fallback Engine
    consent_flags = {
        "keyboard_enabled": consent.keyboard_enabled,
        "usage_enabled": consent.usage_enabled,
        "motion_enabled": consent.motion_enabled,
        "work_enabled": consent.work_enabled,
        "mobility_enabled": consent.mobility_enabled
    }
    risk_score, stage, stage_label, confidence, modality_scores = MLRiskEngine.evaluate_risk(
        z_scores, persistence_days, consent_flags
    )

    # 7. Generate explainability attribution
    top_contributors = ExplainabilityEngine.generate_explanations(z_scores)
    contributors_dict = [c.dict() for c in top_contributors]

    # 8. Save Risk Assessment Record
    assessment = RiskAssessment(
        user_id=user_id,
        timestamp=datetime.utcnow(),
        risk_score=risk_score,
        stage=stage,
        stage_label=stage_label,
        confidence=confidence,
        trend=trend,
        persistence_days=persistence_days,
        top_contributors=contributors_dict,
        modality_scores=modality_scores
    )
    db.add(assessment)
    db.commit()

    return RiskAssessmentResponse(
        risk_score=risk_score,
        stage=stage,
        stage_label=stage_label,
        confidence=confidence,
        trend=trend,
        persistence_days=persistence_days,
        top_contributors=top_contributors,
        modality_scores=modality_scores,
        timestamp=assessment.timestamp
    )
