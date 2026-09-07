from sqlalchemy import Column, Integer, String, Float, Boolean, DateTime, ForeignKey, Text, JSON
from sqlalchemy.orm import relationship
from datetime import datetime
from app.database import Base

class User(Base):
    __tablename__ = "users"

    id = Column(String, primary_key=True, index=True)
    name = Column(String, nullable=False)
    email = Column(String, unique=True, index=True, nullable=False)
    hashed_password = Column(String, nullable=False)
    age_group = Column(String, default="25-34")
    timezone = Column(String, default="UTC")
    occupation_category = Column(String, default="Technology")
    created_at = Column(DateTime, default=datetime.utcnow)
    baseline_completed = Column(Boolean, default=False)

    consent = relationship("Consent", back_populates="user", uselist=False)
    telemetries = relationship("BehavioralTelemetry", back_populates="user")
    baselines = relationship("UserBaseline", back_populates="user")
    risk_assessments = relationship("RiskAssessment", back_populates="user")
    interventions = relationship("InterventionSession", back_populates="user")
    self_reports = relationship("SelfReport", back_populates="user")

class Consent(Base):
    __tablename__ = "consents"

    id = Column(Integer, primary_key=True, autoincrement=True)
    user_id = Column(String, ForeignKey("users.id"), unique=True, nullable=False)
    keyboard_enabled = Column(Boolean, default=True)
    usage_enabled = Column(Boolean, default=True)
    motion_enabled = Column(Boolean, default=True)
    work_enabled = Column(Boolean, default=True)
    mobility_enabled = Column(Boolean, default=True)
    created_at = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)

    user = relationship("User", back_populates="consent")

class BehavioralTelemetry(Base):
    __tablename__ = "behavioral_telemetry"

    id = Column(Integer, primary_key=True, autoincrement=True)
    user_id = Column(String, ForeignKey("users.id"), nullable=False)
    timestamp = Column(DateTime, default=datetime.utcnow, index=True)
    
    # Derived Metadata Features (Zero raw text/content)
    typing_speed = Column(Float, nullable=True)          # WPM
    key_press_duration = Column(Float, nullable=True)    # ms
    pause_duration = Column(Float, nullable=True)        # sec
    correction_rate = Column(Float, nullable=True)       # % backspace/edits

    screen_time = Column(Float, nullable=True)           # hours/day
    unlock_count = Column(Integer, nullable=True)        # unlocks/day
    night_usage = Column(Float, nullable=True)           # hours between 11PM-6AM
    app_switch_frequency = Column(Float, nullable=True)  # switches/hour

    movement_intensity = Column(Float, nullable=True)    # g-force metric
    acceleration_variance = Column(Float, nullable=True)
    stationary_duration = Column(Float, nullable=True)   # hours

    task_accuracy = Column(Float, nullable=True)         # % correct
    task_completion_time = Column(Float, nullable=True) # seconds
    task_error_rate = Column(Float, nullable=True)       # %

    speed_variance = Column(Float, nullable=True)        # mobility variance
    route_variability = Column(Float, nullable=True)     # metric

    raw_features_json = Column(JSON, nullable=True)

    user = relationship("User", back_populates="telemetries")

class UserBaseline(Base):
    __tablename__ = "user_baselines"

    id = Column(Integer, primary_key=True, autoincrement=True)
    user_id = Column(String, ForeignKey("users.id"), nullable=False)
    feature_name = Column(String, nullable=False)
    mean = Column(Float, nullable=False)
    std = Column(Float, nullable=False)
    min_val = Column(Float, nullable=False)
    max_val = Column(Float, nullable=False)
    samples_count = Column(Integer, default=0)
    updated_at = Column(DateTime, default=datetime.utcnow)

    user = relationship("User", back_populates="baselines")

class RiskAssessment(Base):
    __tablename__ = "risk_assessments"

    id = Column(Integer, primary_key=True, autoincrement=True)
    user_id = Column(String, ForeignKey("users.id"), nullable=False)
    timestamp = Column(DateTime, default=datetime.utcnow, index=True)
    risk_score = Column(Float, nullable=False)           # 0 - 100
    stage = Column(Integer, nullable=False)              # 0 to 4
    stage_label = Column(String, nullable=False)         # Stable, Early, Persistent, Elevated, High Concern
    confidence = Column(Float, default=0.85)
    trend = Column(String, default="stable")             # increasing, decreasing, stable
    persistence_days = Column(Integer, default=1)
    
    top_contributors = Column(JSON, nullable=False)      # list of explainability dicts
    modality_scores = Column(JSON, nullable=False)       # breakdown per modality

    user = relationship("User", back_populates="risk_assessments")

class InterventionSession(Base):
    __tablename__ = "intervention_sessions"

    id = Column(Integer, primary_key=True, autoincrement=True)
    user_id = Column(String, ForeignKey("users.id"), nullable=False)
    activity_type = Column(String, nullable=False)      # breathing, reaction, focus, detox, micro_goal, reflection
    started_at = Column(DateTime, default=datetime.utcnow)
    completed_at = Column(DateTime, nullable=True)
    completed = Column(Boolean, default=False)
    feedback_score = Column(Integer, nullable=True)     # 1-5 rating
    result_metrics = Column(JSON, nullable=True)

    user = relationship("User", back_populates="interventions")

class SelfReport(Base):
    __tablename__ = "self_reports"

    id = Column(Integer, primary_key=True, autoincrement=True)
    user_id = Column(String, ForeignKey("users.id"), nullable=False)
    timestamp = Column(DateTime, default=datetime.utcnow)
    mood = Column(String, nullable=False)              # Good, Okay, Neutral, Low, Stressed
    stress_level = Column(Integer, nullable=False)     # 1 to 5
    note = Column(Text, nullable=True)

    user = relationship("User", back_populates="self_reports")
