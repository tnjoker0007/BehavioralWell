from typing import List, Dict, Any

class SyntheticScenarioGenerator:
    """
    Generates deterministic synthetic datasets for behavioral evaluation:
    - Stable User
    - Sleep Disruption
    - Acute Stress-like Deviation
    - Persistent Multimodal Deviation
    - Recovery
    """
    
    @staticmethod
    def get_baseline_records(days: int = 30) -> List[Dict[str, float]]:
        """Generates 30 days of consistent baseline telemetry data for a stable user."""
        records = []
        for i in range(days):
            records.append({
                "typing_speed": 62.0 + (i % 3) * 1.0,
                "key_press_duration": 110.0 + (i % 2) * 2.0,
                "pause_duration": 0.25 + (i % 3) * 0.01,
                "correction_rate": 0.04 + (i % 2) * 0.005,
                "screen_time": 4.0 + (i % 4) * 0.2,
                "unlock_count": 30 + (i % 5),
                "night_usage": 0.2 + (i % 2) * 0.05,
                "app_switch_frequency": 6.0 + (i % 3) * 0.5,
                "movement_intensity": 0.45 + (i % 4) * 0.02,
                "acceleration_variance": 0.05 + (i % 3) * 0.005,
                "stationary_duration": 12.0 + (i % 2) * 0.5,
                "task_accuracy": 0.92 + (i % 2) * 0.01,
                "task_completion_time": 45.0 + (i % 3) * 1.5,
                "task_error_rate": 0.05 + (i % 2) * 0.01,
                "speed_variance": 0.15 + (i % 2) * 0.01,
                "route_variability": 0.20 + (i % 3) * 0.02
            })
        return records

    @staticmethod
    def get_stable_scenario() -> Dict[str, float]:
        """Scenario A: Stable User within normal baseline range."""
        return {
            "typing_speed": 63.0,
            "key_press_duration": 112.0,
            "pause_duration": 0.26,
            "correction_rate": 0.04,
            "screen_time": 4.1,
            "unlock_count": 31,
            "night_usage": 0.2,
            "app_switch_frequency": 6.2,
            "movement_intensity": 0.46,
            "acceleration_variance": 0.05,
            "stationary_duration": 12.1,
            "task_accuracy": 0.93,
            "task_completion_time": 46.0,
            "task_error_rate": 0.05,
            "speed_variance": 0.16,
            "route_variability": 0.21
        }

    @staticmethod
    def get_sleep_disruption_scenario() -> Dict[str, float]:
        """Scenario B: Isolated sleep disruption (high night usage & screen time)."""
        return {
            "typing_speed": 60.0,
            "key_press_duration": 118.0,
            "pause_duration": 0.28,
            "correction_rate": 0.05,
            "screen_time": 7.5,      # Elevated
            "unlock_count": 55,       # Elevated
            "night_usage": 3.2,       # High night usage
            "app_switch_frequency": 12.0, # High switching
            "movement_intensity": 0.44,
            "acceleration_variance": 0.05,
            "stationary_duration": 13.0,
            "task_accuracy": 0.90,
            "task_completion_time": 48.0,
            "task_error_rate": 0.06,
            "speed_variance": 0.15,
            "route_variability": 0.20
        }

    @staticmethod
    def get_acute_deviation_scenario() -> Dict[str, float]:
        """Scenario C: Acute single-day deviation spike."""
        return {
            "typing_speed": 38.0,      # Slower
            "key_press_duration": 160.0, # High dwell time
            "pause_duration": 0.75,     # High pauses
            "correction_rate": 0.18,    # High backspaces
            "screen_time": 6.8,
            "unlock_count": 48,
            "night_usage": 1.5,
            "app_switch_frequency": 14.0,
            "movement_intensity": 0.25,
            "acceleration_variance": 0.12,
            "stationary_duration": 16.0,
            "task_accuracy": 0.70,      # Lower accuracy
            "task_completion_time": 75.0, # Slow completion
            "task_error_rate": 0.25,     # High errors
            "speed_variance": 0.35,
            "route_variability": 0.45
        }

    @staticmethod
    def get_persistent_multimodal_scenario() -> List[Dict[str, float]]:
        """Scenario D: 5 consecutive days of multimodal behavioral deviation."""
        persistent_days = []
        for i in range(5):
            persistent_days.append({
                "typing_speed": 35.0 - i * 1.0,
                "key_press_duration": 165.0 + i * 2.0,
                "pause_duration": 0.80 + i * 0.02,
                "correction_rate": 0.20 + i * 0.01,
                "screen_time": 8.0 + i * 0.3,
                "unlock_count": 60 + i * 2,
                "night_usage": 3.0 + i * 0.2,
                "app_switch_frequency": 16.0 + i * 1.0,
                "movement_intensity": 0.20 - i * 0.01,
                "acceleration_variance": 0.15 + i * 0.01,
                "stationary_duration": 17.0 + i * 0.5,
                "task_accuracy": 0.65 - i * 0.02,
                "task_completion_time": 80.0 + i * 3.0,
                "task_error_rate": 0.30 + i * 0.02,
                "speed_variance": 0.40 + i * 0.02,
                "route_variability": 0.50 + i * 0.03
            })
        return persistent_days

    @staticmethod
    def get_recovery_scenario() -> List[Dict[str, float]]:
        """Scenario E: Deviation recovering towards normal baseline over 4 days."""
        recovery_days = []
        for i in range(4):
            # i = 0 (high deviation) -> i = 3 (returning to baseline)
            factor = (3 - i) / 3.0
            recovery_days.append({
                "typing_speed": 62.0 - (25.0 * factor),
                "key_press_duration": 110.0 + (50.0 * factor),
                "pause_duration": 0.25 + (0.50 * factor),
                "correction_rate": 0.04 + (0.15 * factor),
                "screen_time": 4.0 + (4.0 * factor),
                "unlock_count": 30 + int(30 * factor),
                "night_usage": 0.2 + (2.5 * factor),
                "app_switch_frequency": 6.0 + (10.0 * factor),
                "movement_intensity": 0.45 - (0.20 * factor),
                "acceleration_variance": 0.05 + (0.10 * factor),
                "stationary_duration": 12.0 + (5.0 * factor),
                "task_accuracy": 0.92 - (0.25 * factor),
                "task_completion_time": 45.0 + (30.0 * factor),
                "task_error_rate": 0.05 + (0.20 * factor),
                "speed_variance": 0.15 + (0.25 * factor),
                "route_variability": 0.20 + (0.30 * factor)
            })
        return recovery_days
