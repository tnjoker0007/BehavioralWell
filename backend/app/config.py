import os
from dotenv import load_dotenv
load_dotenv()

class Settings:
    PROJECT_NAME: str = "BehavioralWell — AI Multimodal Behavioral Risk Engine"
    API_V1_STR: str = "/api/v1"
    ENVIRONMENT: str = os.getenv("ENVIRONMENT", "development")
    DATABASE_URL: str = os.getenv("DATABASE_URL", "sqlite:///./behavioral_well.db")
    
    # Secret Key Handling with Environment Validation
    SECRET_KEY: str = os.getenv("SECRET_KEY", "behavioral_well_dev_secret_key_2026_change_in_production")
    ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 60 * 24 * 7 # 7 days

    # CORS Configured Origins (No wildcard origins when credentials allowed)
    CORS_ORIGINS: list = [
        "http://localhost:3000",
        "http://127.0.0.1:3000",
        "http://localhost:8000",
        "http://127.0.0.1:8000"
    ]

    # Baseline configuration
    DEFAULT_BASELINE_DAYS: int = 14
    MIN_BASELINE_SAMPLES: int = 5
    TEMPORAL_PERSISTENCE_DAYS: int = 3

    # Models directory
    MODEL_DIR: str = os.path.join(os.path.dirname(__file__), "..", "ml_pipeline", "models")

    # LLM interpretation layer (via OpenRouter)
    OPENROUTER_API_KEY: str = os.getenv("OPENROUTER_API_KEY", "")
    OPENROUTER_MODEL: str = os.getenv("OPENROUTER_MODEL", "anthropic/claude-sonnet-4.6")
    SITE_URL: str = os.getenv("SITE_URL", "")
    SITE_NAME: str = os.getenv("SITE_NAME", "BehavioralWell")

settings = Settings()

if settings.ENVIRONMENT == "production" and not os.getenv("SECRET_KEY"):
    raise RuntimeError("CRITICAL SECURITY RISK: SECRET_KEY environment variable MUST be explicitly set in production mode!")


