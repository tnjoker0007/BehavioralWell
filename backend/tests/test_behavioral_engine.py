from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from app.database import Base
from app.models.domain import User, BehavioralTelemetry, UserBaseline, RiskAssessment, Consent
from app.services.baseline_engine import BaselineEngine
from app.services.temporal_engine import TemporalEngine
from app.services.ml_risk_engine import MLRiskEngine
from app.services.explainability import ExplainabilityEngine
from app.services.synthetic_scenarios import SyntheticScenarioGenerator

# In-memory SQLite DB for testing
SQLALCHEMY_DATABASE_URL = "sqlite:///:memory:"
engine = create_engine(SQLALCHEMY_DATABASE_URL, connect_args={"check_same_thread": False})
TestingSessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

def test_1_baseline_calculation(db_session):
    records = SyntheticScenarioGenerator.get_baseline_records(30)
    for r in records:
        db_session.add(BehavioralTelemetry(user_id="test_user_ai", **r))
    db_session.commit()

    baselines = BaselineEngine.recalculate_user_baseline(db_session, "test_user_ai")
    assert len(baselines) > 0
    typing_base = next(b for b in baselines if b.feature_name == "typing_speed")
    assert abs(typing_base.mean - 63.0) < 3.0
    assert typing_base.samples_count == 30

def test_2_baseline_maturity(db_session):
    status, conf = BaselineEngine.get_baseline_status_and_confidence(2)
    assert status == "insufficient"
    assert conf < 40.0

    status, conf = BaselineEngine.get_baseline_status_and_confidence(10)
    assert status == "early"

    status, conf = BaselineEngine.get_baseline_status_and_confidence(20)
    assert status == "developing"

    status, conf = BaselineEngine.get_baseline_status_and_confidence(35)
    assert status == "established"
    assert conf >= 85.0

def test_3_z_score_directions(db_session):
    # Establish baseline
    records = SyntheticScenarioGenerator.get_baseline_records(30)
    for r in records:
        db_session.add(BehavioralTelemetry(user_id="test_user_ai", **r))
    db_session.commit()
    BaselineEngine.recalculate_user_baseline(db_session, "test_user_ai")

    # Test concerning direction Z-score
    # typing_speed lower_is_deviation (mean=63.0, current=35.0 -> concerning z should be positive)
    z_map = BaselineEngine.compute_feature_z_scores(db_session, "test_user_ai", {"typing_speed": 35.0, "night_usage": 3.0})
    assert z_map["typing_speed"] > 1.5
    assert z_map["night_usage"] > 1.5

def test_4_zero_variance_handling(db_session):
    # Add identical samples (std = 0)
    for _ in range(10):
        db_session.add(BehavioralTelemetry(user_id="test_user_ai", screen_time=5.0))
    db_session.commit()

    baselines = BaselineEngine.recalculate_user_baseline(db_session, "test_user_ai")
    b = next(b for b in baselines if b.feature_name == "screen_time")
    assert b.std >= 1e-4

    z_map = BaselineEngine.compute_feature_z_scores(db_session, "test_user_ai", {"screen_time": 10.0})
    assert not any(v != v for v in z_map.values()) # check NaNs
    assert z_map["screen_time"] > 0

def test_5_missing_data_handling(db_session):
    records = SyntheticScenarioGenerator.get_baseline_records(10)
    for r in records:
        db_session.add(BehavioralTelemetry(user_id="test_user_ai", **r))
    db_session.commit()
    BaselineEngine.recalculate_user_baseline(db_session, "test_user_ai")

    z_map = BaselineEngine.compute_feature_z_scores(db_session, "test_user_ai", {"typing_speed": None, "screen_time": 4.0})
    assert "typing_speed" not in z_map
    assert "screen_time" in z_map

def test_6_outlier_handling(db_session):
    records = SyntheticScenarioGenerator.get_baseline_records(10)
    records.append({"screen_time": float('nan'), "typing_speed": float('inf')})
    for r in records:
        db_session.add(BehavioralTelemetry(user_id="test_user_ai", **r))
    db_session.commit()

    baselines = BaselineEngine.recalculate_user_baseline(db_session, "test_user_ai")
    assert len(baselines) > 0

def test_7_8_9_temporal_engine(db_session):
    # Add historical risk assessments
    for i in range(5):
        db_session.add(RiskAssessment(
            user_id="test_user_ai",
            risk_score=20.0 + i * 10.0,
            stage=1,
            stage_label="Stage 1",
            confidence=0.9,
            trend="increasing",
            persistence_days=i+1,
            top_contributors=[],
            modality_scores={}
        ))
    db_session.commit()

    p_days, trend, slope, r3, r7 = TemporalEngine.evaluate_persistence(db_session, "test_user_ai", {"screen_time": 2.5})
    assert p_days >= 3
    assert r3 > 0

def test_10_modality_aggregation(db_session):
    z_scores = {"typing_speed": 2.5, "screen_time": 2.2, "movement_intensity": 0.1}
    consent = {"keyboard_enabled": True, "usage_enabled": True, "motion_enabled": True}
    score, stage, label, conf, modalities = MLRiskEngine.evaluate_risk(z_scores, 1, consent, "established", 30)
    
    assert modalities["keyboard"] > 0
    assert modalities["usage"] > 0

