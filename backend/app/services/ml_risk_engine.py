import os
import joblib
import numpy as np
from typing import Dict, Any, Tuple
from app.config import settings
from app.services.baseline_engine import MODALITY_MAP

STAGE_LABELS = {
    0: "Stage 0 — Stable",
    1: "Stage 1 — Early Deviation",
    2: "Stage 2 — Persistent Deviation",
    3: "Stage 3 — Elevated Risk",
    4: "Stage 4 — High Concern"
}

class MLRiskEngine:
    _ml_model = None

    @classmethod
    def load_model(cls):
        model_path = os.path.join(settings.MODEL_DIR, "behavioral_risk_rf.joblib")
        if os.path.exists(model_path):
            try:
                cls._ml_model = joblib.load(model_path)
            except Exception:
                cls._ml_model = None

    @classmethod
    def evaluate_risk(
        cls,
        z_scores: Dict[str, float],
        persistence_days: int,
        consent_flags: Dict[str, bool]
    ) -> Tuple[float, int, str, float, Dict[str, float]]:
        """
        Evaluates risk score (0-100), stage (0-4), stage label, confidence, and modality scores.
        """
        if not z_scores:
            return 0.0, 0, STAGE_LABELS[0], 0.95, {m: 0.0 for m in ["keyboard", "usage", "motion", "work", "mobility"]}

        # Group Z-scores by modality
        modality_zs = {"keyboard": [], "usage": [], "motion": [], "work": [], "mobility": []}
        for feat, z in z_scores.items():
            modality = MODALITY_MAP.get(feat, "keyboard")
            modality_zs[modality].append(abs(z))

        # Modality deviation scores (0 - 100 scale)
        modality_scores = {}
        for m, z_list in modality_zs.items():
            if z_list and consent_flags.get(f"{m}_enabled", True):
                mean_z = float(np.mean(z_list))
                # Map Z-score: 0 -> 0, 1 -> 25, 2 -> 60, 3+ -> 100
                score = min(100.0, max(0.0, mean_z * 30.0))
                modality_scores[m] = round(score, 1)
            else:
                modality_scores[m] = 0.0

        # Try ML Model prediction if loaded
        if cls._ml_model is not None:
            try:
                # Prepare feature vector
                feature_vector = np.array([modality_scores.get(m, 0.0) for m in ["keyboard", "usage", "motion", "work", "mobility"]]).reshape(1, -1)
                predicted_stage = int(cls._ml_model.predict(feature_vector)[0])
                probs = cls._ml_model.predict_proba(feature_vector)[0]
                confidence = float(np.max(probs))
                
                # Base score from probabilities
                base_score = float(np.sum(probs * np.array([10, 35, 60, 80, 95])))
            except Exception:
                predicted_stage = None
        else:
            predicted_stage = None

        # Deterministic Z-score algorithm (fallback / primary)
        active_scores = [v for k, v in modality_scores.items() if consent_flags.get(f"{k}_enabled", True) and v > 0]
        if active_scores:
            raw_composite = float(np.mean(active_scores)) * 0.7 + float(np.max(active_scores)) * 0.3
        else:
            raw_composite = 0.0

        # Temporal persistence multiplier
        # Single day transient spikes are dampened unless persistence_days >= 2
        if persistence_days == 1 and raw_composite > 40:
            temporal_multiplier = 0.85
        elif persistence_days >= 3:
            temporal_multiplier = 1.15
        else:
            temporal_multiplier = 1.0

        final_risk_score = min(100.0, max(0.0, raw_composite * temporal_multiplier))

        # Classify Stage based on score & persistence
        if final_risk_score < 25.0:
            stage = 0
        elif final_risk_score < 48.0:
            stage = 1
        elif final_risk_score < 72.0:
            stage = 2 if persistence_days >= 2 else 1
        elif final_risk_score < 88.0:
            stage = 3
        else:
            stage = 4

        # If ML model predicted a stage, combine with fallback
        if predicted_stage is not None:
            stage = max(stage, predicted_stage)
            confidence = min(0.98, max(0.70, confidence))
        else:
            confidence = 0.90

        stage_label = STAGE_LABELS.get(stage, STAGE_LABELS[0])

        return round(final_risk_score, 1), stage, stage_label, round(confidence, 2), modality_scores
