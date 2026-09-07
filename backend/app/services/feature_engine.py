from typing import Dict, Any
from app.schemas.dto import TelemetryInput, ConsentResponse

class FeatureEngine:
    @staticmethod
    def sanitize_and_filter_telemetry(telemetry_in: TelemetryInput, consent: ConsentResponse) -> Dict[str, Any]:
        """Applies privacy consent filters and extracts derived features only."""
        features = {}

        if consent.keyboard_enabled:
            if telemetry_in.typing_speed is not None: features["typing_speed"] = float(telemetry_in.typing_speed)
            if telemetry_in.key_press_duration is not None: features["key_press_duration"] = float(telemetry_in.key_press_duration)
            if telemetry_in.pause_duration is not None: features["pause_duration"] = float(telemetry_in.pause_duration)
            if telemetry_in.correction_rate is not None: features["correction_rate"] = float(telemetry_in.correction_rate)

        if consent.usage_enabled:
            if telemetry_in.screen_time is not None: features["screen_time"] = float(telemetry_in.screen_time)
            if telemetry_in.unlock_count is not None: features["unlock_count"] = int(telemetry_in.unlock_count)
            if telemetry_in.night_usage is not None: features["night_usage"] = float(telemetry_in.night_usage)
            if telemetry_in.app_switch_frequency is not None: features["app_switch_frequency"] = float(telemetry_in.app_switch_frequency)

        if consent.motion_enabled:
            if telemetry_in.movement_intensity is not None: features["movement_intensity"] = float(telemetry_in.movement_intensity)
            if telemetry_in.acceleration_variance is not None: features["acceleration_variance"] = float(telemetry_in.acceleration_variance)
            if telemetry_in.stationary_duration is not None: features["stationary_duration"] = float(telemetry_in.stationary_duration)

        if consent.work_enabled:
            if telemetry_in.task_accuracy is not None: features["task_accuracy"] = float(telemetry_in.task_accuracy)
            if telemetry_in.task_completion_time is not None: features["task_completion_time"] = float(telemetry_in.task_completion_time)
            if telemetry_in.task_error_rate is not None: features["task_error_rate"] = float(telemetry_in.task_error_rate)

        if consent.mobility_enabled:
            if telemetry_in.speed_variance is not None: features["speed_variance"] = float(telemetry_in.speed_variance)
            if telemetry_in.route_variability is not None: features["route_variability"] = float(telemetry_in.route_variability)

        return features
