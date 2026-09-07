from pydantic import BaseModel, EmailStr
from typing import Optional, List, Dict, Any
from datetime import datetime

# --- Auth & User Identity ---
class UserCreate(BaseModel):
    name: str
    email: EmailStr
    password: str
    age_group: Optional[str] = "25-34"
    occupation_category: Optional[str] = "Technology"

class UserLogin(BaseModel):
    email: EmailStr
    password: str

class TokenRefreshRequest(BaseModel):
    refresh_token: str

class UserResponse(BaseModel):
    id: str
    name: str
    email: str
    age_group: str
    timezone: str
    occupation_category: str
    created_at: datetime
    baseline_completed: bool

    class Config:
        from_attributes = True

class TokenResponse(BaseModel):
    access_token: str
    refresh_token: str
    token_type: str = "bearer"
    user: UserResponse

# --- Consent Schemas ---
class ConsentUpdate(BaseModel):
    keyboard_enabled: bool
    usage_enabled: bool
    motion_enabled: bool
    work_enabled: bool
    mobility_enabled: bool

class ConsentResponse(ConsentUpdate):
    user_id: str
    updated_at: datetime

    class Config:
        from_attributes = True

# --- Telemetry & Batch Ingestion ---
class TelemetryInput(BaseModel):
    idempotency_key: Optional[str] = None
    timestamp: Optional[datetime] = None
    
    typing_speed: Optional[float] = None
    key_press_duration: Optional[float] = None
    pause_duration: Optional[float] = None
    correction_rate: Optional[float] = None

    screen_time: Optional[float] = None
    unlock_count: Optional[int] = None
    night_usage: Optional[float] = None
    app_switch_frequency: Optional[float] = None

    movement_intensity: Optional[float] = None
    acceleration_variance: Optional[float] = None
    stationary_duration: Optional[float] = None

    task_accuracy: Optional[float] = None
    task_completion_time: Optional[float] = None
    task_error_rate: Optional[float] = None

    speed_variance: Optional[float] = None
    route_variability: Optional[float] = None

class BatchTelemetryInput(BaseModel):
    batch: List[TelemetryInput]

class TelemetryStatusResponse(BaseModel):
    total_records: int
    last_telemetry_at: Optional[datetime]
    modalities_collected: List[str]

# --- Baseline ---
class BaselineItem(BaseModel):
    feature_name: str
    mean: float
    std: float
    min_val: float
    max_val: float
    samples_count: int

class BaselineResponse(BaseModel):
    user_id: str
    baseline_completed: bool
    completion_percent: float
    features: List[BaselineItem]

class BaselineProgressResponse(BaseModel):
    baseline_status: str          # "learning", "established"
    completion_percent: float     # 0 to 100
    days_collected: int
    days_required: int
    modalities_status: Dict[str, str]

# --- Risk & Staging ---
class ContributorFactor(BaseModel):
    feature: str
    modality: str
    direction: str          # "elevated" or "reduced"
    z_score: float
    human_explanation: str

class ModalityBreakdown(BaseModel):
    keyboard: float
    usage: float
    motion: float
    work: float
    mobility: float

class RiskAssessmentResponse(BaseModel):
    risk_score: float
    stage: int
    stage_label: str
    confidence: float
    trend: str
    persistence_days: int
    top_contributors: List[ContributorFactor]
    modality_scores: ModalityBreakdown
    timestamp: datetime

class RiskHistoryResponse(BaseModel):
    history: List[RiskAssessmentResponse]

# --- Interventions & Self Check ---
class SelfReportInput(BaseModel):
    mood: str
    stress_level: int
    note: Optional[str] = None

class SelfReportResponse(BaseModel):
    id: int
    user_id: str
    mood: str
    stress_level: int
    note: Optional[str]
    timestamp: datetime

class InterventionStartInput(BaseModel):
    activity_type: str

class InterventionCompleteInput(BaseModel):
    feedback_score: Optional[int] = None
    result_metrics: Optional[Dict[str, Any]] = None

class SimulationPresetRequest(BaseModel):
    preset_name: str # "normal", "sleep_disruption", "acute_stress", "mild_fatigue", "recovery"

# --- Unified Mobile/Web Dashboard Schema ---
class DashboardResponse(BaseModel):
    user: UserResponse
    consent: ConsentResponse
    baseline_progress: BaselineProgressResponse
    current_risk: RiskAssessmentResponse
    recommended_interventions: List[Dict[str, Any]]
    last_self_report: Optional[SelfReportResponse]
