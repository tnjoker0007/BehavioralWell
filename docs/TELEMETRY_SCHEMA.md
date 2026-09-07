# BehavioralWell — Telemetry & Feature Ingestion Schema

This document details the telemetry JSON schema sent from Android / Web clients to the FastAPI backend.

---

## 1. Single Telemetry Payload Schema

```json
{
  "idempotency_key": "and_evt_1782910293_a8f9",
  "timestamp": "2026-09-07T14:00:00Z",
  "typing_speed": 58.2,
  "key_press_duration": 142.4,
  "pause_duration": 0.42,
  "correction_rate": 4.8,
  "screen_time": 4.5,
  "unlock_count": 48,
  "night_usage": 0.3,
  "app_switch_frequency": 14.0,
  "movement_intensity": 1.02,
  "acceleration_variance": 0.08,
  "stationary_duration": 4.5,
  "task_accuracy": 94.5,
  "task_completion_time": 18.2,
  "task_error_rate": 5.5,
  "speed_variance": 0.12,
  "route_variability": 0.05
}
```

---

## 2. Feature Dictionary

| Feature Field | Modality | Type | Unit / Description |
|---|---|---|---|
| `typing_speed` | Keyboard | `float` | Words per minute (WPM) |
| `key_press_duration` | Keyboard | `float` | Key press dwell time (ms) |
| `pause_duration` | Keyboard | `float` | Inter-key pause duration (sec) |
| `correction_rate` | Keyboard | `float` | Backspace & edit frequency (%) |
| `screen_time` | Usage | `float` | Active screen hours per day |
| `unlock_count` | Usage | `int` | Device unlock count per day |
| `night_usage` | Usage | `float` | Screen hours between 11PM and 6AM |
| `app_switch_frequency` | Usage | `float` | App category switches per hour |
| `movement_intensity` | Motion | `float` | Accelerometer g-force magnitude |
| `acceleration_variance` | Motion | `float` | Motion stability variance |
| `task_accuracy` | Work | `float` | Task completion correctness (%) |
| `task_completion_time` | Work | `float` | Task duration (sec) |
| `speed_variance` | Mobility | `float` | Travel speed consistency metric |
