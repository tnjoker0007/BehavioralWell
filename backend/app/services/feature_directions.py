from typing import Dict, Any

FEATURE_CONFIG: Dict[str, Dict[str, Any]] = {
    # Keyboard Dynamics
    "typing_speed": {
        "modality": "keyboard",
        "direction": "lower_is_deviation",
        "weight": 1.2,
        "human_name": "Typing speed & rhythm",
        "unit": "WPM"
    },
    "key_press_duration": {
        "modality": "keyboard",
        "direction": "higher_is_deviation",
        "weight": 1.0,
        "human_name": "Key press dwell time",
        "unit": "ms"
    },
    "pause_duration": {
        "modality": "keyboard",
        "direction": "higher_is_deviation",
        "weight": 1.1,
        "human_name": "Inter-key pause duration",
        "unit": "sec"
    },
    "correction_rate": {
        "modality": "keyboard",
        "direction": "higher_is_deviation",
        "weight": 1.3,
        "human_name": "Typing edit & correction frequency",
        "unit": "ratio"
    },

    # Device & App Usage
    "screen_time": {
        "modality": "usage",
        "direction": "higher_is_deviation",
        "weight": 1.2,
        "human_name": "Daily active screen time",
        "unit": "hours"
    },
    "unlock_count": {
        "modality": "usage",
        "direction": "higher_is_deviation",
        "weight": 1.0,
        "human_name": "Phone unlock frequency",
        "unit": "unlocks"
    },
    "night_usage": {
        "modality": "usage",
        "direction": "higher_is_deviation",
        "weight": 1.5,
        "human_name": "Late-night screen activity",
        "unit": "hours"
    },
    "app_switch_frequency": {
        "modality": "usage",
        "direction": "higher_is_deviation",
        "weight": 1.1,
        "human_name": "App switching frequency",
        "unit": "switches/hr"
    },

    # Physical Motion
    "movement_intensity": {
        "modality": "motion",
        "direction": "bidirectional",
        "weight": 1.0,
        "human_name": "Physical movement intensity",
        "unit": "g-force"
    },
    "acceleration_variance": {
        "modality": "motion",
        "direction": "higher_is_deviation",
        "weight": 1.1,
        "human_name": "Motion instability & variance",
        "unit": "variance"
    },
    "stationary_duration": {
        "modality": "motion",
        "direction": "higher_is_deviation",
        "weight": 1.2,
        "human_name": "Sedentary period duration",
        "unit": "hours"
    },

    # Work & Productivity
    "task_accuracy": {
        "modality": "work",
        "direction": "lower_is_deviation",
        "weight": 1.4,
        "human_name": "Work task accuracy",
        "unit": "ratio"
    },
    "task_completion_time": {
        "modality": "work",
        "direction": "higher_is_deviation",
        "weight": 1.1,
        "human_name": "Task completion duration",
        "unit": "sec"
    },
    "task_error_rate": {
        "modality": "work",
        "direction": "higher_is_deviation",
        "weight": 1.3,
        "human_name": "Task error rate",
        "unit": "ratio"
    },

    # Location & Mobility
    "speed_variance": {
        "modality": "mobility",
        "direction": "bidirectional",
        "weight": 1.0,
        "human_name": "Mobility speed consistency",
        "unit": "variance"
    },
    "route_variability": {
        "modality": "mobility",
        "direction": "bidirectional",
        "weight": 1.0,
        "human_name": "Travel route variation",
        "unit": "metric"
    }
}

MODALITY_MAP = {feat: cfg["modality"] for feat, cfg in FEATURE_CONFIG.items()}
