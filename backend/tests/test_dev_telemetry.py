from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)

def test_dev_telemetry_flow():
    # 1. Check initial state
    resp = client.get("/api/dev/telemetry/current")
    assert resp.status_code == 200
    assert resp.json() == {}

    # 2. Ingest dev snapshot
    payload = {
        "timestamp": "2026-09-12T10:45:23Z",
        "user_id": "test_phone_user",
        "motion": {
            "movementIntensity": 2.84,
            "accelerationVariance": 1.37,
            "stationaryDuration": 0.0
        },
        "usage": {
            "screenTime": 3.42,
            "unlockCount": 51,
            "nightUsage": 0.31,
            "appSwitchFrequency": 8.2
        },
        "keyboard": {
            "typingSpeed": 57.8,
            "keyPressDuration": 104.0,
            "pauseDuration": 0.42,
            "correctionRate": 0.07
        },
        "mobility": {
            "speedVariance": 1.21,
            "routeVariability": 0.32
        }
    }
    post_resp = client.post("/api/dev/telemetry", json=payload)
    assert post_resp.status_code == 200
    assert post_resp.json()["status"] == "received"

    # 3. Check current state after post
    current_resp = client.get("/api/dev/telemetry/current")
    assert current_resp.status_code == 200
    data = current_resp.json()
    assert data["motion"]["movementIntensity"] == 2.84
    assert data["usage"]["screenTime"] == 3.42

    # 4. Check HTML dashboard page
    page_resp = client.get("/dev/telemetry")
    assert page_resp.status_code == 200
    assert "BehavioralWell Live Telemetry" in page_resp.text
    assert "movementIntensity" in page_resp.text

if __name__ == "__main__":
    test_dev_telemetry_flow()
    print("ALL DEV TELEMETRY BACKEND UNIT TESTS PASSED!")
