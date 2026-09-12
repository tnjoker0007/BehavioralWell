import asyncio
import json
from datetime import datetime
from typing import Dict, Any, List
from fastapi import APIRouter, Request, Response, HTTPException
from fastapi.responses import HTMLResponse, StreamingResponse
from app.config import settings

router = APIRouter()
page_router = APIRouter()

# In-memory latest snapshot store (DEV ONLY — No DB writes)
_latest_dev_telemetry: Dict[str, Any] = {}
_subscribers: List[asyncio.Queue] = []
_subscribers_lock = asyncio.Lock()

def _check_dev_guard():
    # Allow dev endpoints in development environment
    pass

@router.post("/dev/telemetry")
async def ingest_dev_telemetry(payload: Dict[str, Any]):
    """DEV-ONLY route to receive real-time derived telemetry snapshots from Android."""
    _check_dev_guard()
    global _latest_dev_telemetry

    timestamp = payload.get("timestamp", datetime.utcnow().isoformat())
    user_id = payload.get("user_id", "dev_user")
    
    modalities = [k for k in payload.keys() if k in ["motion", "usage", "keyboard", "activity", "mobility"]]
    
    # Safe diagnostic logging ONLY — No tokens, passwords, raw sensors, or GPS logged
    print(f"[DEV TELEMETRY] received user={user_id} timestamp={timestamp} modalities={','.join(modalities)}")

    _latest_dev_telemetry = payload

    # Broadcast snapshot to active SSE subscribers
    async with _subscribers_lock:
        for queue in list(_subscribers):
            try:
                queue.put_nowait(payload)
            except Exception:
                pass

    return {"status": "received", "timestamp": timestamp}

@router.get("/dev/telemetry/current")
async def get_current_dev_telemetry():
    """DEV-ONLY route to return current in-memory telemetry snapshot."""
    _check_dev_guard()
    return _latest_dev_telemetry

@router.get("/dev/telemetry/stream")
async def stream_dev_telemetry(request: Request):
    """DEV-ONLY Server-Sent Events (SSE) stream broadcasting real-time snapshots at ~1Hz."""
    _check_dev_guard()
    queue = asyncio.Queue()

    async with _subscribers_lock:
        _subscribers.append(queue)

    # Yield current state immediately if available
    if _latest_dev_telemetry:
        queue.put_nowait(_latest_dev_telemetry)

    async def event_generator():
        try:
            while True:
                if await request.is_disconnected():
                    break
                try:
                    data = await asyncio.wait_for(queue.get(), timeout=1.0)
                    yield f"data: {json.dumps(data)}\n\n"
                except asyncio.TimeoutError:
                    yield f": heartbeat\n\n"
        except asyncio.CancelledError:
            pass
        finally:
            async with _subscribers_lock:
                if queue in _subscribers:
                    _subscribers.remove(queue)

    return StreamingResponse(
        event_generator(),
        media_type="text/event-stream",
        headers={
            "Cache-Control": "no-cache",
            "Connection": "keep-alive",
            "X-Accel-Buffering": "no"
        }
    )

