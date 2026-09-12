import urllib.request
import json

try:
    data = json.dumps({'email': 'demo@behavioralwell.ai', 'password': 'Password123!'}).encode('utf-8')
    req = urllib.request.Request('http://127.0.0.1:8000/api/auth/login', data=data, headers={'Content-Type': 'application/json'})
    with urllib.request.urlopen(req, timeout=5) as resp:
        res = json.loads(resp.read().decode())
        token = res['access_token']
        print("Logged in successfully. User:", res['user']['email'])

    headers = {'Authorization': f'Bearer {token}', 'Content-Type': 'application/json'}

    payload = json.dumps({
        'typing_speed': 45.2,
        'key_press_duration': 180.5,
        'correction_rate': 0.12,
        'screen_time': 5.5,
        'night_usage': 1.2,
        'acceleration_variance': 0.85,
        'task_accuracy': 0.92,
        'speed_variance': 1.45,
        'idempotency_key': 'realtime_verif_003'
    }).encode('utf-8')

    req2 = urllib.request.Request('http://127.0.0.1:8000/api/telemetry', data=payload, headers=headers)
    with urllib.request.urlopen(req2, timeout=5) as resp2:
        telemetry_res = json.loads(resp2.read().decode())

    print("\n[POST /api/telemetry Response]")
    print(json.dumps(telemetry_res, indent=2))

    req3 = urllib.request.Request('http://127.0.0.1:8000/api/risk/current', headers=headers)
    with urllib.request.urlopen(req3, timeout=5) as resp3:
        risk_res = json.loads(resp3.read().decode())

    print("\n[GET /api/risk/current Response]")
    print(json.dumps(risk_res, indent=2))

except Exception as e:
    print("API Error:", e)
