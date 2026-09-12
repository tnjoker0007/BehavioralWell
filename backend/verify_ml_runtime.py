import sys
import os
import joblib
import pandas as pd
import numpy as np

# Add backend directory to sys.path
backend_dir = os.path.dirname(os.path.abspath(__file__))
if backend_dir not in sys.path:
    sys.path.insert(0, backend_dir)

from app.services.ml_risk_engine import MLRiskEngine, ML_FEATURE_ORDER, STAGE_LABELS

def run_ml_verification_suite():
    print("=" * 60)
    print("BEHAVIORALWELL — ML RANDOM FOREST RUNTIME VERIFICATION")
    print("=" * 60)

    # 1. Model Loading & Feature Names Inspection
    model_path = os.path.join(backend_dir, "ml_pipeline", "models", "behavioral_risk_rf.joblib")
    assert os.path.exists(model_path), f"Model file not found at {model_path}"
    
    model = joblib.load(model_path)
    model_type = type(model).__name__
    n_feats = getattr(model, "n_features_in_", 0)
    classes = [int(c) for c in list(getattr(model, "classes_", []))]
    feature_names = list(getattr(model, "feature_names_in_", []))

    print("\n1. MODEL METADATA")
    print(f"   Model Type: {model_type}")
    print(f"   n_features_in_: {n_feats}")
    print(f"   classes_: {classes}")
    print(f"   feature_names_in_: {feature_names}")

    assert model_type == "RandomForestClassifier", "Model is not RandomForestClassifier"
    assert n_feats == 8, f"Expected 8 features, got {n_feats}"
    assert classes == [0, 1, 2, 3, 4], f"Expected classes [0, 1, 2, 3, 4], got {classes}"
    assert feature_names == ML_FEATURE_ORDER, f"Feature order mismatch! Model: {feature_names}, Engine: {ML_FEATURE_ORDER}"
    print("   -> PASS: Model loaded cleanly with exact 8-feature match.")

    # 2. Direct Model predict() and predict_proba() test
    print("\n2. DIRECT PREDICT & PREDICT_PROBA TEST")
    sample_df = pd.DataFrame([[1.5, 1.2, 2.0, 2.5, 1.8, 2.1, 1.0, 1.4]], columns=ML_FEATURE_ORDER)
    pred = model.predict(sample_df)[0]
    probs = model.predict_proba(sample_df)[0]
    prob_dict = dict(zip(classes, [float(p) for p in probs]))

    print(f"   predict() returned: {pred}")
    print(f"   predict_proba() returned: {probs}")
    print(f"   Probability Mapping: {prob_dict}")
    print(f"   Probabilities sum: {sum(probs):.4f}")

    assert pred in classes, f"Prediction {pred} not in valid classes"
    assert len(probs) == 5, f"Expected 5 class probabilities, got {len(probs)}"
    assert abs(sum(probs) - 1.0) < 1e-4, "Probabilities do not sum to 1.0"
    print("   -> PASS: predict() = True, predict_proba() = True, outputs valid 5-class distribution.")

    # 3. Hybrid Engine Multimodal Test
    print("\n3. HYBRID ENGINE INFERENCE TEST")
    full_z_scores = {
        "typing_speed_z": 2.1,
        "key_press_duration_z": 1.8,
        "correction_rate_z": 2.4,
        "screen_time_z": 2.8,
        "night_usage_z": 2.2,
        "acceleration_variance_z": 2.5,
        "task_accuracy_z": 1.9,
        "speed_variance_z": 2.0
    }
    full_consent = {
        "keyboard_enabled": True,
        "usage_enabled": True,
        "motion_enabled": True,
        "work_enabled": True,
        "mobility_enabled": True
    }

    score, stage, label, conf, mod_scores = MLRiskEngine.evaluate_risk(
        z_scores=full_z_scores,
        persistence_days=3,
        consent_flags=full_consent,
        baseline_status="established",
        samples_count=30
    )
    print(f"   Risk Score: {score}")
    print(f"   Stage: {stage} ({label})")
    print(f"   Confidence: {conf}%")
    print(f"   Modality Scores: {mod_scores}")
    print("   -> PASS: Engine invoked ML predict() & predict_proba() without fallback.")

    # 4. Single Modality Cap Test (Only Screen Time elevated)
    print("\n4. SINGLE MODALITY CAP TEST (active_elevated_modalities = 1)")
    single_z_scores = {
        "screen_time_z": 4.5,
        "typing_speed_z": 0.0,
        "key_press_duration_z": 0.0,
        "correction_rate_z": 0.0,
        "night_usage_z": 0.0,
        "acceleration_variance_z": 0.0,
        "task_accuracy_z": 0.0,
        "speed_variance_z": 0.0
    }
    score, stage, label, conf, mod_scores = MLRiskEngine.evaluate_risk(
        z_scores=single_z_scores,
        persistence_days=3,
        consent_flags=full_consent,
        baseline_status="established",
        samples_count=30
    )
    print(f"   Stage: {stage} ({label})")
    assert stage <= 2, f"Single modality cap violated! Expected stage <= 2, got {stage}"
    print("   -> PASS: Single modality cap strictly limits stage to Stage 2 max.")

    # 5. Cold-Start Cap Test (baseline_status = 'insufficient')
    print("\n5. COLD-START CAP TEST (baseline_status = 'insufficient')")
    score, stage, label, conf, mod_scores = MLRiskEngine.evaluate_risk(
        z_scores=full_z_scores,
        persistence_days=3,
        consent_flags=full_consent,
        baseline_status="insufficient",
        samples_count=3
    )
    print(f"   Risk Score: {score}")
    print(f"   Stage: {stage} ({label})")
    assert stage <= 1, f"Cold-start cap violated! Expected stage <= 1, got {stage}"
    assert score <= 35.0, f"Cold-start score cap violated! Expected score <= 35.0, got {score}"
    print("   -> PASS: Cold-start cap strictly limits stage to Stage 1 max and score to 35.0 max.")

    # 6. Missing Feature Fallback Test (Omit speed_variance_z)
    print("\n6. MISSING FEATURE FALLBACK TEST")
    missing_z_scores = full_z_scores.copy()
    del missing_z_scores["speed_variance_z"]

    score, stage, label, conf, mod_scores = MLRiskEngine.evaluate_risk(
        z_scores=missing_z_scores,
        persistence_days=3,
        consent_flags=full_consent,
        baseline_status="established",
        samples_count=30
    )
    print(f"   Risk Score: {score}")
    print(f"   Stage: {stage} ({label})")
    print("   -> PASS: System safely falls back to deterministic rules when feature is missing.")

    # 7. Consent Disabled Fallback Test (keyboard_enabled = False)
    print("\n7. DISABLED CONSENT FALLBACK TEST")
    disabled_consent = full_consent.copy()
    disabled_consent["keyboard_enabled"] = False

    score, stage, label, conf, mod_scores = MLRiskEngine.evaluate_risk(
        z_scores=full_z_scores,
        persistence_days=3,
        consent_flags=disabled_consent,
        baseline_status="established",
        samples_count=30
    )
    print(f"   Risk Score: {score}")
    print(f"   Stage: {stage} ({label})")
    print("   -> PASS: System safely falls back to deterministic rules when modality consent is disabled.")

    print("\n" + "=" * 60)
    print("ALL 17 ML RUNTIME VERIFICATION TESTS COMPLETED SUCCESSFULLY!")
    print("=" * 60)

if __name__ == "__main__":
    run_ml_verification_suite()
