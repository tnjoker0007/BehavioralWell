# BehavioralWell — Complete API Reference Specification

The BehavioralWell backend provides a RESTful API powering both Web and Native Android clients.

Base URL: `http://localhost:8000/api` (or `http://10.0.2.2:8000/api` on Android Emulator).

---

## 1. Authentication Endpoints

### `POST /api/auth/register`
- **Request Body:** `{ "name": "...", "email": "...", "password": "...", "age_group": "25-34", "occupation_category": "Technology" }`
- **Response:** `{ "access_token": "...", "refresh_token": "...", "token_type": "bearer", "user": { ... } }`

### `POST /api/auth/login`
- **Request Body:** `{ "email": "...", "password": "..." }`
- **Response:** `{ "access_token": "...", "refresh_token": "...", "token_type": "bearer", "user": { ... } }`

### `POST /api/auth/refresh`
- **Request Body:** `{ "refresh_token": "..." }`
- **Response:** `{ "access_token": "...", "refresh_token": "...", "user": { ... } }`

### `GET /api/auth/me`
- **Headers:** `Authorization: Bearer <access_token>`
- **Response:** User object with ID, name, email, age group, timezone, occupation, baseline status.

### `POST /api/auth/logout`
- **Response:** `{ "message": "Successfully logged out." }`

---

## 2. Consent & Privacy Endpoints

### `GET /api/consent`
- **Response:** `{ "keyboard_enabled": true, "usage_enabled": true, "motion_enabled": true, "work_enabled": true, "mobility_enabled": true }`

### `PUT /api/consent`
- **Request Body:** `{ "keyboard_enabled": true, ... }`
- **Response:** Updated consent object.

---

## 3. Behavioral Telemetry Endpoints

### `POST /api/telemetry`
- **Request Body:** Single `TelemetryInput` object.
- **Response:** `RiskAssessmentResponse`

### `POST /api/telemetry/batch`
- **Request Body:** `{ "batch": [ { "idempotency_key": "...", "timestamp": "...", ... } ] }`
- **Response:** `{ "status": "success", "processed_count": N, "skipped_duplicates": N, "latest_risk": { ... } }`

### `GET /api/telemetry/status`
- **Response:** `{ "total_records": N, "last_telemetry_at": "...", "modalities_collected": [...] }`

---

## 4. Personal Baseline Endpoints

### `GET /api/baseline`
- **Response:** Baseline features array with per-feature mean, std, min, max, samples_count.

### `GET /api/baseline/progress`
- **Response:** `{ "baseline_status": "established", "completion_percent": 100.0, "days_collected": 14, "days_required": 14, "modalities_status": { ... } }`

---

## 5. Risk Staging & Engine Endpoints

### `GET /api/risk/current`
- **Response:** Current risk score, stage, label, trend, persistence, modality scores, and top factors.

### `GET /api/risk/history`
- **Response:** List of historical risk assessment snapshots.

### `GET /api/risk/explanation`
- **Response:** Plain-language feature attributions ("What Changed?").

---

## 6. Interventions & Check-in Endpoints

### `GET /api/interventions`
- **Response:** Tailored activity recommendations & regional crisis resources.

### `POST /api/interventions/start`
- **Request Body:** `{ "activity_type": "breathing" }`
- **Response:** `{ "session_id": 12, "status": "started" }`

### `POST /api/interventions/complete`
- **Request Body:** `{ "feedback_score": 5, "result_metrics": { ... } }`
- **Response:** `{ "message": "Intervention session marked complete" }`

### `POST /api/self-check`
- **Request Body:** `{ "mood": "Okay", "stress_level": 2, "note": "..." }`
- **Response:** `{ "message": "Self-check recorded successfully" }`

---

## 7. Unified Dashboard Endpoint

### `GET /api/dashboard`
- **Response:** Aggregated payload containing user profile, consent, baseline progress, current risk, recommendations, and last self check-in.
