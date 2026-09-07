from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)

def test_routes():
    print("Testing Backend API Routes...")
    
    # 1. Login
    login_resp = client.post("/api/auth/login", json={
        "email": "demo@behavioralwell.ai",
        "password": "Password123!"
    })
    print(f"POST /api/auth/login -> {login_resp.status_code}")
    assert login_resp.status_code == 200
    token = login_resp.json()["access_token"]
    headers = {"Authorization": f"Bearer {token}"}

    # 2. Dashboard
    dash_resp = client.get("/api/dashboard", headers=headers)
    print(f"GET /api/dashboard -> {dash_resp.status_code}")
    assert dash_resp.status_code == 200

    # 3. Interventions Start
    start_resp = client.post("/api/interventions/start", json={
        "activity_type": "Breathing Reset"
    }, headers=headers)
    print(f"POST /api/interventions/start -> {start_resp.status_code}, body: {start_resp.json()}")
    assert start_resp.status_code == 200
    session_id = start_resp.json()["session_id"]

    # 4. Interventions Complete
    comp_resp = client.post("/api/interventions/complete", json={
        "session_id": session_id,
        "feedback_score": 5,
        "result_metrics": {"heart_rate_bpm": 68}
    }, headers=headers)
    print(f"POST /api/interventions/complete -> {comp_resp.status_code}, body: {comp_resp.json()}")
    assert comp_resp.status_code == 200

    # 5. Self-check POST
    self_resp = client.post("/api/self-check", json={
        "mood": "Calm",
        "stress_level": 2,
        "note": "Feeling rested after breathing reset"
    }, headers=headers)
    print(f"POST /api/self-check -> {self_resp.status_code}, body: {self_resp.json()}")
    assert self_resp.status_code == 200

    # 6. Self-check History GET
    hist_resp = client.get("/api/self-check/history", headers=headers)
    print(f"GET /api/self-check/history -> {hist_resp.status_code}, items count: {len(hist_resp.json())}")
    assert hist_resp.status_code == 200

    print("\nALL API ROUTE INTEGRATION TESTS PASSED PERFECTLY!")

if __name__ == "__main__":
    test_routes()
