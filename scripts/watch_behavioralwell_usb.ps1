# BehavioralWell — Automatic Host-Side USB Bridge Watcher
# Continuously monitors USB ADB device state and automatically establishes ADB reverse port forwarding (tcp:8000 -> tcp:8000)

if (-not (Get-Command adb -ErrorAction SilentlyContinue)) {
    $sdkAdb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools"
    if (Test-Path "$sdkAdb\adb.exe") {
        $env:PATH = "$sdkAdb;$env:PATH"
    }
}

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "BEHAVIORALWELL AUTOMATIC USB BRIDGE WATCHER" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "Backend Endpoint Target : 127.0.0.1:8000" -ForegroundColor Yellow
Write-Host "Monitoring Interval      : 2 seconds" -ForegroundColor Gray
Write-Host "Press Ctrl+C to stop the watcher." -ForegroundColor Gray
Write-Host "------------------------------------------------" -ForegroundColor Gray

$configuredDevices = @{}

while ($true) {
    try {
        $adbOutput = adb devices 2>&1
        $currentDeviceMatches = $adbOutput | Select-String -Pattern "^\s*([^\s]+)\s+device\s*$"
        $currentDevices = @{}

        if ($currentDeviceMatches) {
            foreach ($match in $currentDeviceMatches) {
                $devId = $match.Matches[0].Groups[1].Value.Trim()
                $currentDevices[$devId] = $true

                # Check if device is new or needs reverse re-established
                $listOutput = adb -s $devId reverse --list 2>&1
                $hasReverse = $listOutput -match "tcp:8000 tcp:8000"

                if (-not $configuredDevices.ContainsKey($devId) -or -not $hasReverse) {
                    Write-Host "[$(Get-Date -Format 'HH:mm:ss')] 📱 New or reconnected Android USB device detected: $devId" -ForegroundColor Green
                    Write-Host "[$(Get-Date -Format 'HH:mm:ss')] 🔄 Executing: adb -s $devId reverse tcp:8000 tcp:8000" -ForegroundColor Yellow
                    
                    adb -s $devId reverse tcp:8000 tcp:8000 2>&1 | Out-Null
                    $verifyOutput = adb -s $devId reverse --list 2>&1

                    if ($verifyOutput -match "tcp:8000 tcp:8000") {
                        Write-Host "[$(Get-Date -Format 'HH:mm:ss')] ✅ Reverse tunnel established successfully for $devId (127.0.0.1:8000)" -ForegroundColor Green
                        $configuredDevices[$devId] = $true
                    } else {
                        Write-Host "[$(Get-Date -Format 'HH:mm:ss')] ❌ Failed to establish reverse tunnel for $devId" -ForegroundColor Red
                    }
                }
            }
        }

        # Check for disconnected devices
        $disconnectedList = @()
        foreach ($knownDev in $configuredDevices.Keys) {
            if (-not $currentDevices.ContainsKey($knownDev)) {
                $disconnectedList += $knownDev
            }
        }

        foreach ($discDev in $disconnectedList) {
            Write-Host "[$(Get-Date -Format 'HH:mm:ss')] 🔌 Device disconnected: $discDev" -ForegroundColor Yellow
            $configuredDevices.Remove($discDev)
        }

    } catch {
        Write-Host "[$(Get-Date -Format 'HH:mm:ss')] ⚠️ Error scanning ADB devices: $_" -ForegroundColor Red
    }

    Start-Sleep -Seconds 2
}