def test_11_12_risk_thresholds_and_confidence(db_session):
    consent = {m: True for m in ["keyboard_enabled", "usage_enabled", "motion_enabled", "work_enabled", "mobility_enabled"]}
    
    score0, stage0, _, conf0, _ = MLRiskEngine.evaluate_risk({}, 1, consent, "established", 30)
    assert stage0 == 0
    assert conf0 > 80.0

    z_high = {"typing_speed": 3.0, "screen_time": 3.0, "night_usage": 3.0, "task_accuracy": 3.0}
    score3, stage3, _, _, _ = MLRiskEngine.evaluate_risk(z_high, 3, consent, "established", 30)
    assert stage3 >= 3
    assert score3 >= 60.0

def test_13_cold_start_handling(db_session):
    consent = {m: True for m in ["keyboard_enabled", "usage_enabled", "motion_enabled", "work_enabled", "mobility_enabled"]}
    z_high = {"typing_speed": 4.0, "screen_time": 4.0}
    score, stage, label, conf, _ = MLRiskEngine.evaluate_risk(z_high, 1, consent, "insufficient", 2)
    
    assert stage <= 1 # Clamped for cold start
    assert conf < 50.0

def test_14_explainability(db_session):
    z_scores = {"night_usage": 2.5, "typing_speed": 2.1}
    explanations = ExplainabilityEngine.generate_explanations(z_scores)
    assert len(explanations) == 2
    assert "personal baseline" in explanations[0].human_explanation
    assert not any(word in explanations[0].human_explanation.lower() for word in ["illness", "drug", "diagnosis", "depression"])

def test_15_stable_synthetic_scenario(db_session):
    records = SyntheticScenarioGenerator.get_baseline_records(30)
    for r in records:
        db_session.add(BehavioralTelemetry(user_id="test_user_ai", **r))
    db_session.commit()
    BaselineEngine.recalculate_user_baseline(db_session, "test_user_ai")

    stable_data = SyntheticScenarioGenerator.get_stable_scenario()
    z_scores = BaselineEngine.compute_feature_z_scores(db_session, "test_user_ai", stable_data)
    consent = {m: True for m in ["keyboard_enabled", "usage_enabled", "motion_enabled", "work_enabled", "mobility_enabled"]}
    score, stage, _, _, _ = MLRiskEngine.evaluate_risk(z_scores, 1, consent, "established", 30)
    
    assert stage == 0
    assert score < 20.0

def test_16_acute_deviation_scenario(db_session):
    records = SyntheticScenarioGenerator.get_baseline_records(30)
    for r in records:
        db_session.add(BehavioralTelemetry(user_id="test_user_ai", **r))
    db_session.commit()
    BaselineEngine.recalculate_user_baseline(db_session, "test_user_ai")

    acute_data = SyntheticScenarioGenerator.get_acute_deviation_scenario()
    z_scores = BaselineEngine.compute_feature_z_scores(db_session, "test_user_ai", acute_data)
    consent = {m: True for m in ["keyboard_enabled", "usage_enabled", "motion_enabled", "work_enabled", "mobility_enabled"]}
    score, stage, _, _, _ = MLRiskEngine.evaluate_risk(z_scores, 1, consent, "established", 30)
    
    # Transient single-day spike dampening prevents jumping straight to Stage 4
    assert stage < 4

def test_17_persistent_multimodal_scenario(db_session):
    records = SyntheticScenarioGenerator.get_baseline_records(30)
    for r in records:
        db_session.add(BehavioralTelemetry(user_id="test_user_ai", **r))
    db_session.commit()
    BaselineEngine.recalculate_user_baseline(db_session, "test_user_ai")

    persistent_data = SyntheticScenarioGenerator.get_persistent_multimodal_scenario()
    z_scores = BaselineEngine.compute_feature_z_scores(db_session, "test_user_ai", persistent_data[-1])
    consent = {m: True for m in ["keyboard_enabled", "usage_enabled", "motion_enabled", "work_enabled", "mobility_enabled"]}
    score, stage, _, _, _ = MLRiskEngine.evaluate_risk(z_scores, 4, consent, "established", 30)
    
    # Persistent multi-day multimodal deviation reaches high stage
    assert stage >= 3

def test_18_recovery_scenario(db_session):
    records = SyntheticScenarioGenerator.get_baseline_records(30)
    for r in records:
        db_session.add(BehavioralTelemetry(user_id="test_user_ai", **r))
    db_session.commit()
    BaselineEngine.recalculate_user_baseline(db_session, "test_user_ai")

    recovery_data = SyntheticScenarioGenerator.get_recovery_scenario()
    z_scores_recovered = BaselineEngine.compute_feature_z_scores(db_session, "test_user_ai", recovery_data[-1])
    consent = {m: True for m in ["keyboard_enabled", "usage_enabled", "motion_enabled", "work_enabled", "mobility_enabled"]}
    score, stage, _, _, _ = MLRiskEngine.evaluate_risk(z_scores_recovered, 1, consent, "established", 30)
    
    assert stage <= 1

def test_19_20_single_modality_vs_multimodal_anomaly(db_session):
    consent = {m: True for m in ["keyboard_enabled", "usage_enabled", "motion_enabled", "work_enabled", "mobility_enabled"]}
    
    # Isolated single modality anomaly
    z_single = {"screen_time": 4.5, "unlock_count": 4.0}
    score_single, stage_single, _, _, _ = MLRiskEngine.evaluate_risk(z_single, 3, consent, "established", 30)
    
    # Multimodal anomaly
    z_multi = {"screen_time": 3.0, "typing_speed": 3.0, "task_accuracy": 3.0, "stationary_duration": 3.0}
    score_multi, stage_multi, _, _, _ = MLRiskEngine.evaluate_risk(z_multi, 3, consent, "established", 30)

    # Critical requirement: Single modality cannot trigger Stage 4
    assert stage_single < 4
    assert score_multi > score_single
    assert stage_multi > stage_single
