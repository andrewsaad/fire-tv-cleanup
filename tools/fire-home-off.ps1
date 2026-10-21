param(
  [string]$Tv = $(if ($env:TV_HOST) { $env:TV_HOST } else { throw "Set TV_HOST (e.g. 192.168.1.50:5555) or pass -Tv" }),
  [string]$Adb = "C:\platform-tools\adb.exe",
  [string]$AppId = "com.andrewsaad.myhome",
  [switch]$Uninstall,
  [switch]$DryRun
)

# Turns the My Home redirect off (Home button goes back to Amazon's launcher): removes our accessibility service from
# the enabled list, keeping other services, and switches accessibility off if nothing is left. -Uninstall also removes the app.
. "$PSScriptRoot\fire-lib.ps1"
$key = 'enabled_accessibility_services'
$cur = (Adb-Sh "settings get secure $key") -join ''
$new = Update-A11yList $cur -RemovePrefixes @("$AppId/", "$OldAppId/")
if ($new) { Adb-Sh "settings put secure $key $new" -Mutate }
else {
  Adb-Sh "settings delete secure $key" -Mutate
  Adb-Sh "settings put secure accessibility_enabled 0" -Mutate
}
if ($Uninstall) { Adb-Sh "pm uninstall $AppId" -Mutate }
if ($DryRun) { Write-Output "[dry-run] enabled list was '$cur', would become '$new'"; exit 0 }
$after = (Adb-Sh "settings get secure $key") -join ''
if ($after -match [regex]::Escape("$AppId/")) { Write-Output "FAIL: still enabled: $after"; exit 1 }
Write-Output "Home redirect off. Enabled list now: $after"
