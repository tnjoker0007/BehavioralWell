import os
import joblib
import numpy as np
import pandas as pd
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import classification_report, accuracy_score, f1_score, roc_auc_score
from generate_synthetic_data import generate_synthetic_dataset

MODALITIES = {
    "Model A (Keyboard Only)": ["typing_speed_z", "key_press_duration_z", "correction_rate_z"],
    "Model B (Keyboard + Phone)": ["typing_speed_z", "key_press_duration_z", "correction_rate_z", "screen_time_z", "night_usage_z"],
    "Model C (Keyboard + Phone + Motion)": ["typing_speed_z", "key_press_duration_z", "correction_rate_z", "screen_time_z", "night_usage_z", "acceleration_variance_z"],
    "Model D (Keyboard + Phone + Motion + Work)": ["typing_speed_z", "key_press_duration_z", "correction_rate_z", "screen_time_z", "night_usage_z", "acceleration_variance_z", "task_accuracy_z"],
    "Model E (All Modalities - Full Fusion)": ["typing_speed_z", "key_press_duration_z", "correction_rate_z", "screen_time_z", "night_usage_z", "acceleration_variance_z", "task_accuracy_z", "speed_variance_z"]
}

def train_and_evaluate():
    csv_path = os.path.join(os.path.dirname(__file__), "synthetic_behavioral_dataset.csv")
    if not os.path.exists(csv_path):
        generate_synthetic_dataset()

    df = pd.read_csv(csv_path)

    # Calculate Personal Baseline Z-scores per user
    feature_cols = ["typing_speed", "key_press_duration", "correction_rate", "screen_time", "night_usage", "acceleration_variance", "task_accuracy", "speed_variance"]
    
    print("Calculating Z-scores relative to personal baseline (first 14 days per user)...")
    z_cols = []
    
    for col in feature_cols:
        z_col = f"{col}_z"
        z_cols.append(z_col)
        df[z_col] = 0.0

        for user_id, group in df.groupby("user_id"):
            baseline_rows = group[group["day"] < 14]
            mean_val = baseline_rows[col].mean()
            std_val = baseline_rows[col].std()
            if pd.isna(std_val) or std_val < 1e-4:
                std_val = 1.0
            
            df.loc[df["user_id"] == user_id, z_col] = (group[col] - mean_val) / std_val

    # Split train/test by user (group split to avoid data leakage)
    unique_users = df["user_id"].unique()
    np.random.shuffle(unique_users)
    split_idx = int(len(unique_users) * 0.8)
    train_users, test_users = unique_users[:split_idx], unique_users[split_idx:]

    train_df = df[df["user_id"].isin(train_users)]
    test_df = df[df["user_id"].isin(test_users)]

    print("\n--- MULTIMODAL ABLATION STUDY ---")
    results = {}

    for name, cols in MODALITIES.items():
        X_train, y_train = train_df[cols], train_df["risk_stage"]
        X_test, y_test = test_df[cols], test_df["risk_stage"]

        clf = RandomForestClassifier(n_estimators=100, max_depth=8, random_state=42)
        clf.fit(X_train, y_train)

        y_pred = clf.predict(X_test)
        acc = accuracy_score(y_test, y_pred)
        f1 = f1_score(y_test, y_pred, average="weighted")

        results[name] = {"accuracy": acc, "f1_score": f1, "model": clf}
        print(f"{name:45s} | Accuracy: {acc:.4f} | F1: {f1:.4f}")

    # Save best model (Full Fusion Model E)
    models_dir = os.path.join(os.path.dirname(__file__), "models")
    os.makedirs(models_dir, exist_ok=True)
    best_model_path = os.path.join(models_dir, "behavioral_risk_rf.joblib")
    joblib.dump(results["Model E (All Modalities - Full Fusion)"]["model"], best_model_path)
    print(f"\nBest trained model saved to {best_model_path}")

if __name__ == "__main__":
    train_and_evaluate()
