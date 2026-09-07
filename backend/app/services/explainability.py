from typing import Dict, List, Any, Optional
from app.services.feature_directions import FEATURE_CONFIG, MODALITY_MAP
from app.schemas.dto import ContributorFactor

class ExplainabilityEngine:
    @staticmethod
    def generate_explanations(
        z_scores: Dict[str, float],
        raw_telemetry: Optional[Dict[str, Any]] = None,
        baselines_map: Optional[Dict[str, Any]] = None
    ) -> List[ContributorFactor]:
        """
        Generates structured, non-stigmatizing, non-diagnostic explainability attributions.
        Sorted by absolute concerning Z-score magnitude.
        """
        if not z_scores:
            return []

        # Sort Z-scores by magnitude
        sorted_items = sorted(z_scores.items(), key=lambda item: abs(item[1]), reverse=True)
        contributors: List[ContributorFactor] = []

        for feat, z in sorted_items[:5]:
            if abs(z) < 0.4:
                continue

            config = FEATURE_CONFIG.get(feat, {})
            modality = config.get("modality", "behavioral")
            human_name = config.get("human_name", feat.replace("_", " ").title())
            unit = config.get("unit", "")
            direction_cfg = config.get("direction", "higher_is_deviation")

            # Determine direction label
            if direction_cfg == "lower_is_deviation":
                direction = "decreased" if z > 0 else "increased"
            else:
                direction = "increased" if z > 0 else "decreased"

            current_val = raw_telemetry.get(feat) if raw_telemetry else None
            baseline_val = baselines_map.get(feat).mean if (baselines_map and feat in baselines_map) else None

            # Build non-diagnostic supportive text
            if feat == "night_usage":
                desc = f"Late-night screen activity is {abs(z):.1f} std-dev above your personal baseline routine."
            elif feat == "typing_speed":
                desc = f"Typing rhythm & speed shows a {abs(z):.1f} std-dev shift from your typical baseline."
            elif feat == "correction_rate":
                desc = f"Typing edit & backspace frequency is {abs(z):.1f} std-dev higher than usual."
            elif feat == "screen_time":
                desc = f"Active daily screen time is {abs(z):.1f} std-dev higher than your normal average."
            elif feat == "task_accuracy":
                desc = f"Work challenge performance shows a {abs(z):.1f} std-dev variation from baseline."
            elif feat == "stationary_duration":
                desc = f"Sedentary period duration has shifted by {abs(z):.1f} std-dev from your usual pattern."
            elif feat == "movement_intensity":
                desc = f"Physical movement intensity shows a {abs(z):.1f} std-dev pattern shift."
            else:
                desc = f"{human_name} is {direction} by {abs(z):.1f} std-dev relative to your personal baseline."

            contributors.append(ContributorFactor(
                feature=feat,
                modality=modality,
                direction=direction,
                z_score=round(z, 2),
                human_explanation=desc
            ))

        return contributors
