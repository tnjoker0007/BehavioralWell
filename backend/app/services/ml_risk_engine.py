from abc import ABC, abstractmethod
import os
import joblib
import numpy as np
from typing import Dict, Any, Tuple, Optional, List
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

# Centralized exact ML Feature Order — DO NOT ALTER ORDER
ML_FEATURE_ORDER = [
    "typing_speed_z",
    "key_press_duration_z",
    "correction_rate_z",
    "screen_time_z",
    "night_usage_z",
    "acceleration_variance_z",
    "task_accuracy_z",
    "speed_variance_z"
]

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
        """Loads trained Random Forest model without retraining or modifying weights."""
        model_path = os.path.join(settings.MODEL_DIR, "behavioral_risk_rf.joblib")
        if os.path.exists(model_path):
            try:
                self._ml_model = joblib.load(model_path)
                classes_list = list(self._ml_model.classes_) if hasattr(self._ml_model, "classes_") else []
                n_feats = getattr(self._ml_model, "n_features_in_", 8)
                model_type = type(self._ml_model).__name__
                print(f"[ML MODEL] loaded=true type={model_type} features={n_feats} classes={classes_list}")
            except Exception as e:
                print(f"[ML MODEL] load_failed error={e}")
                self._ml_model = None

    def get_model_diagnostics(self) -> Dict[str, Any]:
        """Safe internal diagnostic reporter."""
        return {
            "model_loaded": self._ml_model is not None,
            "model_type": type(self._ml_model).__name__ if self._ml_model else None,
            "feature_count": getattr(self._ml_model, "n_features_in_", 0) if self._ml_model else 0,
            "expected_feature_order": ML_FEATURE_ORDER,
            "classes": list(self._ml_model.classes_) if (self._ml_model and hasattr(self._ml_model, "classes_")) else []
        }

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

        # 1. Group Z-scores by Modality & Compute Modality Scores
        modality_zs: Dict[str, list] = {m: [] for m in ALL_MODALITIES}
        for feat, z in z_scores.items():
            base_feat = feat.replace("_z", "")
            modality = MODALITY_MAP.get(base_feat, MODALITY_MAP.get(feat, "keyboard"))
            weight = FEATURE_CONFIG.get(base_feat, {}).get("weight", 1.0)
            
            # Deviations >= 0.6 std-dev are treated as active signals above noise floor
            if z >= 0.6:
                effective_z = (z - 0.5) * weight
                modality_zs[modality].append(effective_z)

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

        # 2. Deterministic Modality Composite Calculation
        active_scores = [v for k, v in modality_scores.items() if consent_flags.get(f"{k}_enabled", True) and v > 0]
        if active_scores:
            peak_modality_score = float(np.max(active_scores))
            mean_modality_score = float(np.mean(active_scores))
            deterministic_composite = (peak_modality_score * 0.5) + (mean_modality_score * 0.5)
            
            if active_elevated_modalities == 1:
                synergy_multiplier = 0.45
            elif active_elevated_modalities == 2:
                synergy_multiplier = 0.85
            elif active_elevated_modalities >= 3:
                synergy_multiplier = 1.25
            else:
                synergy_multiplier = 1.0

            deterministic_composite *= synergy_multiplier
        else:
            deterministic_composite = 0.0

        # 3. Attempt Random Forest Inference (Requires ALL 8 features present and consented)
        fallback = True
        rf_prediction = 0
        rf_confidence = 0.0

        if self._ml_model is not None:
            # Helper to retrieve feature Z-score by exact key or suffix
            def get_z(name_with_z: str) -> Optional[float]:
                base_name = name_with_z.replace("_z", "")
                v = z_scores.get(name_with_z)
                if v is None:
                    v = z_scores.get(base_name)
                return float(v) if v is not None else None

            feature_vals = [get_z(f) for f in ML_FEATURE_ORDER]
            all_features_present = all(v is not None for v in feature_vals)
            all_modalities_consented = all(consent_flags.get(f"{m}_enabled", True) for m in ALL_MODALITIES)

            if all_features_present and all_modalities_consented:
                try:
                    import pandas as pd
                    # Construct DataFrame matching exact ML_FEATURE_ORDER by column names
                    X = pd.DataFrame([feature_vals], columns=ML_FEATURE_ORDER)
                    
                    # Predict class and probabilities
                    rf_prediction = int(self._ml_model.predict(X)[0])
                    probabilities = self._ml_model.predict_proba(X)[0]
                    
                    # Explicit class probability mapping matching model.classes_
                    class_probabilities = {
                        int(cls): float(prob)
                        for cls, prob in zip(self._ml_model.classes_, probabilities)
                    }
                    rf_confidence = class_probabilities.get(rf_prediction, 0.5)
                    fallback = False

                    # Safe diagnostic log — No telemetry values, tokens, or PII logged
                    print(f"[ML INFERENCE] model=behavioral_risk_rf features=8 prediction={rf_prediction} confidence={rf_confidence:.2f} fallback=false")
                except Exception as ex:
                    fallback = True
                    print(f"[ML INFERENCE] model=behavioral_risk_rf fallback=true error={ex}")
            else:
                fallback = True
                print(f"[ML INFERENCE] model=behavioral_risk_rf fallback=true reason=missing_features_or_consent")
        else:
            fallback = True
            print(f"[ML INFERENCE] model=behavioral_risk_rf fallback=true reason=model_not_loaded")

        # 4. Fusion Layer (Blend ML evidence with Deterministic score)
        if not fallback:
            # Model Evidence Score: scale predicted stage (0-4) and model confidence
            ml_evidence_score = (rf_prediction * 20.0) + (rf_confidence * 15.0 - 7.5)
            raw_composite = (deterministic_composite * 0.4) + (ml_evidence_score * 0.6)
        else:
            raw_composite = deterministic_composite

        # 5. Temporal Persistence Adjustment
        if persistence_days <= 1 and raw_composite > 30:
            temporal_multiplier = 0.75
        elif persistence_days >= 3:
            temporal_multiplier = 1.20
        else:
            temporal_multiplier = 1.0

        risk_score = min(100.0, max(0.0, raw_composite * temporal_multiplier))

        # Initial stage mapping from risk score
        if risk_score < 20.0:
            stage = 0
        elif risk_score < 40.0:
            stage = 1
        elif risk_score < 60.0:
            stage = 2 if persistence_days >= 2 else 1
        elif risk_score < 80.0:
            stage = 3
        else:
            stage = 4

        # 6. Cross-Modal Safety Gating Restriction
        # - Single modality anomaly CANNOT trigger Stage 3 or Stage 4 (max Stage 2)
        # - Stage 4 REQUIRES multi-modal evidence AND multi-day persistence
        if active_elevated_modalities < 2:
            stage = min(stage, 2)
        elif stage == 4 and persistence_days < 2:
            stage = 3

        # 7. Cold-Start Gating Restriction
        # - Insufficient baseline maturity caps stage at Stage 1 max and score at 35.0 max
        if baseline_status == "insufficient":
            stage = min(stage, 1)
            risk_score = min(risk_score, 35.0)

        # 8. Confidence Score Calculation (0-100)
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
        if not fallback:
            # ML prediction confidence boosts overall system confidence
            confidence_score = min(100.0, confidence_score * 0.8 + (rf_confidence * 100.0) * 0.2)

        stage_label = STAGE_LABELS.get(stage, STAGE_LABELS[0])

        return round(risk_score, 1), stage, stage_label, round(confidence_score, 1), modality_scores

MLRiskEngine = HybridMLRiskEngine()
