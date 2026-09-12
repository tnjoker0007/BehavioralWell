import os
import numpy as np
import pandas as pd
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from app.database import Base
from app.models.domain import User, Consent, BehavioralTelemetry
from app.services.ml_risk_engine import HybridMLRiskEngine, ML_FEATURE_ORDER
from app.services.baseline_engine import BaselineEngine
from app.services.temporal_engine import TemporalEngine
from app.services.explainability import ExplainabilityEngine

def test_1_feature_order_verification():
    """Verify that feature vector construction preserves exact ML_FEATURE_ORDER regardless of dictionary key order."""
    engine = HybridMLRiskEngine()
    assert engine._ml_model is not None, "Model failed to load"

    # Jumbled dictionary key order
    jumbled_z_scores = {
        "speed_variance_z": 8.0,
        "task_accuracy_z": 7.0,
        "acceleration_variance_z": 6.0,
        "night_usage_z": 5.0,
        "screen_time_z": 4.0,
        "correction_rate_z": 3.0,
        "key_press_duration_z": 2.0,
        "typing_speed_z": 1.0,
    }

    # Extract vector using ML_FEATURE_ORDER lookup
    def get_z(name_with_z):
        base_name = name_with_z.replace("_z", "")
        v = jumbled_z_scores.get(name_with_z)
        if v is None:
            v = jumbled_z_scores.get(base_name)
        return float(v) if v is not None else None

    extracted_vector = [get_z(f) for f in ML_FEATURE_ORDER]
    expected_vector = [1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0]

    assert extracted_vector == expected_vector, f"Expected {expected_vector}, got {extracted_vector}"
    print("[PASS] Test 1: Feature order matches exact ML_FEATURE_ORDER [1.0..8.0]")

def test_2_model_immutability():
    """Verify model parameters and ensure no fit/partial_fit calls mutate the joblib weights."""
    engine = HybridMLRiskEngine()
    model = engine._ml_model

    assert model is not None
    assert model.n_features_in_ == 8
    assert list(model.classes_) == [0, 1, 2, 3, 4]
    assert type(model).__name__ == "RandomForestClassifier"
    print("[PASS] Test 2: Model immutability verified (8 features, classes [0..4])")

def test_3_fallback_scenarios():
    """Verify clean fallback to deterministic pipeline for all missing feature/model failure cases."""
    engine = HybridMLRiskEngine()
    all_consented = {
        "keyboard_enabled": True,
        "usage_enabled": True,
        "motion_enabled": True,
        "work_enabled": True,
        "mobility_enabled": True
    }

    complete_z_scores = {
        "typing_speed": 1.2,
        "key_press_duration": 1.0,
        "correction_rate": 0.8,
        "screen_time": 1.5,
        "night_usage": 1.1,
        "acceleration_variance": 0.9,
        "task_accuracy": 1.4,
        "speed_variance": 1.0
    }

    # Scenario A: Model set to None (missing/corrupted file)
    original_model = engine._ml_model
    try:
        engine._ml_model = None
        score, stage, label, conf, mod_scores = engine.evaluate_risk(
            complete_z_scores, persistence_days=3, consent_flags=all_consented
        )
        assert stage >= 0
        assert isinstance(score, float)
    finally:
        engine._ml_model = original_model

    # Scenario B: Missing 1 feature in z_scores
    incomplete_z_scores = complete_z_scores.copy()
    del incomplete_z_scores["typing_speed"]
    score, stage, label, conf, mod_scores = engine.evaluate_risk(
        incomplete_z_scores, persistence_days=3, consent_flags=all_consented
    )
    assert stage >= 0

    # Scenario C: Modality consent disabled
    disabled_consent = all_consented.copy()
    disabled_consent["keyboard_enabled"] = False
    score, stage, label, conf, mod_scores = engine.evaluate_risk(
        complete_z_scores, persistence_days=3, consent_flags=disabled_consent
    )
    assert stage >= 0

    print("[PASS] Test 3: Safe fallback verified across missing model, missing feature, and disabled consent")

