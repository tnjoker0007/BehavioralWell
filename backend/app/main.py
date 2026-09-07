from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.config import settings
from app.database import engine, Base, SessionLocal
from app.api import (
    auth_routes,
    consent_routes,
    telemetry_routes,
    risk_routes,
    intervention_routes,
    consultant_routes,
    analytics_routes,
    simulator_routes,
    baseline_routes,
    dashboard_routes
)
from app.services.auth_service import AuthService
from app.schemas.dto import UserCreate
from app.services.simulation_service import SimulationService
from app.api.telemetry_routes import ingest_single_telemetry

# Create database tables
Base.metadata.create_all(bind=engine)

app = FastAPI(
    title="BehavioralWell — Web + Android Shared Platform",
    description="Privacy-conscious multimodal behavioral risk detection, digital phenotyping, and early intervention API engine.",
    version="1.0.0"
)

# CORS setup
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Mount API routes under /api/v1 AND /api for Android client backwards-compatibility
for prefix in [settings.API_V1_STR, "/api"]:
    app.include_router(auth_routes.router, prefix=prefix)
    app.include_router(consent_routes.router, prefix=prefix)
    app.include_router(telemetry_routes.router, prefix=prefix)
    app.include_router(risk_routes.router, prefix=prefix)
    app.include_router(intervention_routes.router, prefix=prefix)
    app.include_router(consultant_routes.router, prefix=prefix)
    app.include_router(analytics_routes.router, prefix=prefix)
    app.include_router(simulator_routes.router, prefix=prefix)
    app.include_router(baseline_routes.router, prefix=prefix)
    app.include_router(dashboard_routes.router, prefix=prefix)

@app.on_event("startup")
def seed_demo_data():
    """Seeds a default demo user and baseline telemetry on server start."""
    db = SessionLocal()
    try:
        from app.models.domain import User
        demo_user = db.query(User).filter(User.email == "demo@behavioralwell.ai").first()
        if not demo_user:
            demo_user = AuthService.create_user(
                db,
                UserCreate(
                    name="Alex Morgan",
                    email="demo@behavioralwell.ai",
                    password="Password123!",
                    age_group="25-34",
                    occupation_category="Software Engineer"
                )
            )
            # Seed 14 days of baseline "normal" telemetry
            normal_telemetry = SimulationService.get_preset_telemetry("normal")
            for _ in range(14):
                ingest_single_telemetry(normal_telemetry, demo_user.id, db)
    except Exception as e:
        print(f"Seed info: {e}")
    finally:
        db.close()

@app.get("/")
def root():
    return {
        "status": "online",
        "service": settings.PROJECT_NAME,
        "docs_url": "/docs",
        "openapi_url": "/openapi.json"
    }
