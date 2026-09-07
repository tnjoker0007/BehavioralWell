from typing import Dict, List, Any
from app.schemas.dto import ContributorFactor

INTERVENTION_LIBRARY: Dict[str, Dict[str, Any]] = {
    "breathing": {
        "id": "breathing",
        "title": "2-Minute Breathing Reset",
        "description": "Guided 4-7-8 rhythmic breathing exercise to lower physiological tension and restore calm.",
        "duration": "2 mins",
        "category": "Stress & Tension",
        "icon": "wind"
    },
    "reaction": {
        "id": "reaction",
        "title": "Visual Reaction Speed Test",
        "description": "Interactive visual stimuli test to assess and sharpen cognitive responsiveness.",
        "duration": "1 min",
        "category": "Cognitive Focus",
        "icon": "zap"
    },
    "focus": {
        "id": "focus",
        "title": "Visual Focus Challenge",
        "description": "Grid number tracking exercise designed to restore targeted mental concentration.",
        "duration": "3 mins",
        "category": "Productivity & Clarity",
        "icon": "target"
    },
    "detox": {
        "id": "detox",
        "title": "Digital Detox Pause",
        "description": "Timed phone pause exercise to encourage screen-free restorative rest.",
        "duration": "10 mins",
        "category": "Digital Balance",
        "icon": "smartphone-off"
    },
    "micro_goal": {
        "id": "micro_goal",
        "title": "Daily Micro Goal",
        "description": "Select one achievable micro-action (hydration, short walk, posture reset).",
        "duration": "5 mins",
        "category": "Routine & Wellness",
        "icon": "check-circle"
    },
    "reflection": {
        "id": "reflection",
        "title": "Sleep & Routine Reflection",
        "description": "Brief non-judgmental prompt to reflect on current sleep schedule and daily rhythm.",
        "duration": "4 mins",
        "category": "Routine & Rest",
        "icon": "moon"
    },
    "support_contact": {
        "id": "support_contact",
        "title": "Reach Out to Trusted Support",
        "description": "Encouraging prompt to connect with a trusted friend, colleague, mentor, or professional.",
        "duration": "Flexible",
        "category": "Social & Support Connection",
        "icon": "users"
    }
}

class InterventionEngine:
    @staticmethod
    def recommend_interventions(contributors: List[ContributorFactor], stage: int) -> List[Dict[str, Any]]:
        """
        Recommends tailored non-diagnostic supportive interventions based on behavioral stage and feature contributions.
        """
        recommended_ids = set()

        if stage == 0:
            recommended_ids.add("micro_goal")
            recommended_ids.add("breathing")
        elif stage == 1:
            recommended_ids.add("breathing")
            recommended_ids.add("micro_goal")
            recommended_ids.add("reaction")
        elif stage == 2:
            recommended_ids.add("focus")
            recommended_ids.add("detox")
            recommended_ids.add("reflection")
        elif stage == 3:
            recommended_ids.add("breathing")
            recommended_ids.add("detox")
            recommended_ids.add("support_contact")
        else: # Stage 4
            recommended_ids.add("support_contact")
            recommended_ids.add("reflection")
            recommended_ids.add("breathing")

        # Add feature-specific recommendations
        for c in contributors:
            if c.feature in ["night_usage", "screen_time", "unlock_count"]:
                recommended_ids.add("detox")
                recommended_ids.add("reflection")
            elif c.feature in ["task_accuracy", "task_error_rate", "typing_speed"]:
                recommended_ids.add("focus")
                recommended_ids.add("reaction")
            elif c.feature in ["acceleration_variance", "movement_intensity", "correction_rate"]:
                recommended_ids.add("breathing")
                recommended_ids.add("micro_goal")

        return [INTERVENTION_LIBRARY[iid] for iid in recommended_ids if iid in INTERVENTION_LIBRARY]
