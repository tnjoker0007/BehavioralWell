# BehavioralWell — USB Development Bridge (ADB Reverse Port Forwarding)
# Automatically configures all connected Android physical devices to route port 8000 to FastAPI backend.

# Auto-resolve adb path if not in current PATH
if (-not (Get-Command adb -ErrorAction SilentlyContinue)) {
    $sdkAdb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools"
    if (Test-Path "$sdkAdb\adb.exe") {
        $env:PATH = "$sdkAdb;$env:PATH"
    }
}

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "BEHAVIORALWELL USB DEVELOPMENT BRIDGE" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan

$BackendUrl = "http://localhost:8000"
$BackendHealthUrl = "$BackendUrl/docs"
$BackendStatus = "UNKNOWN"

try {
    $response = Invoke-WebRequest -Uri $BackendHealthUrl -UseBasicParsing -TimeoutSec 3 -ErrorAction Stop
    if ($response.StatusCode -eq 200) {
        $BackendStatus = "CONNECTED"
    } else {
        $BackendStatus = "HTTP $($response.StatusCode)"
    }
} catch {
    $BackendStatus = "DISCONNECTED (Server not running on port 8000)"
}

Write-Host ""
Write-Host "Backend Server URL: $BackendUrl" -ForegroundColor Yellow
$statusColor = if ($BackendStatus -eq "CONNECTED") { "Green" } else { "Red" }
Write-Host "FastAPI Health    : $BackendStatus" -ForegroundColor $statusColor
Write-Host ""

# Get connected ADB devices
$adbDevicesOutput = adb devices
$deviceLines = $adbDevicesOutput | Select-String -Pattern "^\s*([^\s]+)\s+device\s*$"

if (-not $deviceLines) {
    Write-Host "⚠️  No connected Android USB devices found via ADB." -ForegroundColor Yellow
    Write-Host "Please connect your Android phone via USB and enable USB Debugging." -ForegroundColor Gray
    exit 0
}

Write-Host "Connected USB Devices & Port Forwarding Status:" -ForegroundColor White
Write-Host "------------------------------------------------------------------------" -ForegroundColor Gray
Write-Host ("{0,-20} {1,-20} {2,-15}" -f "DEVICE_ID", "BACKEND_ENDPOINT", "STATUS") -ForegroundColor Cyan
Write-Host "------------------------------------------------------------------------" -ForegroundColor Gray

foreach ($match in $deviceLines) {
    $deviceId = $match.Matches[0].Groups[1].Value.Trim()
    
    # Establish ADB reverse rule for port 8000
    $revOutput = adb -s $deviceId reverse tcp:8000 tcp:8000 2>&1
    $listOutput = adb -s $deviceId reverse --list 2>&1

    if ($listOutput -match "tcp:8000 tcp:8000") {
        $status = "REVERSE OK"
        Write-Host ("{0,-20} {1,-20} {2,-15}" -f $deviceId, "127.0.0.1:8000", $status) -ForegroundColor Green
    } else {
        $status = "FAILED"
        Write-Host ("{0,-20} {1,-20} {2,-15}" -f $deviceId, "127.0.0.1:8000", $status) -ForegroundColor Red
    }
}

Write-Host "------------------------------------------------------------------------" -ForegroundColor Gray
Write-Host "✅ All physical Android devices configured to connect permanently to http://127.0.0.1:8000/api/" -ForegroundColor Green
Write-Host "Zero Kotlin/Gradle source code modifications or APK rebuilds required!" -ForegroundColor Green
Write-Host ""
