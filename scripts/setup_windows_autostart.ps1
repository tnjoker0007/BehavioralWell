# BehavioralWell — Optional Windows Login Auto-Start Setup
# Creates a Windows Startup shortcut so the BehavioralWell USB Watcher runs automatically on Windows login.

$PSScriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Definition
$StartupFolder = [System.Environment]::GetFolderPath([System.Environment+SpecialFolder]::Startup)
$ShortcutPath = Join-Path -Path $StartupFolder -ChildPath "BehavioralWellUSBWatcher.lnk"
$ScriptToRun = Join-Path -Path $PSScriptRoot -ChildPath "start_behavioralwell_usb.ps1"

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "OPTIONAL WINDOWS LOGIN AUTO-START SETUP" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan

$WScriptShell = New-Object -ComObject WScript.Shell
$Shortcut = $WScriptShell.CreateShortcut($ShortcutPath)
$Shortcut.TargetPath = "powershell.exe"
$Shortcut.Arguments = "-ExecutionPolicy Bypass -File `"$ScriptToRun`""
$Shortcut.WorkingDirectory = $PSScriptRoot
$Shortcut.WindowStyle = 7 # Minimized
$Shortcut.Description = "BehavioralWell USB Bridge Watcher"
$Shortcut.Save()

Write-Host "✅ Created Windows Startup Shortcut:" -ForegroundColor Green
Write-Host "   Path: $ShortcutPath" -ForegroundColor Gray
Write-Host "   Target: powershell.exe -ExecutionPolicy Bypass -File `"$ScriptToRun`"" -ForegroundColor Gray
Write-Host ""
Write-Host "Any physical Android phone connected via USB will now automatically get ADB reverse forwarding whenever you log in!" -ForegroundColor Green
