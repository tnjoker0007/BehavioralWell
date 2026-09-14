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
    dashboard_routes,
    dev_telemetry_routes,
    device_routes
)
from app.services.auth_service import AuthService
from app.schemas.dto import UserCreate
from app.services.simulation_service import SimulationService
from app.api.telemetry_routes import ingest_single_telemetry

from sqlalchemy import text

# Create database tables & auto-migrate schema
Base.metadata.create_all(bind=engine)

def auto_migrate_db():
    try:
        db = SessionLocal()
        result = db.execute(text("PRAGMA table_info(behavioral_telemetry)")).fetchall()
        cols = [r[1] for r in result]
        if "device_id" not in cols:
            print("[DB MIGRATION] Adding missing 'device_id' column to behavioral_telemetry table...")
            db.execute(text("ALTER TABLE behavioral_telemetry ADD COLUMN device_id VARCHAR"))
            db.commit()
    except Exception as e:
        print(f"[DB MIGRATION NOTICE] {e}")
    finally:
        db.close()

auto_migrate_db()



app = FastAPI(
    title="BehavioralWell — Web + Android Shared Platform",
    description="Privacy-conscious multimodal behavioral risk detection, digital phenotyping, and early intervention API engine.",
    version="1.0.0"
)

# CORS setup with configured trusted origins (no wildcard origins when credentials allowed)
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.CORS_ORIGINS,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Mount DEV page router directly at root
app.include_router(dev_telemetry_routes.page_router)

# Mount API routes under /api/v1 AND /api for Android client backwards-compatibility
for prefix in [settings.API_V1_STR, "/api"]:
    app.include_router(auth_routes.router, prefix=prefix)
    app.include_router(consent_routes.router, prefix=prefix)
    app.include_router(telemetry_routes.router, prefix=prefix)
    app.include_router(risk_routes.router, prefix=prefix)
    app.include_router(intervention_routes.router, prefix=prefix)
    app.include_router(intervention_routes.self_check_router, prefix=prefix)
    app.include_router(consultant_routes.router, prefix=prefix)
    app.include_router(analytics_routes.router, prefix=prefix)
    app.include_router(simulator_routes.router, prefix=prefix)
    app.include_router(baseline_routes.router, prefix=prefix)
    app.include_router(dashboard_routes.router, prefix=prefix)
    app.include_router(dev_telemetry_routes.router, prefix=prefix)
    app.include_router(device_routes.router, prefix=prefix)

@app.on_event("startup")
def seed_demo_data():
    """Seeds default admin, user accounts, and initial telemetry profiles on server start."""
    db = SessionLocal()
    try:
        from app.models.domain import User
        # 1. Admin Account
        admin_user = db.query(User).filter(User.email == "admin@behavioralwell.ai").first()
        if not admin_user:
            AuthService.create_user(
                db,
                UserCreate(
                    name="Dr. Sarah Chen (Admin)",
                    email="admin@behavioralwell.ai",
                    password="AdminPassword123!",
                    role="admin",
                    age_group="35-44",
                    occupation_category="Clinical Psychiatry"
                )
            )

        # 2. Main Demo User
        demo_user = db.query(User).filter(User.email == "demo@behavioralwell.ai").first()
        if not demo_user:
            demo_user = AuthService.create_user(
                db,
                UserCreate(
                    name="Alex Morgan",
                    email="demo@behavioralwell.ai",
                    password="Password123!",
                    role="user",
                    age_group="25-34",
                    occupation_category="Software Engineer"
                )
            )
            normal_telemetry = SimulationService.get_preset_telemetry("normal")
            for _ in range(14):
                ingest_single_telemetry(normal_telemetry, demo_user.id, db)

        # 3. Sample Patient: Sam Rivera (High Stress / Sleep Disruption)
        sam_user = db.query(User).filter(User.email == "sam.rivera@behavioralwell.ai").first()
        if not sam_user:
            sam_user = AuthService.create_user(
                db,
                UserCreate(
                    name="Sam Rivera",
                    email="sam.rivera@behavioralwell.ai",
                    password="Password123!",
                    role="user",
                    age_group="25-34",
                    occupation_category="Software Engineer"
                )
            )
            sleep_telemetry = SimulationService.get_preset_telemetry("sleep_disruption")
            for _ in range(10):
                ingest_single_telemetry(sleep_telemetry, sam_user.id, db)

        # 4. Sample Patient: Jordan Lee (Acute Stress / Cognitive Overload)
        jordan_user = db.query(User).filter(User.email == "jordan.lee@behavioralwell.ai").first()
        if not jordan_user:
            jordan_user = AuthService.create_user(
                db,
                UserCreate(
                    name="Jordan Lee",
                    email="jordan.lee@behavioralwell.ai",
                    password="Password123!",
                    role="user",
                    age_group="35-44",
                    occupation_category="Financial Analyst"
                )
            )
            stress_telemetry = SimulationService.get_preset_telemetry("acute_stress")
            for _ in range(7):
                ingest_single_telemetry(stress_telemetry, jordan_user.id, db)

        # 5. Sample Patient: Taylor Morgan (Recovery Pattern - Fictional Persona)
        taylor_user = db.query(User).filter(User.email == "taylor.morgan@behavioralwell.ai").first()
        if not taylor_user:
            taylor_user = AuthService.create_user(
                db,
                UserCreate(
                    name="Taylor Morgan",
                    email="taylor.morgan@behavioralwell.ai",
                    password="Password123!",
                    role="user",
                    age_group="25-34",
                    occupation_category="Creative Design"
                )
            )
            recovery_telemetry = SimulationService.get_preset_telemetry("recovery")
            for _ in range(12):
                ingest_single_telemetry(recovery_telemetry, taylor_user.id, db)

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