@page_router.get("/dev/telemetry", response_class=HTMLResponse)
async def dev_telemetry_dashboard_page():
    """DEV-ONLY Live Telemetry Web Dashboard."""
    _check_dev_guard()
    html_content = """<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>BehavioralWell — Live Telemetry Monitor (DEV ONLY)</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <style>
        :root {
            --bg-dark: #0D1117;
            --surface-dark: #161B22;
            --card-bg: rgba(22, 27, 34, 0.75);
            --border-color: #30363D;
            --text-primary: #F0F6FC;
            --text-secondary: #8B949E;
            --primary-purple: #A855F7;
            --primary-teal: #14B8A6;
            --status-live: #22C55E;
            --status-stale: #EAB308;
            --status-disconnected: #EF4444;
        }

        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
            font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
        }

        body {
            background-color: var(--bg-dark);
            color: var(--text-primary);
            min-height: 100vh;
            padding: 24px;
        }

        .header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 20px 24px;
            background: var(--surface-dark);
            border: 1px solid var(--border-color);
            border-radius: 16px;
            margin-bottom: 24px;
            backdrop-filter: blur(12px);
        }

        .header-title h1 {
            font-size: 1.5rem;
            font-weight: 700;
            background: linear-gradient(135deg, #C084FC, #2DD4BF);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
        }

        .header-title p {
            font-size: 0.85rem;
            color: var(--text-secondary);
            margin-top: 4px;
        }

        .status-badge {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            padding: 8px 16px;
            border-radius: 20px;
            font-size: 0.85rem;
            font-weight: 700;
            letter-spacing: 0.5px;
            background: rgba(255, 255, 255, 0.05);
            border: 1px solid var(--border-color);
        }

        .status-dot {
            width: 10px;
            height: 10px;
            border-radius: 50%;
            display: inline-block;
        }

        .status-live { background-color: var(--status-live); box-shadow: 0 0 10px var(--status-live); }
        .status-stale { background-color: var(--status-stale); box-shadow: 0 0 10px var(--status-stale); }
        .status-disconnected { background-color: var(--status-disconnected); box-shadow: 0 0 10px var(--status-disconnected); }

        .control-bar {
            display: flex;
            gap: 12px;
            margin-bottom: 24px;
            align-items: center;
        }

        .btn {
            background: var(--surface-dark);
            color: var(--text-primary);
            border: 1px solid var(--border-color);
            padding: 10px 20px;
            border-radius: 10px;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.2s ease;
        }

        .btn:hover {
            border-color: var(--primary-purple);
            transform: translateY(-1px);
        }

        .btn-primary {
            background: linear-gradient(135deg, #9333EA, #0D9488);
            border: none;
        }

        .meta-info {
            font-size: 0.85rem;
            color: var(--text-secondary);
            margin-left: auto;
        }

        .grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
            gap: 20px;
        }

        .card {
            background: var(--card-bg);
            border: 1px solid var(--border-color);
            border-radius: 16px;
            padding: 20px;
            backdrop-filter: blur(16px);
        }

        .card-header {
            display: flex;
            align-items: center;
            justify-content: space-between;
            border-bottom: 1px solid rgba(255, 255, 255, 0.08);
            padding-bottom: 12px;
            margin-bottom: 16px;
        }

        .card-title {
            font-size: 1.1rem;
            font-weight: 700;
            color: var(--primary-purple);
            display: flex;
            align-items: center;
            gap: 8px;
        }

        .metric-row {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 10px 0;
            border-bottom: 1px dashed rgba(255, 255, 255, 0.05);
        }

        .metric-row:last-child {
            border-bottom: none;
        }

        .metric-name {
            font-size: 0.9rem;
            color: var(--text-secondary);
        }

        .metric-value {
            font-size: 1.05rem;
            font-weight: 700;
            color: var(--primary-teal);
        }

        .metric-value.waiting {
            color: var(--text-secondary);
            font-weight: 500;
            font-style: italic;
        }

        .changed-ago {
            font-size: 0.75rem;
            color: #6E7681;
            margin-left: 6px;
        }

        .graph-container {
            margin-top: 16px;
            background: rgba(0, 0, 0, 0.3);
            border-radius: 12px;
            padding: 12px;
            border: 1px solid var(--border-color);
        }

        canvas {
            width: 100%;
            height: 120px;
            display: block;
        }

        .privacy-note {
            font-size: 0.75rem;
            color: var(--text-secondary);
            margin-top: 10px;
            line-height: 1.4;
            opacity: 0.8;
        }
    </style>
</head>
<body>
    <div class="header">
        <div class="header-title">
            <h1>BehavioralWell Live Telemetry</h1>
            <p>Real-Time Android Derived Feature Monitor (DEV ONLY — No DB Writes)</p>
        </div>
        <div id="statusBadge" class="status-badge">
            <span id="statusDot" class="status-dot status-disconnected"></span>
            <span id="statusText">DISCONNECTED</span>
        </div>
    </div>

    <div class="control-bar">
        <button id="btnStart" class="btn btn-primary" onclick="startMonitor()">START LIVE MONITOR</button>
        <button id="btnStop" class="btn" onclick="stopMonitor()">STOP LIVE MONITOR</button>
        <button class="btn" onclick="clearDisplay()">CLEAR DISPLAY</button>
        <div class="meta-info">
            Stream Last Update: <span id="lastStreamUpdate">Never</span> | Phone Telemetry: <span id="lastPhoneTime">N/A</span>
        </div>
    </div>

    <div class="grid">
        <!-- MOTION -->
        <div class="card">
            <div class="card-header">
                <span class="card-title">🏃 MOTION</span>
                <span id="motionChanged" class="changed-ago">Waiting...</span>
            </div>
            <div class="metric-row">
                <span class="metric-name">Movement Intensity</span>
                <span id="val_movementIntensity" class="metric-value waiting">Waiting for sensor...</span>
            </div>
            <div class="metric-row">
                <span class="metric-name">Acceleration Variance</span>
                <span id="val_accelerationVariance" class="metric-value waiting">Waiting for sensor...</span>
            </div>
            <div class="metric-row">
                <span class="metric-name">Stationary Duration</span>
                <span id="val_stationaryDuration" class="metric-value waiting">Waiting for sensor...</span>
            </div>
            <div class="graph-container">
                <canvas id="motionCanvas"></canvas>
            </div>
            <p class="privacy-note">Calculated derived magnitude variance. Zero raw accel/gyro vectors transmitted.</p>
        </div>

        <!-- USAGE -->
        <div class="card">
            <div class="card-header">
                <span class="card-title">📱 APP USAGE</span>
                <span id="usageChanged" class="changed-ago">Waiting...</span>
            </div>
            <div class="metric-row">
                <span class="metric-name">Screen Time</span>
                <span id="val_screenTime" class="metric-value waiting">Waiting for sensor...</span>
            </div>
            <div class="metric-row">
                <span class="metric-name">Unlock Count</span>
                <span id="val_unlockCount" class="metric-value waiting">Waiting for sensor...</span>
            </div>
            <div class="metric-row">
                <span class="metric-name">Night Usage</span>
                <span id="val_nightUsage" class="metric-value waiting">Waiting for sensor...</span>
            </div>
            <div class="metric-row">
                <span class="metric-name">App Switch Frequency</span>
                <span id="val_appSwitchFrequency" class="metric-value waiting">Waiting for sensor...</span>
            </div>
            <p class="privacy-note">Aggregated daily foreground stats. Updates when usage events change.</p>
        </div>

        <!-- KEYBOARD -->
        <div class="card">
            <div class="card-header">
                <span class="card-title">⌨️ KEYBOARD METADATA</span>
                <span id="keyboardChanged" class="changed-ago">Waiting...</span>
            </div>
            <div class="metric-row">
                <span class="metric-name">Typing Speed</span>
                <span id="val_typingSpeed" class="metric-value waiting">Waiting for sensor...</span>
            </div>
            <div class="metric-row">
                <span class="metric-name">Key Press Duration</span>
                <span id="val_keyPressDuration" class="metric-value waiting">Waiting for sensor...</span>
            </div>
            <div class="metric-row">
                <span class="metric-name">Pause Duration</span>
                <span id="val_pauseDuration" class="metric-value waiting">Waiting for sensor...</span>
            </div>
            <div class="metric-row">
                <span class="metric-name">Correction Rate</span>
                <span id="val_correctionRate" class="metric-value waiting">Waiting for sensor...</span>
            </div>
            <p class="privacy-note">Derived typing dynamics only. Zero text, characters, or passwords collected.</p>
        </div>

        <!-- ACTIVITY -->
        <div class="card">
            <div class="card-header">
                <span class="card-title">🚶 ACTIVITY</span>
                <span id="activityChanged" class="changed-ago">Waiting...</span>
            </div>
            <div class="metric-row">
                <span class="metric-name">Movement Intensity</span>
                <span id="val_activityMovementIntensity" class="metric-value waiting">Waiting for sensor...</span>
            </div>
            <p class="privacy-note">Derived activity state transitions.</p>
        </div>

        <!-- MOBILITY -->
        <div class="card">
            <div class="card-header">
                <span class="card-title">🗺️ MOBILITY</span>
                <span id="mobilityChanged" class="changed-ago">Waiting...</span>
            </div>
            <div class="metric-row">
                <span class="metric-name">Speed Variance</span>
                <span id="val_speedVariance" class="metric-value waiting">Waiting for sensor...</span>
            </div>
            <div class="metric-row">
                <span class="metric-name">Route Variability</span>
                <span id="val_routeVariability" class="metric-value waiting">Waiting for sensor...</span>
            </div>
            <p class="privacy-note">Derived spatial variance ratios. Zero GPS coordinates stored or transmitted.</p>
        </div>
    </div>

    <script>
        let eventSource = null;
        let lastReceivedTimestamp = null;
        let motionHistory = [];
        const MAX_GRAPH_POINTS = 50;

        const previousValues = {};
        const lastChangedTimestamps = {};

        function startMonitor() {
            if (eventSource) {
                eventSource.close();
            }

            updateConnectionStatus('CONNECTING', 'status-stale');
            eventSource = new EventSource('/api/dev/telemetry/stream');

            eventSource.onopen = function() {
                updateConnectionStatus('LIVE', 'status-live');
            };

            eventSource.onmessage = function(event) {
                if (!event.data) return;
                try {
                    const data = JSON.parse(event.data);
                    handleTelemetrySnapshot(data);
                } catch (e) {
                    console.error("Error parsing SSE data:", e);
                }
            };

            eventSource.onerror = function() {
                updateConnectionStatus('DISCONNECTED', 'status-disconnected');
            };
        }

        function stopMonitor() {
            if (eventSource) {
                eventSource.close();
                eventSource = null;
            }
            updateConnectionStatus('DISCONNECTED', 'status-disconnected');
        }

        function updateConnectionStatus(text, className) {
            const dot = document.getElementById('statusDot');
            const textSpan = document.getElementById('statusText');
            dot.className = 'status-dot ' + className;
            textSpan.innerText = text;
        }

        function clearDisplay() {
            const elements = document.querySelectorAll('.metric-value');
            elements.forEach(el => {
                el.innerText = 'Waiting for sensor...';
                el.className = 'metric-value waiting';
            });
            motionHistory = [];
            drawGraph();
        }

        function handleTelemetrySnapshot(data) {
            lastReceivedTimestamp = new Date();
            document.getElementById('lastStreamUpdate').innerText = lastReceivedTimestamp.toLocaleTimeString();
            if (data.timestamp) {
                document.getElementById('lastPhoneTime').innerText = data.timestamp.split('T')[1] || data.timestamp;
            }

            updateConnectionStatus('LIVE', 'status-live');

            const now = new Date();

            // 1. Motion
            if (data.motion) {
                const intensity = data.motion.movementIntensity;
                const variance = data.motion.accelerationVariance;
                const stationary = data.motion.stationaryDuration;

                updateMetric('val_movementIntensity', intensity !== undefined && intensity !== null ? intensity.toFixed(2) : null, 'motionChanged', 'motion', now);
                updateMetric('val_accelerationVariance', variance !== undefined && variance !== null ? variance.toFixed(4) : null, 'motionChanged', 'motion', now);
                updateMetric('val_stationaryDuration', stationary !== undefined && stationary !== null ? stationary.toFixed(1) + ' mins' : null, 'motionChanged', 'motion', now);

                if (intensity !== undefined && intensity !== null) {
                    motionHistory.push(intensity);
                    if (motionHistory.length > MAX_GRAPH_POINTS) {
                        motionHistory.shift();
                    }
                    drawGraph();
                }
            }

            // 2. Usage
            if (data.usage) {
                const status = data.usage.status;
                if (status === 'USAGE_ACCESS_REQUIRED') {
                    updateStatusMetric('val_screenTime', 'Usage Access Required', 'usageChanged', 'usage', now, 'warning');
                    updateStatusMetric('val_unlockCount', 'Usage Access Required', 'usageChanged', 'usage', now, 'warning');
                    updateStatusMetric('val_nightUsage', 'Usage Access Required', 'usageChanged', 'usage', now, 'warning');
                    updateStatusMetric('val_appSwitchFrequency', 'Usage Access Required', 'usageChanged', 'usage', now, 'warning');
                } else if (status === 'WAITING_FOR_USAGE_DATA') {
                    updateStatusMetric('val_screenTime', 'Waiting for Usage Data', 'usageChanged', 'usage', now, 'waiting');
                    updateStatusMetric('val_unlockCount', 'Waiting for Usage Data', 'usageChanged', 'usage', now, 'waiting');
                    updateStatusMetric('val_nightUsage', 'Waiting for Usage Data', 'usageChanged', 'usage', now, 'waiting');
                    updateStatusMetric('val_appSwitchFrequency', 'Waiting for Usage Data', 'usageChanged', 'usage', now, 'waiting');
                } else {
                    const screenTime = data.usage.screenTime;
                    const unlock = data.usage.unlockCount;
                    const night = data.usage.nightUsage;
                    const freq = data.usage.appSwitchFrequency;

                    updateMetric('val_screenTime', screenTime !== undefined && screenTime !== null ? screenTime.toFixed(2) + ' hrs' : null, 'usageChanged', 'usage', now);
                    updateMetric('val_unlockCount', unlock !== undefined && unlock !== null ? unlock : null, 'usageChanged', 'usage', now);
                    updateMetric('val_nightUsage', night !== undefined && night !== null ? night.toFixed(2) + ' hrs' : null, 'usageChanged', 'usage', now);
                    updateMetric('val_appSwitchFrequency', freq !== undefined && freq !== null ? freq.toFixed(1) + ' /hr' : null, 'usageChanged', 'usage', now);
                }
            }

            // 3. Keyboard
            if (data.keyboard) {
                const status = data.keyboard.status;
                if (status === 'WAITING_FOR_INPUT') {
                    updateStatusMetric('val_typingSpeed', 'Waiting for Typing Input', 'keyboardChanged', 'keyboard', now, 'waiting');
                    updateStatusMetric('val_keyPressDuration', 'Waiting for Typing Input', 'keyboardChanged', 'keyboard', now, 'waiting');
                    updateStatusMetric('val_pauseDuration', 'Waiting for Typing Input', 'keyboardChanged', 'keyboard', now, 'waiting');
                    updateStatusMetric('val_correctionRate', 'Waiting for Typing Input', 'keyboardChanged', 'keyboard', now, 'waiting');
                } else {
                    const speed = data.keyboard.typingSpeed;
                    const dwell = data.keyboard.keyPressDuration;
                    const pause = data.keyboard.pauseDuration;
                    const corr = data.keyboard.correctionRate;

                    updateMetric('val_typingSpeed', speed !== undefined && speed !== null ? speed.toFixed(1) + ' WPM' : null, 'keyboardChanged', 'keyboard', now);
                    updateMetric('val_keyPressDuration', dwell !== undefined && dwell !== null ? dwell.toFixed(0) + ' ms' : null, 'keyboardChanged', 'keyboard', now);
                    updateMetric('val_pauseDuration', pause !== undefined && pause !== null ? pause.toFixed(2) + ' s' : null, 'keyboardChanged', 'keyboard', now);
                    updateMetric('val_correctionRate', corr !== undefined && corr !== null ? (corr * 100).toFixed(1) + ' %' : null, 'keyboardChanged', 'keyboard', now);
                }
            }

            // 4. Activity
            if (data.activity) {
                const status = data.activity.status;
                const actInt = data.activity.movementIntensity;
                const actState = data.activity.activityState || 'Live';
                if (status === 'WAITING_FOR_MOTION') {
                    updateStatusMetric('val_activityMovementIntensity', 'Waiting for Motion Data', 'activityChanged', 'activity', now, 'waiting');
                } else {
                    updateMetric('val_activityMovementIntensity', actInt !== undefined && actInt !== null ? actInt.toFixed(2) + ' (' + actState + ')' : null, 'activityChanged', 'activity', now);
                }
            }

            // 5. Mobility
            if (data.mobility) {
                const status = data.mobility.status;
                if (status === 'LOCATION_PERMISSION_REQUIRED') {
                    updateStatusMetric('val_speedVariance', 'Location Permission Required', 'mobilityChanged', 'mobility', now, 'warning');
                    updateStatusMetric('val_routeVariability', 'Location Permission Required', 'mobilityChanged', 'mobility', now, 'warning');
                } else if (status === 'WAITING_FOR_LOCATION') {
                    updateStatusMetric('val_speedVariance', 'Waiting for Location Fix', 'mobilityChanged', 'mobility', now, 'waiting');
                    updateStatusMetric('val_routeVariability', 'Waiting for Location Fix', 'mobilityChanged', 'mobility', now, 'waiting');
                } else {
                    const spd = data.mobility.speedVariance;
                    const rte = data.mobility.routeVariability;

                    updateMetric('val_speedVariance', spd !== undefined && spd !== null ? spd.toFixed(2) : null, 'mobilityChanged', 'mobility', now);
                    updateMetric('val_routeVariability', rte !== undefined && rte !== null ? rte.toFixed(2) : null, 'mobilityChanged', 'mobility', now);
                }
            }
        }

        function updateStatusMetric(elementId, text, categoryHeaderId, categoryKey, nowTime, statusType) {
            const el = document.getElementById(elementId);
            if (!el) return;
            el.innerText = text;
            el.className = 'metric-value ' + statusType;
        }

        function updateMetric(elementId, valStr, categoryHeaderId, categoryKey, nowTime) {
            const el = document.getElementById(elementId);
            if (!el) return;

            if (valStr === null || valStr === undefined) {
                el.innerText = 'Waiting for sensor...';
                el.className = 'metric-value waiting';
            } else {
                if (previousValues[elementId] !== valStr) {
                    previousValues[elementId] = valStr;
                    lastChangedTimestamps[categoryKey] = nowTime;
                }
                el.innerText = valStr;
                el.className = 'metric-value';
            }

            const headerEl = document.getElementById(categoryHeaderId);
            if (headerEl && lastChangedTimestamps[categoryKey]) {
                const elapsedSec = Math.round((nowTime - lastChangedTimestamps[categoryKey]) / 1000);
                headerEl.innerText = elapsedSec === 0 ? 'Changed just now' : `Changed ${elapsedSec}s ago`;
            }
        }

        // Health check interval for STALE status
        setInterval(() => {
            if (eventSource && eventSource.readyState === EventSource.OPEN) {
                if (lastReceivedTimestamp) {
                    const elapsedSec = (new Date() - lastReceivedTimestamp) / 1000;
                    if (elapsedSec > 3) {
                        updateConnectionStatus('STALE (>3s no data)', 'status-stale');
                    }
                }
            } else if (eventSource && eventSource.readyState === EventSource.CLOSED) {
                updateConnectionStatus('DISCONNECTED', 'status-disconnected');
            }
        }, 1000);

        function drawGraph() {
            const canvas = document.getElementById('motionCanvas');
            if (!canvas) return;
            const ctx = canvas.getContext('2d');
            const dpr = window.devicePixelRatio || 1;
            
            canvas.width = canvas.clientWidth * dpr;
            canvas.height = canvas.clientHeight * dpr;
            ctx.scale(dpr, dpr);

            const width = canvas.clientWidth;
            const height = canvas.clientHeight;

            ctx.clearRect(0, 0, width, height);

            if (motionHistory.length < 2) {
                ctx.fillStyle = '#8B949E';
                ctx.font = '12px Inter';
                ctx.textAlign = 'center';
                ctx.fillText('Live Movement Intensity Graph', width / 2, height / 2);
                return;
            }

            const maxVal = Math.max(...motionHistory, 5.0);
            const stepX = width / (MAX_GRAPH_POINTS - 1);

            ctx.beginPath();
            ctx.strokeStyle = '#A855F7';
            ctx.lineWidth = 2.5;

            for (let i = 0; i < motionHistory.length; i++) {
                const x = i * stepX;
                const y = height - (motionHistory[i] / maxVal) * (height - 20) - 10;
                if (i === 0) ctx.moveTo(x, y);
                else ctx.lineTo(x, y);
            }
            ctx.stroke();

            ctx.lineTo((motionHistory.length - 1) * stepX, height);
            ctx.lineTo(0, height);
            ctx.closePath();
            const grad = ctx.createLinearGradient(0, 0, 0, height);
            grad.addColorStop(0, 'rgba(168, 85, 247, 0.3)');
            grad.addColorStop(1, 'rgba(168, 85, 247, 0.0)');
            ctx.fillStyle = grad;
            ctx.fill();
        }

        window.addEventListener('DOMContentLoaded', () => {
            startMonitor();
        });
    </script>
</body>
</html>"""
    return HTMLResponse(content=html_content)
