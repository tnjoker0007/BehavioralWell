from typing import Dict, Any
from app.schemas.dto import TelemetryInput

PRESETS: Dict[str, Dict[str, Any]] = {
    "normal": {
        "typing_speed": 62.0,
        "key_press_duration": 115.0,
        "pause_duration": 0.35,
        "correction_rate": 4.2,
        "screen_time": 4.1,
        "unlock_count": 42,
        "night_usage": 0.2,
        "app_switch_frequency": 12.0,
        "movement_intensity": 1.02,
        "acceleration_variance": 0.08,
        "stationary_duration": 4.5,
        "task_accuracy": 94.5,
        "task_completion_time": 18.2,
        "task_error_rate": 5.5,
        "speed_variance": 0.12,
        "route_variability": 0.05
    },
    "mild_fatigue": {
        "typing_speed": 51.0,
        "key_press_duration": 140.0,
        "pause_duration": 0.58,
        "correction_rate": 9.5,
        "screen_time": 6.4,
        "unlock_count": 68,
        "night_usage": 1.4,
        "app_switch_frequency": 22.0,
        "movement_intensity": 0.88,
        "acceleration_variance": 0.18,
        "stationary_duration": 6.2,
        "task_accuracy": 85.0,
        "task_completion_time": 24.5,
        "task_error_rate": 15.0,
        "speed_variance": 0.25,
        "route_variability": 0.15
    },
    "acute_stress": {
        "typing_speed": 37.5,
        "key_press_duration": 185.0,
        "pause_duration": 0.92,
        "correction_rate": 18.4,
        "screen_time": 8.8,
        "unlock_count": 98,
        "night_usage": 3.2,
        "app_switch_frequency": 38.0,
        "movement_intensity": 0.65,
        "acceleration_variance": 0.45,
        "stationary_duration": 8.5,
        "task_accuracy": 71.0,
        "task_completion_time": 35.0,
        "task_error_rate": 29.0,
        "speed_variance": 0.55,
        "route_variability": 0.42
    },
    "recovery": {
        "typing_speed": 60.0,
        "key_press_duration": 120.0,
        "pause_duration": 0.38,
        "correction_rate": 4.8,
        "screen_time": 4.3,
        "unlock_count": 45,
        "night_usage": 0.3,
        "app_switch_frequency": 14.0,
        "movement_intensity": 1.05,
        "acceleration_variance": 0.09,
        "stationary_duration": 4.8,
        "task_accuracy": 93.0,
        "task_completion_time": 19.0,
        "task_error_rate": 7.0,
        "speed_variance": 0.14,
        "route_variability": 0.06
    }
}

class SimulationService:
    @staticmethod
    def get_preset_telemetry(preset_name: str) -> TelemetryInput:
        preset_data = PRESETS.get(preset_name.lower(), PRESETS["normal"])
        return TelemetryInput(**preset_data)
