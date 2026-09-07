from abc import ABC, abstractmethod
import os
import joblib
import numpy as np
from typing import Dict, Any, Tuple, Optional
from app.config import settings
from app.services.feature_directions import FEATURE_CONFIG, MODALITY_MAP

STAGE_LABELS = {
    0: "Stage 0 — Stable",
    1: "Stage 1 — Early Deviation",
    2: "Stage 2 — Persistent Behavioral Deviation",
    3: "Stage 3 — Elevated Risk",
    4: "Stage 4 — High Concern"
}

ALL_MODALITIES = ["keyboard", "usage", "motion", "work", "mobility"]

class BaseRiskEngine(ABC):
    @abstractmethod
    def evaluate_risk(
        self,
        z_scores: Dict[str, float],
        persistence_days: int,
        consent_flags: Dict[str, bool],
        baseline_status: str = "established",
        samples_count: int = 30
    ) -> Tuple[float, int, str, float, Dict[str, float]]:
        pass

class HybridMLRiskEngine(BaseRiskEngine):
    def __init__(self):
        self._ml_model = None
        self._load_model()

    def _load_model(self):
        model_path = os.path.join(settings.MODEL_DIR, "behavioral_risk_rf.joblib")
        if os.path.exists(model_path):
            try:
                self._ml_model = joblib.load(model_path)
            except Exception:
                self._ml_model = None

    def evaluate_risk(
        self,
        z_scores: Dict[str, float],
        persistence_days: int,
        consent_flags: Dict[str, bool],
        baseline_status: str = "established",
        samples_count: int = 30
    ) -> Tuple[float, int, str, float, Dict[str, float]]:
        
        modality_scores = {m: 0.0 for m in ALL_MODALITIES}

        if not z_scores:
            confidence = 92.0 if baseline_status == "established" else 30.0
            return 0.0, 0, STAGE_LABELS[0], confidence, modality_scores

        # 1. Group Z-scores by Modality (Ignore normal baseline noise < 0.6 sigma)
        modality_zs: Dict[str, list] = {m: [] for m in ALL_MODALITIES}
        for feat, z in z_scores.items():
            modality = MODALITY_MAP.get(feat, "keyboard")
            weight = FEATURE_CONFIG.get(feat, {}).get("weight", 1.0)
            
            # Apply noise floor: deviations below 0.6 std-dev are treated as normal baseline noise
            if z >= 0.6:
                effective_z = (z - 0.5) * weight
                modality_zs[modality].append(effective_z)

        # 2. Compute Modality Deviation Scores (0–100 scale)
        active_elevated_modalities = 0
        for m, z_list in modality_zs.items():
            is_enabled = consent_flags.get(f"{m}_enabled", True)
            if z_list and is_enabled:
                avg_z = float(np.mean(z_list))
                max_z = float(np.max(z_list))
                combo_z = (avg_z * 0.5) + (max_z * 0.5)
                score = min(100.0, max(0.0, combo_z * 30.0))
                modality_scores[m] = round(score, 1)
                if score >= 25.0:
                    active_elevated_modalities += 1
            else:
                modality_scores[m] = 0.0

        # 3. Layer 3: Modality Synergy Aggregation
        active_scores = [v for k, v in modality_scores.items() if consent_flags.get(f"{k}_enabled", True) and v > 0]
        if active_scores:
            peak_modality_score = float(np.max(active_scores))
            mean_modality_score = float(np.mean(active_scores))
            raw_composite = (peak_modality_score * 0.5) + (mean_modality_score * 0.5)
            
            # Multimodal Synergy Multiplier:
            # - Single modality anomaly alone is capped to Stage 1/2
            # - Multi-modality anomalies (> 2 active modalities) amplify the score
            if active_elevated_modalities == 1:
                synergy_multiplier = 0.45
            elif active_elevated_modalities == 2:
                synergy_multiplier = 0.85
            elif active_elevated_modalities >= 3:
                synergy_multiplier = 1.25
            else:
                synergy_multiplier = 1.0

            raw_composite *= synergy_multiplier
        else:
            raw_composite = 0.0

        # 4. Layer 2: Temporal Persistence Multiplier
        if persistence_days <= 1 and raw_composite > 30:
            temporal_multiplier = 0.75
        elif persistence_days >= 3:
            temporal_multiplier = 1.20
        else:
            temporal_multiplier = 1.0

        risk_score = min(100.0, max(0.0, raw_composite * temporal_multiplier))

        # 5. Classify Risk Stage with Strict Cross-Modal & Persistence Requirements
        if risk_score < 20.0:
            stage = 0
        elif risk_score < 40.0:
            stage = 1
        elif risk_score < 60.0:
            stage = 2 if persistence_days >= 2 else 1
        elif risk_score < 80.0:
            # Stage 3 requires at least 2 active modalities
            stage = 3 if active_elevated_modalities >= 2 else 2
        else:
            # Stage 4 REQUIRES multi-modal evidence AND multi-day persistence
            if active_elevated_modalities >= 2 and persistence_days >= 2:
                stage = 4
            elif active_elevated_modalities >= 2:
                stage = 3
            else:
                stage = 2

        # 6. Cold Start Constraint
        if baseline_status == "insufficient":
            stage = min(stage, 1)
            risk_score = min(risk_score, 35.0)

        # 7. Confidence Score Calculation (0-100)
        maturity_weight = {
            "insufficient": 25.0,
            "early": 55.0,
            "developing": 80.0,
            "established": 95.0
        }.get(baseline_status, 50.0)

        enabled_modalities_count = sum(1 for m in ALL_MODALITIES if consent_flags.get(f"{m}_enabled", True))
        coverage_weight = (enabled_modalities_count / 5.0) * 100.0
        density_weight = min(100.0, (samples_count / 30.0) * 100.0)

        confidence_score = (maturity_weight * 0.40) + (coverage_weight * 0.30) + (density_weight * 0.30)

        stage_label = STAGE_LABELS.get(stage, STAGE_LABELS[0])

        return round(risk_score, 1), stage, stage_label, round(confidence_score, 1), modality_scores

MLRiskEngine = HybridMLRiskEngine()
