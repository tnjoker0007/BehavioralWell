import os

class Settings:
    PROJECT_NAME: str = "BehavioralWell — AI Multimodal Behavioral Risk Engine"
    API_V1_STR: str = "/api/v1"
    DATABASE_URL: str = os.getenv("DATABASE_URL", "sqlite:///./behavioral_well.db")
    SECRET_KEY: str = os.getenv("SECRET_KEY", "behavioral_well_super_secret_key_2026")
    ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 60 * 24 * 7 # 7 days

    # Baseline configuration
    DEFAULT_BASELINE_DAYS: int = 14
    MIN_BASELINE_SAMPLES: int = 5
    TEMPORAL_PERSISTENCE_DAYS: int = 3

    # Models directory
    MODEL_DIR: str = os.path.join(os.path.dirname(__file__), "..", "ml_pipeline", "models")

settings = Settings()
