from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from app.database import Base
from app.models.domain import User, BehavioralTelemetry, Consent
from app.services.baseline_engine import BaselineEngine
from app.services.temporal_engine import TemporalEngine
from app.services.ml_risk_engine import MLRiskEngine
from app.services.explainability import ExplainabilityEngine
from app.services.synthetic_scenarios import SyntheticScenarioGenerator

def run_scenarios():
    engine = create_engine("sqlite:///:memory:", connect_args={"check_same_thread": False})
    TestingSessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
    
    Base.metadata.create_all(bind=engine)
    db = TestingSessionLocal()
    
    # 1. Establish User Baseline with 30 days of stable data
    user = User(id="user_synth", name="Synthetic User", email="synth@example.com", hashed_password="pwd")
    consent = Consent(user_id="user_synth", keyboard_enabled=True, usage_enabled=True, motion_enabled=True, work_enabled=True, mobility_enabled=True)
    db.add(user)
    db.add(consent)
    db.commit()

    baseline_recs = SyntheticScenarioGenerator.get_baseline_records(30)
    for r in baseline_recs:
        db.add(BehavioralTelemetry(user_id="user_synth", **r))
    db.commit()

    BaselineEngine.recalculate_user_baseline(db, "user_synth")
    consent_flags = {m: True for m in ["keyboard_enabled", "usage_enabled", "motion_enabled", "work_enabled", "mobility_enabled"]}

    results = []

    # Scenario 1: Stable User
    stable_data = SyntheticScenarioGenerator.get_stable_scenario()
    z_stable = BaselineEngine.compute_feature_z_scores(db, "user_synth", stable_data)
    score1, stage1, label1, conf1, _ = MLRiskEngine.evaluate_risk(z_stable, 1, consent_flags, "established", 30)
    results.append(("Stable User", score1, label1, conf1, "Stage 0 (Score < 20)"))

    # Scenario 2: Isolated Sleep Disruption
    sleep_data = SyntheticScenarioGenerator.get_sleep_disruption_scenario()
    z_sleep = BaselineEngine.compute_feature_z_scores(db, "user_synth", sleep_data)
    score2, stage2, label2, conf2, _ = MLRiskEngine.evaluate_risk(z_sleep, 1, consent_flags, "established", 30)
    results.append(("Isolated Sleep Disruption", score2, label2, conf2, "Stage 1 (Early Deviation)"))

    # Scenario 3: Acute Single-Day Spike
    acute_data = SyntheticScenarioGenerator.get_acute_deviation_scenario()
    z_acute = BaselineEngine.compute_feature_z_scores(db, "user_synth", acute_data)
    score3, stage3, label3, conf3, _ = MLRiskEngine.evaluate_risk(z_acute, 1, consent_flags, "established", 30)
    results.append(("Acute Single-Day Spike", score3, label3, conf3, "Stage 1-2 (Dampened transient)"))

    # Scenario 4: Persistent Multimodal Deviation (5 Days)
    persistent_data = SyntheticScenarioGenerator.get_persistent_multimodal_scenario()
    z_pers = BaselineEngine.compute_feature_z_scores(db, "user_synth", persistent_data[-1])
    score4, stage4, label4, conf4, _ = MLRiskEngine.evaluate_risk(z_pers, 5, consent_flags, "established", 30)
    results.append(("Persistent Multimodal (5d)", score4, label4, conf4, "Stage 3-4 (High Concern)"))

    # Scenario 5: Recovery
    recovery_data = SyntheticScenarioGenerator.get_recovery_scenario()
    z_rec = BaselineEngine.compute_feature_z_scores(db, "user_synth", recovery_data[-1])
    score5, stage5, label5, conf5, _ = MLRiskEngine.evaluate_risk(z_rec, 1, consent_flags, "established", 30)
    results.append(("Recovery Phase", score5, label5, conf5, "Stage 0-1 (Returning to baseline)"))

    print("\n==========================================================================================")
    print("                 SYNTHETIC SCENARIO BEHAVIORAL EVALUATION TABLE                            ")
    print("==========================================================================================")
    print(f"{'Scenario':<30} | {'Score':<6} | {'Stage Label':<32} | {'Conf':<5} | {'Expected':<28}")
    print("-" * 105)
    for name, score, label, conf, exp in results:
        print(f"{name:<30} | {score:<6.1f} | {label:<32} | {conf:<5.1f} | {exp:<28}")
    print("==========================================================================================\n")

    db.close()
    Base.metadata.drop_all(bind=engine)

if __name__ == "__main__":
    run_scenarios()
