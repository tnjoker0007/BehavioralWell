# BehavioralWell — Intervention Engine & Activity Schema

This document specifies the interactive preventive intervention catalog and completion logging API.

---

## 1. Intervention Catalog Response Schema (`GET /api/interventions`)

```json
{
  "stage": 2,
  "recommendations": [
    {
      "id": "breathing",
      "title": "2-Minute Breathing Reset",
      "description": "Guided 4-7-8 rhythmic breathing technique to lower physiological tension.",
      "duration": "2 mins",
      "category": "Stress & Tension",
      "icon": "wind"
    },
    {
      "id": "focus",
      "title": "Visual Focus Challenge",
      "description": "Grid number tracking exercise designed to restore targeted mental concentration.",
      "duration": "3 mins",
      "category": "Productivity & Clarity",
      "icon": "target"
    }
  ],
  "escalation": {
    "disclaimer": "BehavioralWell provides behavioral pattern analytics, not medical diagnoses.",
    "stage": 2,
    "escalation_recommended": false,
    "resources": [
      {
        "title": "National Crisis & Helpline",
        "phone": "988 (USA/Canada) / 112 (EU/UK)",
        "type": "crisis"
      }
    ]
  }
}
```

---

## 2. Activity Completion Request Schema (`POST /api/interventions/complete`)

```json
{
  "feedback_score": 5,
  "result_metrics": {
    "activity": "breathing",
    "duration_seconds": 120,
    "completed_at": "2026-09-07T14:35:00Z"
  }
}
```