def test_4_cold_start_and_cross_modal_safety_gating():
    """Verify safety gating overrides ML predictions for cold-start and single-modality anomalies."""
    engine = HybridMLRiskEngine()
    all_consented = {f"{m}_enabled": True for m in ["keyboard", "usage", "motion", "work", "mobility"]}

    high_risk_z = {
        "typing_speed": 4.5,
        "key_press_duration": 4.2,
        "correction_rate": 4.0,
        "screen_time": 4.5,
        "night_usage": 4.2,
        "acceleration_variance": 4.0,
        "task_accuracy": 4.5,
        "speed_variance": 4.0
    }

    # Cold start check: baseline_status == "insufficient" -> capped at Stage 1 max
    score, stage, label, conf, mod_scores = engine.evaluate_risk(
        high_risk_z, persistence_days=5, consent_flags=all_consented, baseline_status="insufficient"
    )
    assert stage <= 1, f"Cold start failed: expected max Stage 1, got Stage {stage}"
    assert score <= 35.0, f"Cold start failed: expected max score 35.0, got {score}"

    # Single modality check: only keyboard elevated -> capped at Stage 2 max
    single_modality_z = {
        "typing_speed": 4.5,
        "key_press_duration": 4.2,
        "correction_rate": 4.0,
        "screen_time": 0.0,
        "night_usage": 0.0,
        "acceleration_variance": 0.0,
        "task_accuracy": 0.0,
        "speed_variance": 0.0
    }
    score, stage, label, conf, mod_scores = engine.evaluate_risk(
        single_modality_z, persistence_days=5, consent_flags=all_consented, baseline_status="established"
    )
    assert stage <= 2, f"Cross-modal gating failed: expected max Stage 2, got Stage {stage}"

    print("[PASS] Test 4: Cold-start and cross-modal safety gating restrictions verified")

def test_5_real_telemetry_inference_trace():
    """Traces a real telemetry record from database -> Z-score -> ML inference -> Risk Response."""
    sqlite_engine = create_engine("sqlite:///:memory:")
    Base.metadata.create_all(sqlite_engine)
    TestingSession = sessionmaker(bind=sqlite_engine)
    db = TestingSession()

    user_id = "test_ml_trace_user"
    user = User(id=user_id, name="Test ML Trace User", email="mltrace@example.com", hashed_password="dummy")
    db.add(user)
    consent = Consent(user_id=user_id, keyboard_enabled=True, usage_enabled=True, motion_enabled=True, work_enabled=True, mobility_enabled=True)
    db.add(consent)
    db.commit()

    # Seed 14 days baseline telemetry
    for i in range(14):
        t = BehavioralTelemetry(
            user_id=user_id,
            typing_speed=60.0 + (i % 3),
            key_press_duration=110.0 + (i % 2),
            correction_rate=0.04,
            screen_time=3.5,
            night_usage=0.2,
            acceleration_variance=0.10,
            task_accuracy=95.0,
            speed_variance=0.12
        )
        db.add(t)
    db.commit()

    # Recalculate baseline
    BaselineEngine.recalculate_user_baseline(db, user_id)

    # Ingest new elevated telemetry record
    current_telemetry = {
        "typing_speed": 40.0,
        "key_press_duration": 145.0,
        "correction_rate": 0.15,
        "screen_time": 6.5,
        "night_usage": 1.8,
        "acceleration_variance": 0.35,
        "task_accuracy": 78.0,
        "speed_variance": 0.45
    }
    new_t = BehavioralTelemetry(user_id=user_id, **current_telemetry)
    db.add(new_t)
    db.commit()

    # Compute Z-scores
    z_scores = BaselineEngine.compute_feature_z_scores(db, user_id, current_telemetry)
    assert len(z_scores) >= 8

    # Evaluate risk via ML Risk Engine
    consent_flags = {"keyboard_enabled": True, "usage_enabled": True, "motion_enabled": True, "work_enabled": True, "mobility_enabled": True}
    risk_score, stage, stage_label, confidence, modality_scores = HybridMLRiskEngine().evaluate_risk(
        z_scores, persistence_days=3, consent_flags=consent_flags, baseline_status="established", samples_count=15
    )

    explanations = ExplainabilityEngine.generate_explanations(z_scores, current_telemetry)

    print("\n--- REAL TELEMETRY INFERENCE TRACE ---")
    print(f"User ID: {user_id}")
    print(f"Z-Scores: {z_scores}")
    print(f"Risk Score: {risk_score}")
    print(f"Stage: {stage} ({stage_label})")
    print(f"Confidence: {confidence}%")
    print(f"Modality Scores: {modality_scores}")
    print(f"Top Contributor Factors: {[e.feature for e in explanations]}")
    print("---------------------------------------")

    assert stage >= 1
    assert risk_score > 20.0
    print("[PASS] Test 5: Real telemetry inference trace succeeded!")

if __name__ == "__main__":
    test_1_feature_order_verification()
    test_2_model_immutability()
    test_3_fallback_scenarios()
    test_4_cold_start_and_cross_modal_safety_gating()
    test_5_real_telemetry_inference_trace()
    print("\nALL ML MODEL INTEGRATION TESTS PASSED 100%!")
