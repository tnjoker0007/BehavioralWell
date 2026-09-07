from typing import Dict, List, Any
from app.schemas.dto import ContributorFactor

INTERVENTION_LIBRARY = {
    "breathing": {
        "id": "breathing",
        "title": "2-Minute Breathing Reset",
        "description": "Guided 4-7-8 rhythmic breathing technique to lower physiological tension.",
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
        "title": "Digital Detox Break",
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
    }
}

class InterventionEngine:
    @staticmethod
    def recommend_interventions(contributors: List[ContributorFactor], stage: int) -> List[Dict[str, Any]]:
        """
        Recommends tailored interactive activities based on detected behavioral deviations and stage.
        """
        recommended_ids = set()

        if stage == 0:
            recommended_ids.add("micro_goal")
            recommended_ids.add("breathing")
        elif stage >= 1:
            for c in contributors:
                if c.feature in ["night_usage", "screen_time", "unlock_count"]:
                    recommended_ids.add("detox")
                    recommended_ids.add("breathing")
                elif c.feature in ["task_accuracy", "task_error_rate", "typing_speed"]:
                    recommended_ids.add("focus")
                    recommended_ids.add("reaction")
                elif c.feature in ["acceleration_variance", "movement_intensity", "correction_rate"]:
                    recommended_ids.add("breathing")
                    recommended_ids.add("micro_goal")

        if not recommended_ids:
            recommended_ids = {"breathing", "focus"}

        return [INTERVENTION_LIBRARY[iid] for iid in recommended_ids if iid in INTERVENTION_LIBRARY]
