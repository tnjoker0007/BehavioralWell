# BehavioralWell — Automatic Host-Side USB Bridge Watcher
# Continuously monitors USB ADB device state and automatically establishes ADB reverse port forwarding (tcp:8000 -> tcp:8000)

$adbExe = "adb"
if (-not (Get-Command adb -ErrorAction SilentlyContinue)) {
    $sdkAdb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
    if (Test-Path $sdkAdb) {
        $adbExe = $sdkAdb
    }
}

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "BEHAVIORALWELL AUTOMATIC USB BRIDGE WATCHER" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "ADB Executable          : $adbExe" -ForegroundColor Gray
Write-Host "Backend Endpoint Target : 127.0.0.1:8000" -ForegroundColor Yellow
Write-Host "Monitoring Interval      : 2 seconds" -ForegroundColor Gray
Write-Host "Press Ctrl+C to stop the watcher." -ForegroundColor Gray
Write-Host "------------------------------------------------" -ForegroundColor Gray

$configuredDevices = @{}

while ($true) {
    try {
        $adbLines = & $adbExe devices 2>&1
        $currentDevices = @{}

        foreach ($line in $adbLines) {
            $lineStr = "$line".Trim()
            if ($lineStr.Length -gt 0 -and -not $lineStr.StartsWith("List of devices")) {
                $parts = $lineStr -split "\s+"
                if ($parts.Count -ge 2 -and $parts[1] -eq "device") {
                    $devId = $parts[0].Trim()
                    $currentDevices[$devId] = $true

                    $listOutput = & $adbExe -s $devId reverse --list 2>&1
                    $listStr = [string]($listOutput -join " ")
                    $hasReverse = $listStr.Contains("tcp:8000 tcp:8000")

                    if (-not $configuredDevices.ContainsKey($devId) -or -not $hasReverse) {
                        $ts = Get-Date -Format "HH:mm:ss"
                        Write-Host "[$ts] Android USB device detected: $devId" -ForegroundColor Green
                        Write-Host "[$ts] Establishing reverse tcp:8000 -> tcp:8000 for $devId" -ForegroundColor Yellow
                        
                        & $adbExe -s $devId reverse tcp:8000 tcp:8000 2>&1 | Out-Null
                        $verifyOutput = & $adbExe -s $devId reverse --list 2>&1
                        $verifyStr = [string]($verifyOutput -join " ")

                        if ($verifyStr.Contains("tcp:8000 tcp:8000")) {
                            Write-Host "[$ts] SUCCESS: Reverse tunnel active for $devId" -ForegroundColor Green
                            $configuredDevices[$devId] = $true
                        } else {
                            Write-Host "[$ts] ERROR: Failed to set reverse tunnel for $devId" -ForegroundColor Red
                        }
                    }
                }
            }
        }

        $knownKeys = @($configuredDevices.Keys)
        foreach ($knownDev in $knownKeys) {
            if (-not $currentDevices.ContainsKey($knownDev)) {
                $ts = Get-Date -Format "HH:mm:ss"
                Write-Host "[$ts] Device disconnected: $knownDev" -ForegroundColor Yellow
                $configuredDevices.Remove($knownDev)
            }
        }
    } catch {
        $ts = Get-Date -Format "HH:mm:ss"
        Write-Host "[$ts] Error scanning ADB devices: $_" -ForegroundColor Red
    }

    Start-Sleep -Seconds 2
}
