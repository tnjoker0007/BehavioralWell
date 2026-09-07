import os
import json
import numpy as np
import pandas as pd

def generate_synthetic_dataset(num_users=200, days_per_user=60, seed=42):
    np.random.seed(seed)
    data = []

    print(f"Generating synthetic dataset: {num_users} users x {days_per_user} days...")

    for u in range(num_users):
        user_id = f"user_{u:04d}"
        
        # User personal baseline parameters
        base_wpm = np.random.normal(60.0, 8.0)
        base_key_dwell = np.random.normal(115.0, 10.0)
        base_screen = np.random.normal(4.0, 0.8)
        base_night = np.random.normal(0.3, 0.1)
        base_accel_var = np.random.normal(0.10, 0.02)
        base_accuracy = np.random.normal(94.0, 2.0)
        base_speed_var = np.random.normal(0.12, 0.03)

        # User risk trajectory pattern
        trajectory_type = np.random.choice(["stable", "mild_stress", "severe_deviation"], p=[0.5, 0.3, 0.2])

        for day in range(days_per_user):
            # First 14 days are baseline learning
            is_baseline_period = (day < 14)
            
            if is_baseline_period or trajectory_type == "stable":
                dev_factor = 0.0
                risk_stage = 0
            elif trajectory_type == "mild_stress":
                if day > 35:
                    dev_factor = 0.5 # recovery
                    risk_stage = 1
                else:
                    dev_factor = 1.2
                    risk_stage = 1 if day < 20 else 2
            else: # severe_deviation
                if day < 20:
                    dev_factor = 1.0
                    risk_stage = 1
                elif day < 40:
                    dev_factor = 2.5
                    risk_stage = 3
                else:
                    dev_factor = 3.2
                    risk_stage = 4

            # Feature values with noise
            noise = lambda: np.random.normal(0, 0.05)
            
            typing_speed = max(20.0, base_wpm - dev_factor * 8.0 + noise() * 3.0)
            key_dwell = max(50.0, base_key_dwell + dev_factor * 20.0 + noise() * 5.0)
            correction_rate = max(1.0, 4.0 + dev_factor * 4.5 + noise() * 1.5)
            
            screen_time = max(1.0, base_screen + dev_factor * 1.5 + noise() * 0.5)
            night_usage = max(0.0, base_night + dev_factor * 0.9 + noise() * 0.2)
            unlock_count = int(max(10, 40 + dev_factor * 18 + noise() * 5.0))
            
            movement_intensity = max(0.2, 1.0 - dev_factor * 0.15 + noise() * 0.1)
            acceleration_variance = max(0.02, base_accel_var + dev_factor * 0.12 + noise() * 0.03)
            
            task_accuracy = max(40.0, min(100.0, base_accuracy - dev_factor * 7.5 + noise() * 2.0))
            task_completion_time = max(10.0, 18.0 + dev_factor * 5.0 + noise() * 2.0)
            
            speed_variance = max(0.01, base_speed_var + dev_factor * 0.10 + noise() * 0.02)

            data.append({
                "user_id": user_id,
                "day": day,
                "typing_speed": round(typing_speed, 2),
                "key_press_duration": round(key_dwell, 2),
                "correction_rate": round(correction_rate, 2),
                "screen_time": round(screen_time, 2),
                "night_usage": round(night_usage, 2),
                "unlock_count": unlock_count,
                "movement_intensity": round(movement_intensity, 2),
                "acceleration_variance": round(acceleration_variance, 4),
                "task_accuracy": round(task_accuracy, 2),
                "task_completion_time": round(task_completion_time, 2),
                "speed_variance": round(speed_variance, 4),
                "risk_stage": risk_stage
            })

    df = pd.DataFrame(data)
    os.makedirs(os.path.dirname(__file__), exist_ok=True)
    csv_path = os.path.join(os.path.dirname(__file__), "synthetic_behavioral_dataset.csv")
    df.to_csv(csv_path, index=False)
    print(f"Dataset successfully saved to {csv_path} ({len(df)} rows)")
    return csv_path

if __name__ == "__main__":
    generate_synthetic_dataset()
