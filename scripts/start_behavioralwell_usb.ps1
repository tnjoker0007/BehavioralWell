# BehavioralWell — Single-Command USB Development Starter
# Ensures FastAPI backend is active and launches the automatic host-side USB Bridge Watcher.

$PSScriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Definition
$BackendPort = 8000
$PortCheck = Get-NetTCPConnection -LocalPort $BackendPort -ErrorAction SilentlyContinue

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "BEHAVIORALWELL USB DEVELOPMENT STARTER" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan

if (-not $PortCheck) {
    Write-Host "⚠️  FastAPI Backend is not running on port $BackendPort." -ForegroundColor Yellow
    Write-Host "Starting FastAPI Backend server..." -ForegroundColor Gray
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PSScriptRoot\..\backend'; python -m uvicorn app.main:app --host 0.0.0.0 --port 8000" -WindowStyle Normal
    Start-Sleep -Seconds 3
} else {
    Write-Host "✅ FastAPI Backend is already active on port $BackendPort (reusing existing process)." -ForegroundColor Green
}

# Verify backend health
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8000/docs" -UseBasicParsing -TimeoutSec 3 -ErrorAction Stop
    Write-Host "✅ FastAPI Backend Health Check: HTTP 200 OK" -ForegroundColor Green
} catch {
    Write-Host "⚠️  FastAPI Backend health check pending or starting..." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Launching Automatic USB Bridge Watcher..." -ForegroundColor Yellow
& "$PSScriptRoot\watch_behavioralwell_usb.ps1"
