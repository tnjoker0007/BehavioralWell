import urllib.request
import json
import time

BASE_URL = "http://127.0.0.1:8000"

def test_device_registration():
    print("\n--- 1. Testing Device Registration (Phone A) ---")
    payload = {
        "deviceId": "device-uuid-phone-a-1111",
        "deviceModel": "Google Pixel 8 Pro",
        "androidVersion": "Android 14 (API 34)",
        "appVersion": "1.0.0"
    }
    data = json.dumps(payload).encode('utf-8')
    req = urllib.request.Request(f"{BASE_URL}/api/device/register", data=data, headers={"Content-Type": "application/json"})
    try:
        with urllib.request.urlopen(req) as resp:
            body = json.loads(resp.read().decode('utf-8'))
            print("Register Phone A response:", body)
            assert body.get("device_id") == "device-uuid-phone-a-1111"
            print("[SUCCESS] Phone A Registration PASS")
    except Exception as e:
        print("[ERROR] Phone A Registration Failed:", e)
        raise e

def test_multi_device_dev_telemetry():
    print("\n--- 2. Testing Dev Telemetry Streaming (Phone A & Phone B) ---")
    
    # Phone A Telemetry
    telemetry_a = {
        "deviceId": "device-uuid-phone-a-1111",
        "deviceModel": "Google Pixel 8 Pro",
        "androidVersion": "Android 14",
        "snapshotId": "telemetry-phone-a-001",
        "motion": {"movementIntensity": 4.85, "accelerationVariance": 0.12, "stationaryDuration": 5.0},
        "usage": {"screenTime": 2.5, "unlockCount": 35, "nightUsage": 0.2, "appSwitchFrequency": 12.0}
    }
    
    # Phone B Telemetry
    telemetry_b = {
        "deviceId": "device-uuid-phone-b-2222",
        "deviceModel": "Vivo V29 5G",
        "androidVersion": "Android 13",
        "snapshotId": "telemetry-phone-b-001",
        "motion": {"movementIntensity": 0.15, "accelerationVariance": 0.001, "stationaryDuration": 120.0},
        "usage": {"screenTime": 0.8, "unlockCount": 10, "nightUsage": 0.0, "appSwitchFrequency": 4.0}
    }

    # Post Phone A
    data_a = json.dumps(telemetry_a).encode('utf-8')
    req_a = urllib.request.Request(f"{BASE_URL}/api/dev/telemetry", data=data_a, headers={"Content-Type": "application/json"})
    with urllib.request.urlopen(req_a) as resp:
        res = json.loads(resp.read().decode('utf-8'))
        print("Posted Phone A:", res)

    # Post Phone B
    data_b = json.dumps(telemetry_b).encode('utf-8')
    req_b = urllib.request.Request(f"{BASE_URL}/api/dev/telemetry", data=data_b, headers={"Content-Type": "application/json"})
    with urllib.request.urlopen(req_b) as resp:
        res = json.loads(resp.read().decode('utf-8'))
        print("Posted Phone B:", res)

    # Verify device list endpoint
    req_devices = urllib.request.Request(f"{BASE_URL}/api/dev/telemetry/devices")
    with urllib.request.urlopen(req_devices) as resp:
        devices = json.loads(resp.read().decode('utf-8'))
        print("Active Dev Devices:", devices)
        device_ids = [d["deviceId"] for d in devices]
        assert "device-uuid-phone-a-1111" in device_ids
        assert "device-uuid-phone-b-2222" in device_ids
        print("[SUCCESS] Active Devices List PASS")

    # Fetch snapshot for Phone A
    req_cur_a = urllib.request.Request(f"{BASE_URL}/api/dev/telemetry/current?deviceId=device-uuid-phone-a-1111")
    with urllib.request.urlopen(req_cur_a) as resp:
        snap_a = json.loads(resp.read().decode('utf-8'))
        print("Current Snapshot Phone A:", snap_a["deviceId"], "intensity:", snap_a["motion"]["movementIntensity"])
        assert snap_a["motion"]["movementIntensity"] == 4.85

    # Fetch snapshot for Phone B
    req_cur_b = urllib.request.Request(f"{BASE_URL}/api/dev/telemetry/current?deviceId=device-uuid-phone-b-2222")
    with urllib.request.urlopen(req_cur_b) as resp:
        snap_b = json.loads(resp.read().decode('utf-8'))
        print("Current Snapshot Phone B:", snap_b["deviceId"], "intensity:", snap_b["motion"]["movementIntensity"])
        assert snap_b["motion"]["movementIntensity"] == 0.15

    print("[SUCCESS] Multi-device Telemetry Isolation PASS")

if __name__ == "__main__":
    test_device_registration()
    test_multi_device_dev_telemetry()
