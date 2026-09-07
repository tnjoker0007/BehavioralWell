import numpy as np
from typing import Dict, List, Tuple
from sqlalchemy.orm import Session
from app.models.domain import User, BehavioralTelemetry, UserBaseline
from app.config import settings

FEATURE_KEYS = [
    "typing_speed", "key_press_duration", "pause_duration", "correction_rate",
    "screen_time", "unlock_count", "night_usage", "app_switch_frequency",
    "movement_intensity", "acceleration_variance", "stationary_duration",
    "task_accuracy", "task_completion_time", "task_error_rate",
    "speed_variance", "route_variability"
]

MODALITY_MAP = {
    "typing_speed": "keyboard", "key_press_duration": "keyboard", "pause_duration": "keyboard", "correction_rate": "keyboard",
    "screen_time": "usage", "unlock_count": "usage", "night_usage": "usage", "app_switch_frequency": "usage",
    "movement_intensity": "motion", "acceleration_variance": "motion", "stationary_duration": "motion",
    "task_accuracy": "work", "task_completion_time": "work", "task_error_rate": "work",
    "speed_variance": "mobility", "route_variability": "mobility"
}

class BaselineEngine:
    @staticmethod
    def recalculate_user_baseline(db: Session, user_id: str) -> List[UserBaseline]:
        """Calculates mean, std, min, max per feature across user's history."""
        telemetry_records = db.query(BehavioralTelemetry).filter(
            BehavioralTelemetry.user_id == user_id
        ).order_by(BehavioralTelemetry.timestamp.desc()).all()

        user_baselines = []
        if len(telemetry_records) < settings.MIN_BASELINE_SAMPLES:
            return user_baselines

        for key in FEATURE_KEYS:
            vals = [getattr(r, key) for r in telemetry_records if getattr(r, key) is not None]
            if len(vals) >= settings.MIN_BASELINE_SAMPLES:
                mean_val = float(np.mean(vals))
                std_val = float(np.std(vals))
                # Ensure minimum std to avoid zero-division
                if std_val < 1e-4:
                    std_val = max(abs(mean_val) * 0.1, 1.0)
                
                min_v = float(np.min(vals))
                max_v = float(np.max(vals))

                baseline_record = db.query(UserBaseline).filter(
                    UserBaseline.user_id == user_id,
                    UserBaseline.feature_name == key
                ).first()

                if not baseline_record:
                    baseline_record = UserBaseline(
                        user_id=user_id,
                        feature_name=key,
                        mean=mean_val,
                        std=std_val,
                        min_val=min_v,
                        max_val=max_v,
                        samples_count=len(vals)
                    )
                    db.add(baseline_record)
                else:
                    baseline_record.mean = mean_val
                    baseline_record.std = std_val
                    baseline_record.min_val = min_v
                    baseline_record.max_val = max_v
                    baseline_record.samples_count = len(vals)
                
                user_baselines.append(baseline_record)

        # Mark user baseline as completed if enough samples exist
        if len(telemetry_records) >= settings.DEFAULT_BASELINE_DAYS:
            user = db.query(User).filter(User.id == user_id).first()
            if user:
                user.baseline_completed = True

        db.commit()
        return user_baselines

    @staticmethod
    def compute_feature_z_scores(db: Session, user_id: str, current_telemetry: Dict[str, float]) -> Dict[str, float]:
        """Calculates Z-score (current - mean) / std for provided telemetry features."""
        baselines = db.query(UserBaseline).filter(UserBaseline.user_id == user_id).all()
        baseline_map = {b.feature_name: b for b in baselines}
        z_scores = {}

        for key, value in current_telemetry.items():
            if value is None or key not in baseline_map:
                continue
            b = baseline_map[key]
            z = (value - b.mean) / b.std
            z_scores[key] = float(z)

        return z_scores
