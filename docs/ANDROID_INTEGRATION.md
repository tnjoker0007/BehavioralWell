# BehavioralWell — Android Native Integration Guide

This document is the official handoff guide for developing the **BehavioralWell Native Android Application** (Kotlin + Jetpack Compose) to connect directly to the shared FastAPI backend.

---

## 1. System Architecture & Single Source of Truth

```text
                                 BEHAVIORALWELL BACKEND
                                     (FastAPI)
                                         │
                        ┌────────────────┴────────────────┐
                        │                                 │
                   REST / HTTPS                      REST / HTTPS
                        │                                 │
                 ANDROID CLIENT                      WEB CLIENT
              (Jetpack Compose)                    (React + Vite)
```

- **Backend Base URL (Development):**
  - Android Emulator access to host machine: `http://10.0.2.2:8000/api`
  - Physical Device / LAN access: `http://<YOUR_LOCAL_IP>:8000/api`
- **Single Source of Truth:**
  - Both Android and Web share the exact same user database, JWT tokens, baseline statistics, ML risk scores, risk stages (Stage 0 to Stage 4), and intervention history.
  - Android is the **device sensor telemetry client**.
  - Web is the **dashboard & research client**.

---

## 2. Shared Authentication Flow

1. User registers/logins via `POST /api/auth/register` or `POST /api/auth/login`.
2. Backend returns `access_token` (JWT, 7 days) and `refresh_token` (JWT, 30 days) along with `user` profile.
3. Android app stores tokens in **EncryptedSharedPreferences** or **Android DataStore**.
4. Include `Authorization: Bearer <access_token>` header on all subsequent requests.
5. Account created on Android immediately enables logging into Web with identical credentials.

---

## 3. Endpoints Cheat Sheet for Android

| Feature | HTTP Method | Endpoint | Description |
|---|---|---|---|
| Register | `POST` | `/api/auth/register` | Register new account |
| Login | `POST` | `/api/auth/login` | Log into existing account |
| Refresh Token | `POST` | `/api/auth/refresh` | Obtain new access token |
| User Profile | `GET` | `/api/auth/me` | Fetch authenticated user object |
| Get Consent | `GET` | `/api/consent` | Fetch privacy permission toggles |
| Update Consent | `PUT` | `/api/consent` | Update privacy permissions |
| Send Telemetry | `POST` | `/api/telemetry` | Send real-time derived telemetry |
| Batch Telemetry | `POST` | `/api/telemetry/batch` | Upload offline-queued telemetry batch |
| Telemetry Status | `GET` | `/api/telemetry/status` | Query total records & collected modalities |
| Baseline | `GET` | `/api/baseline` | Fetch per-feature baseline mean/std Z-scores |
| Baseline Progress | `GET` | `/api/baseline/progress` | Check baseline learning progress (0-100%) |
| Current Risk | `GET` | `/api/risk/current` | Fetch latest risk score, stage, trend, & attribution |
| Risk History | `GET` | `/api/risk/history` | Fetch historical risk assessments |
| Risk Explanation | `GET` | `/api/risk/explanation` | Fetch "What Changed?" feature factors |
| Interventions | `GET` | `/api/interventions` | Get recommended activities & crisis resources |
| Start Intervention | `POST` | `/api/interventions/start` | Log activity start timestamp |
| Complete Activity | `POST` | `/api/interventions/complete` | Submit activity completion metrics |
| Self Check-in | `POST` | `/api/self-check` | Submit mood & stress check-in |
| Unified Dashboard | `GET` | `/api/dashboard` | Fetch single-payload aggregated dashboard state |

---

## 4. Privacy & Telemetry Ingestion Contract

> [!IMPORTANT]
> The Android application MUST NEVER upload raw keystroke text, typed messages, passwords, or precise GPS track logs.
> Upload ONLY derived behavioral metadata:
> - `typing_speed` (WPM)
> - `key_press_duration` (dwell time ms)
> - `pause_duration` (inter-key delay sec)
> - `correction_rate` (backspace % frequency)
> - `screen_time` (active screen hours)
> - `unlock_count` (unlocks per day)
> - `night_usage` (screen hours 11PM–6AM)
> - `movement_intensity` (accelerometer g-force metric)
> - `acceleration_variance` (motion variance)

---

## 5. Offline-First Queueing & Batch Upload

When internet is unavailable:
1. Android app persists telemetry records locally using **Room Database**.
2. Attach a unique `idempotency_key` (e.g. `and_evt_<timestamp>_<uuid>`) to each record.
3. When network becomes available, upload batch via `POST /api/telemetry/batch`:

```json
{
  "batch": [
    {
      "idempotency_key": "and_evt_1782910293_a8f9",
      "timestamp": "2026-09-07T14:00:00Z",
      "typing_speed": 48.5,
      "key_press_duration": 142.0,
      "screen_time": 6.2,
      "night_usage": 1.8
    }
  ]
}
```

The backend guarantees idempotency deduplication so retried batch uploads never corrupt baseline statistics.
