param(
  [string]$Tv = $(if ($env:TV_HOST) { $env:TV_HOST } else { throw "Set TV_HOST (e.g. 192.168.1.50:5555) or pass -Tv" }),
  [string]$Adb = "C:\platform-tools\adb.exe",
  [int]$Seconds = 60
)

# Keeps a Fire TV reachable over ADB during a work session. Fire OS drops network ADB when it sleeps, so every
# $Seconds this reconnects and sends KEYCODE_WAKEUP, which does nothing on screen when the TV is already awake but
# resets its sleep timer. Stop with Ctrl+C (or stop the background task); nothing on the TV is changed.
while ($true) {
  $t = Get-Date -Format HH:mm:ss
  $c = (& $Adb connect $Tv 2>&1) -join ' '
  $w = (& $Adb -s $Tv shell input keyevent KEYCODE_WAKEUP 2>&1) -join ' '
  if ($LASTEXITCODE -eq 0) { Write-Output "$t ok" } else { Write-Output "$t FAIL: $c $w" }
  Start-Sleep -Seconds $Seconds
}
