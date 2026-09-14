# BehavioralWell — Single-Command USB Development Setup
# Ensures FastAPI backend is active and configures ADB reverse for all connected Android devices.

$PSScriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Definition
$BackendPort = 8000
$PortCheck = Get-NetTCPConnection -LocalPort $BackendPort -ErrorAction SilentlyContinue

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "BEHAVIORALWELL USB DEVELOPMENT STARTER" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan

if (-not $PortCheck) {
    Write-Host "⚠️  FastAPI Backend is not running on port $BackendPort." -ForegroundColor Yellow
    Write-Host "Starting FastAPI Backend server..." -ForegroundColor Gray
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PSScriptRoot\..\backend'; python run_backend.py" -WindowStyle Normal
    Start-Sleep -Seconds 3
} else {
    Write-Host "✅ FastAPI Backend is active on port $BackendPort." -ForegroundColor Green
}

# Run device connection bridge
& "$PSScriptRoot\connect_android_devices.ps1"
