from typing import Dict, List, Any
from app.services.baseline_engine import MODALITY_MAP
from app.schemas.dto import ContributorFactor

HUMAN_LABELS = {
    "typing_speed": "Typing rhythm & speed",
    "key_press_duration": "Key press dwell time",
    "pause_duration": "Inter-key pause duration",
    "correction_rate": "Typing edit & correction frequency",
    "screen_time": "Daily active screen time",
    "unlock_count": "Phone unlock frequency",
    "night_usage": "Late-night screen activity",
    "app_switch_frequency": "App switching rate",
    "movement_intensity": "Physical movement intensity",
    "acceleration_variance": "Motion stability",
    "stationary_duration": "Sedentary period duration",
    "task_accuracy": "Work task accuracy",
    "task_completion_time": "Task completion duration",
    "task_error_rate": "Task error rate",
    "speed_variance": "Mobility speed consistency",
    "route_variability": "Travel route variation"
}

class ExplainabilityEngine:
    @staticmethod
    def generate_explanations(z_scores: Dict[str, float]) -> List[ContributorFactor]:
        """
        Sorts Z-scores by absolute magnitude and converts into non-stigmatizing natural language explanations.
        """
        if not z_scores:
            return []

        sorted_items = sorted(z_scores.items(), key=lambda item: abs(item[1]), reverse=True)
        contributors = []

        for feat, z in sorted_items[:5]:
            if abs(z) < 0.5:
                continue

            direction = "elevated" if z > 0 else "reduced"
            modality = MODALITY_MAP.get(feat, "behavioral")
            human_name = HUMAN_LABELS.get(feat, feat.replace("_", " ").title())

            if feat == "typing_speed":
                desc = f"Typing speed is {abs(z):.1f} std-dev {'slower' if z < 0 else 'faster'} than your baseline."
            elif feat == "correction_rate":
                desc = f"Backspace & correction rate is {abs(z):.1f} std-dev higher than your usual pattern."
            elif feat == "night_usage":
                desc = f"Late-night activity increased by {abs(z):.1f} std-dev relative to your normal routine."
            elif feat == "task_accuracy":
                desc = f"Work performance accuracy shows a {abs(z):.1f} std-dev deviation from baseline."
            elif feat == "acceleration_variance":
                desc = f"Physical motion patterns show a {abs(z):.1f} std-dev variance shift."
            else:
                desc = f"{human_name} is {direction} by {abs(z):.1f} std-dev compared to your personal baseline."

            contributors.append(ContributorFactor(
                feature=feat,
                modality=modality,
                direction=direction,
                z_score=round(z, 2),
                human_explanation=desc
            ))

        return contributors
