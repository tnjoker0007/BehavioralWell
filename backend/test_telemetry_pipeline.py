import sys
import time
from fastapi.testclient import TestClient
from app.main import app

def test_telemetry_ingestion_and_idempotency():
    client = TestClient(app)
    
    # 1. Test Dashboard Before Telemetry
    res_dash = client.get("/api/dashboard")
    assert res_dash.status_code == 200, f"Dashboard error: {res_dash.status_code}"
    print("[PASS] GET /api/dashboard succeeded (200 OK)")

    ts = int(time.time())
    key1 = f"test_evt_{ts}_A"
    key2 = f"test_evt_{ts}_B"

    # 2. Test Single Telemetry Ingestion
    payload = {
        "idempotency_key": key1,
        "typing_speed": 52.5,
        "key_press_duration": 115.0,
        "pause_duration": 0.22,
        "correction_rate": 0.04,
        "screen_time": 4.2,
        "unlock_count": 35,
        "night_usage": 0.5,
        "app_switch_frequency": 8.1,
        "movement_intensity": 0.35,
        "acceleration_variance": 0.08,
        "stationary_duration": 14.5
    }
    
    res_telemetry = client.post("/api/telemetry", json=payload)
    assert res_telemetry.status_code == 200, f"Telemetry error: {res_telemetry.text}"
    data = res_telemetry.json()
    print("[PASS] POST /api/telemetry succeeded (200 OK). Risk score:", data.get("risk_score"))

    # 3. Test Batch Ingestion & Idempotency Deduplication
    batch_payload = {
        "batch": [
            {
                "idempotency_key": key1, # Existing duplicate key
                "screen_time": 4.2
            },
            {
                "idempotency_key": key2, # New unique key
                "screen_time": 5.1,
                "unlock_count": 42
            }
        ]
    }
    
    res_batch = client.post("/api/telemetry/batch", json=batch_payload)
    assert res_batch.status_code == 200, f"Batch error: {res_batch.text}"
    batch_res = res_batch.json()
    assert batch_res["processed_count"] == 1, f"Expected 1 processed, got {batch_res['processed_count']}"
    assert batch_res["skipped_duplicates"] == 1, f"Expected 1 skipped duplicate, got {batch_res['skipped_duplicates']}"
    print(f"[PASS] POST /api/telemetry/batch succeeded! Processed: {batch_res['processed_count']}, Skipped Duplicates: {batch_res['skipped_duplicates']}")

    # 4. Test Telemetry Status
    res_status = client.get("/api/telemetry/status")
    assert res_status.status_code == 200
    status_data = res_status.json()
    print("[PASS] GET /api/telemetry/status succeeded! Total records:", status_data.get("total_records"), "Modalities:", status_data.get("modalities_collected"))

    # 5. Test Consent Update
    consent_payload = {
        "keyboard_enabled": True,
        "usage_enabled": True,
        "motion_enabled": False,
        "work_enabled": True,
        "mobility_enabled": False
    }
    res_consent = client.put("/api/consent", json=consent_payload)
    assert res_consent.status_code == 200, f"Consent error: {res_consent.text}"
    print("[PASS] PUT /api/consent succeeded!")

    print("\n--- ALL BACKEND TELEMETRY TESTS PASSED SUCCESSFULLY! ---")

if __name__ == "__main__":
    test_telemetry_ingestion_and_idempotency()
