# BehavioralWell — Risk Staging & Explainability Model Schema

This document specifies the risk evaluation response returned by `GET /api/risk/current` and `GET /api/dashboard`.

---

## 1. Risk Evaluation JSON Schema

```json
{
  "risk_score": 68.5,
  "stage": 2,
  "stage_label": "Stage 2 — Persistent Deviation",
  "confidence": 0.92,
  "trend": "increasing",
  "persistence_days": 3,
  "modality_scores": {
    "keyboard": 62.0,
    "usage": 78.0,
    "motion": 45.0,
    "work": 70.0,
    "mobility": 20.0
  },
  "top_contributors": [
    {
      "feature": "night_usage",
      "modality": "usage",
      "direction": "elevated",
      "z_score": 2.8,
      "human_explanation": "Late-night screen activity increased by 2.8 std-dev relative to personal baseline."
    },
    {
      "feature": "task_accuracy",
      "modality": "work",
      "direction": "reduced",
      "z_score": -2.1,
      "human_explanation": "Work performance accuracy shows a 2.1 std-dev deviation from baseline."
    }
  ],
  "timestamp": "2026-09-07T14:30:00Z"
}
```

---

## 2. Risk Stage Hierarchy

- **Stage 0 (`Stage 0 — Stable`):** Deviation score 0 to 24. Behavior aligns with personal baseline. No action required.
- **Stage 1 (`Stage 1 — Early Deviation`):** Deviation score 25 to 48. Mild behavioral changes detected. Gentle wellness recommendation.
- **Stage 2 (`Stage 2 — Persistent Deviation`):** Deviation score 49 to 71 (persisting over 2+ days). Multi-modal changes detected. Self-check & preventive reset recommended.
- **Stage 3 (`Stage 3 — Elevated Risk`):** Deviation score 72 to 87. Multiple behavioral signals significantly differ from baseline over 3+ days. Encourage self-assessment & professional counseling options.
- **Stage 4 (`Stage 4 — High Concern`):** Deviation score 88 to 100. Strong, persistent multi-modal deviations. Display regional crisis resources (988 / 112) and offer shareable consultant report.
