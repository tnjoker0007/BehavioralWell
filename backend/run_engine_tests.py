import sys
import traceback
from tests.test_behavioral_engine import (
    TestingSessionLocal, engine, Base, User, Consent,
    test_1_baseline_calculation,
    test_2_baseline_maturity,
    test_3_z_score_directions,
    test_4_zero_variance_handling,
    test_5_missing_data_handling,
    test_6_outlier_handling,
    test_7_8_9_temporal_engine,
    test_10_modality_aggregation,
    test_11_12_risk_thresholds_and_confidence,
    test_13_cold_start_handling,
    test_14_explainability,
    test_15_stable_synthetic_scenario,
    test_16_acute_deviation_scenario,
    test_17_persistent_multimodal_scenario,
    test_18_recovery_scenario,
    test_19_20_single_modality_vs_multimodal_anomaly
)

def run_all_engine_tests():
    test_functions = [
        ("1. Baseline Calculation", test_1_baseline_calculation),
        ("2. Progressive Baseline Maturity", test_2_baseline_maturity),
        ("3. Directional Z-Score Calculation", test_3_z_score_directions),
        ("4. Zero Variance Floor Handling", test_4_zero_variance_handling),
        ("5. Missing Data Handling", test_5_missing_data_handling),
        ("6. Outlier Handling", test_6_outlier_handling),
        ("7-9. Temporal Rolling Averages & Slope", test_7_8_9_temporal_engine),
        ("10. Modality Aggregation", test_10_modality_aggregation),
        ("11-12. Risk Thresholds & Confidence Scores", test_11_12_risk_thresholds_and_confidence),
        ("13. Cold Start Handling", test_13_cold_start_handling),
        ("14. Non-Diagnostic Explainability", test_14_explainability),
        ("15. Stable Synthetic Scenario", test_15_stable_synthetic_scenario),
        ("16. Acute Deviation Scenario", test_16_acute_deviation_scenario),
        ("17. Persistent Multimodal Scenario", test_17_persistent_multimodal_scenario),
        ("18. Recovery Scenario", test_18_recovery_scenario),
        ("19-20. Single vs Multimodal Anomaly Gating", test_19_20_single_modality_vs_multimodal_anomaly)
    ]

    passed = 0
    failed = 0

    print("==================================================")
    print("   BEHAVIORAL INTELLIGENCE ENGINE SUITE RUNNER   ")
    print("==================================================\n")

    for name, func in test_functions:
        # Create fresh DB session for each test function
        Base.metadata.create_all(bind=engine)
        db = TestingSessionLocal()
        test_user = User(id="test_user_ai", name="AI Test User", email="aitest@example.com", hashed_password="hashed_dummy")
        db.add(test_user)
        consent = Consent(user_id="test_user_ai", keyboard_enabled=True, usage_enabled=True, motion_enabled=True, work_enabled=True, mobility_enabled=True)
        db.add(consent)
        db.commit()

        try:
            func(db)
            print(f"[PASS] {name}")
            passed += 1
        except Exception as e:
            print(f"[FAIL] {name}: {e}")
            traceback.print_exc()
            failed += 1
        finally:
            db.close()
            Base.metadata.drop_all(bind=engine)

    print("\n==================================================")
    print(f"RESULTS: {passed} PASSED, {failed} FAILED out of {len(test_functions)} test groups")
    print("==================================================")

    if failed > 0:
        sys.exit(1)

if __name__ == "__main__":
    run_all_engine_tests()
