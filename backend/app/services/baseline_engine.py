import math
import numpy as np
from typing import Dict, List, Any, Optional, Tuple
from sqlalchemy.orm import Session
from app.models.domain import User, BehavioralTelemetry, UserBaseline
from app.services.feature_directions import FEATURE_CONFIG, MODALITY_MAP

STD_FLOOR = 1e-4

class BaselineEngine:
    @staticmethod
    def get_baseline_status_and_confidence(samples_count: int) -> Tuple[str, float]:
        """
        Determines progressive baseline maturity stage and confidence level.
        - < 3 days (< 5 samples): Insufficient
        - 3–7 days (5–14 samples): Early
        - 7–14 days (15–29 samples): Developing
        - 14+ days (30+ samples): Established
        """
        if samples_count < 5:
            return "insufficient", round(min(40.0, 10.0 + samples_count * 6.0), 1)
        elif samples_count < 15:
            return "early", round(40.0 + (samples_count - 5) * 2.5, 1)
        elif samples_count < 30:
            return "developing", round(65.0 + (samples_count - 15) * 1.5, 1)
        else:
            return "established", round(min(98.0, 87.5 + (samples_count - 30) * 0.3), 1)

    @classmethod
    def recalculate_user_baseline(cls, db: Session, user_id: str) -> List[UserBaseline]:
        """
        Calculates mean, std, median, min, max, variability, and sample count per feature
        across user's individual historical telemetry data.
        """
        telemetry_records = db.query(BehavioralTelemetry).filter(
            BehavioralTelemetry.user_id == user_id
        ).order_by(BehavioralTelemetry.timestamp.desc()).all()

        user_baselines = []
        sample_count_total = len(telemetry_records)

        for key, config in FEATURE_CONFIG.items():
            vals = [float(getattr(r, key)) for r in telemetry_records if getattr(r, key) is not None]
            
            # Clean Outliers: filter extreme invalid values (e.g. negative time or Inf)
            clean_vals = [v for v in vals if not math.isnan(v) and not math.isinf(v) and v >= 0.0]

            if clean_vals:
                mean_val = float(np.mean(clean_vals))
                std_val = float(np.std(clean_vals))
                median_val = float(np.median(clean_vals))
                min_v = float(np.min(clean_vals))
                max_v = float(np.max(clean_vals))

                # Safe standard deviation floor
                if std_val < STD_FLOOR:
                    std_val = max(abs(mean_val) * 0.1, 0.1)

                variability = std_val / (mean_val + STD_FLOOR)

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
                        samples_count=len(clean_vals)
                    )
                    db.add(baseline_record)
                else:
                    baseline_record.mean = mean_val
                    baseline_record.std = std_val
                    baseline_record.min_val = min_v
                    baseline_record.max_val = max_v
                    baseline_record.samples_count = len(clean_vals)

                user_baselines.append(baseline_record)

        # Update User baseline completed status if >= 14 days (approx 28 samples)
        if sample_count_total >= 28:
            user = db.query(User).filter(User.id == user_id).first()
            if user:
                user.baseline_completed = True

        db.commit()
        return user_baselines

    @classmethod
    def compute_feature_z_scores(cls, db: Session, user_id: str, current_telemetry: Dict[str, float]) -> Dict[str, float]:
        """
        Calculates directional Z-scores taking feature direction into account:
        - lower_is_deviation: z = (mean - current) / std
        - higher_is_deviation: z = (current - mean) / std
        - bidirectional: z = abs(current - mean) / std
        Handles zero std, NaNs, missing features gracefully.
        """
        baselines = db.query(UserBaseline).filter(UserBaseline.user_id == user_id).all()
        baseline_map = {b.feature_name: b for b in baselines}
        z_scores = {}

        for key, value in current_telemetry.items():
            if value is None or key not in baseline_map or key not in FEATURE_CONFIG:
                continue

            val_float = float(value)
            if math.isnan(val_float) or math.isinf(val_float):
                continue

            b = baseline_map[key]
            std_val = max(b.std, STD_FLOOR)
            direction = FEATURE_CONFIG[key]["direction"]

            raw_diff = val_float - b.mean
            if direction == "lower_is_deviation":
                # Decreases relative to mean produce positive concerning Z
                z = (b.mean - val_float) / std_val
            elif direction == "higher_is_deviation":
                # Increases relative to mean produce positive concerning Z
                z = raw_diff / std_val
            else:
                # Bidirectional magnitude shift
                z = abs(raw_diff) / std_val

            # Cap Z-score to realistic range [-5.0, 5.0]
            z_scores[key] = round(float(np.clip(z, -5.0, 5.0)), 2)

        return z_scores
