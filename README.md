# BehavioralWell — AI Multimodal Behavioral Risk Detection & Early Intervention Platform

![BehavioralWell Banner](docs/assets/banner.png)

> **Privacy-First Digital Phenotyping, Time-Series Baseline Fingerprinting & Preventive Interventions**

BehavioralWell is a full-stack, cross-platform AI solution for early behavioral risk detection and preventive micro-interventions. Rather than relying on clinical diagnoses, BehavioralWell learns each user's unique **Personal Behavioral Fingerprint** across 5 privacy-preserving digital modalities, measures continuous Z-score deviations over time, detects multi-day temporal persistence, and routes tailored interactive micro-apps before escalation occurs.

---

## 🌟 Core Differentiators

* **Privacy-Conscious Derived Telemetry**: Raw sensor signals (keystroke timings, touch dynamics, sensor rates) are converted into privacy-safe mathematical features strictly on-device. Zero raw text or location raw data is uploaded.
* **Personal Behavioral Fingerprint**: 14–30 day sliding window calculation establishing baseline means and standard deviations ($\mu, \sigma$) per user.
* **Multimodal Data Fusion**: Analyzes 5 distinct modalities:
  1. ⌨️ **Keystroke Dynamics** (Flight time, dwell time, error/backspace rate)
  2. 📱 **Screen & Touch Dynamics** (Tap velocity, swipe acceleration, scroll jitter)
  3. 🌙 **Circadian & Usage Rhythms** (Late-night activity, screen-on duration, session frequency)
  4. 🚶 **Mobility & Activity** (Step cadence, still-ratio, radius of gyration)
  5. 🔋 **Device State & Battery** (Battery drain rate, charging posture, night charge state)
* **5-Stage Risk Classification**: Smooth risk progression mapping across:
  - **Stage 1**: Baseline Normal
  - **Stage 2**: Mild Deviation (Subtle pattern shift)
  - **Stage 3**: Moderate Deviation (Multi-modality shift)
  - **Stage 4**: High Risk / Escalation (Persistent multi-day deviation)
  - **Stage 5**: Severe Crisis (Immediate 988/112 support routing)
* **Hybrid Risk Classifier**: Random Forest Classifier ensemble with deterministic fallback to multi-modality Z-score thresholding.
* **Explainable AI Cards**: Transparent "What Changed?" cards providing clear non-judgmental feedback.
* **Preventive Micro-Apps**: Interactive reset modules including 4-7-8 Box Breathing, Visual Focus Grids, Reaction Challenges, Micro-Goal Trackers, and Digital Detox Prompts.

---

## 🏗️ Platform Architecture

```text
                        BehavioralWell Architecture
                                  
     ┌────────────────────────┐              ┌────────────────────────┐
     │   Native Android App   │              │   React / Vite Web     │
     │  (Sensors, Room, KSP)  │              │  (Dashboard, Sim, UI)  │
     └───────────┬────────────┘              └───────────┬────────────┘
                 │                                       │
                 └───────────────────┬───────────────────┘
                                     │ REST / HTTPS (JWT)
                                     ▼
                        ┌────────────────────────┐
                        │    FastAPI Backend     │
                        ├────────────────────────┤
                        │ • Auth & Consent Hub   │
                        │ • Baseline Z-Score Engine│
                        │ • ML Risk Classifier   │
                        │ • Intervention Router  │
                        │ • Consultant Triaging  │
                        └────────────────────────┘
```

---

## 📁 Repository Structure

```text
behavioral_well/
├── android/            # Native Android App (Jetpack Compose, Room DB, Retrofit2, Sensors)
│   ├── app/            # Main application module
│   ├── build.gradle.kts# Root build configuration
│   └── gradle/         # Gradle version catalog & wrapper
├── backend/            # FastAPI Python Backend & ML Engine
│   ├── app/            # FastAPI routes, models, ML pipeline, baseline engine
│   ├── data/           # Synthetic dataset generators & ML model artifacts (.joblib)
│   ├── tests/          # pytest unit & integration test suites
│   ├── main.py         # Application entry point
│   └── requirements.txt# Backend dependencies
├── docs/               # System architecture & integration documentation
│   ├── ANDROID_INTEGRATION.md
│   ├── API.md
│   ├── API_OPENAPI.json
│   ├── AUTH_FLOW.md
│   ├── DESIGN_TOKENS.md
│   ├── INTERVENTION_SCHEMA.md
│   ├── RISK_MODEL_SCHEMA.md
│   └── TELEMETRY_SCHEMA.md
└── frontend/           # React + Vite Web Dashboard & Simulator
    ├── src/            # Components, Views, Contexts, Hooks, Interventions
    ├── index.html
    └── package.json
```

---

## ⚡ Quickstart Guide

### 1. Run the FastAPI Backend
```bash
cd backend
python -m venv venv
# On Windows:
venv\Scripts\activate
# On Linux/macOS:
source venv/bin/activate

pip install -r requirements.txt
python run_backend.py
```
*API running at:* `http://127.0.0.1:8000`  
*Swagger Docs:* `http://127.0.0.1:8000/docs`

### 2. Run the React Web Dashboard & Simulator
```bash
cd frontend
npm install
npm run dev
```
*Web App running at:* `http://localhost:5173`

### 3. Build & Run the Android Application
Open the `android/` directory in **Android Studio Jellyfish (or newer)** and execute:
```bash
cd android
.\gradlew.bat :app:assembleDebug
```

---

## 🔒 Security & Privacy Model

- **On-Device Feature Extraction**: Raw sensor data is converted into statistical summaries before transmission.
- **Explicit Granular Consent Hub**: Users opt-in or opt-out of individual modalities at any time.
- **Token-Based Authentication**: Secure JWT access & refresh tokens with bcrypt password hashing.

---

## 📄 License

Distributed under the MIT License. See `LICENSE` for more information.
