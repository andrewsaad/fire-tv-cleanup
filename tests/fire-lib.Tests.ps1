$root = Split-Path -Parent $PSScriptRoot
. "$root\tools\fire-lib.ps1"

Describe 'Update-A11yList' {
  $c = 'com.andrewsaad.myhome/com.gathin.tvhome.HomeRedirectService'
  It 'adds to null' { Update-A11yList 'null' -Add $c | Should Be $c }
  It 'adds to empty' { Update-A11yList '' -Add $c | Should Be $c }
  It 'keeps others and appends' { Update-A11yList 'a/b:c/d' -Add $c | Should Be "a/b:c/d:$c" }
  It 'is idempotent' { Update-A11yList "a/b:$c" -Add $c | Should Be "a/b:$c" }
  It 'removes the old app and keeps others' {
    Update-A11yList 'a/b:com.gathin.tvhome/com.gathin.tvhome.HomeRedirectService' -Add $c -RemovePrefixes @('com.gathin.tvhome/') | Should Be "a/b:$c" }
  It 'removal can empty the list' { Update-A11yList $c -RemovePrefixes @('com.andrewsaad.myhome/') | Should Be '' }
  It 'does not remove a prefix lookalike' { Update-A11yList 'com.gathin.tvhomeX/s' -RemovePrefixes @('com.gathin.tvhome/') | Should Be 'com.gathin.tvhomeX/s' }
}

Describe 'Test-NeverPackage' {
  It 'blocks home, settings and wildcard families' {
    foreach ($p in 'com.amazon.tv.launcher', 'com.amazon.tv.settings.v2.overlay.x', 'com.amazon.tv.quicksettings.ui', 'com.droidlogic.tvinput', 'com.amazon.venezia', 'com.plexapp.android') {
      Test-NeverPackage $p | Should Be $true } }
  It 'blocks the app id passed in' { Test-NeverPackage 'x.y.z' -AppId 'x.y.z' | Should Be $true }
  It 'allows ordinary bloat' { Test-NeverPackage 'com.amazon.kso.blackbird' | Should Be $false }
}

Describe 'Read-DebloatList' {
  $f = Join-Path $env:TEMP 'fire-lib-test-list.txt'
  Set-Content $f @('# header com.amazon.foo', 'com.a.one   # note', '', 'com.amazon.tv.launcher', 'com.a.one', '# com.a.three', 'bad line', 'com.droidlogic.x') -Encoding ascii
  $r = @(Read-DebloatList $f)
  It 'parses, dedupes, skips comments and never-list' { ($r -join ',') | Should Be 'com.a.one' }
  Remove-Item $f
  It 'the shipped list has no never-listed package and no optional entries' {
    $all = Get-Content "$root\examples\amazon-fire-tv-aftalmo\debloat.txt" | ForEach-Object { ($_ -split '#')[0].Trim() } | Where-Object { $_ }
    @(Read-DebloatList "$root\examples\amazon-fire-tv-aftalmo\debloat.txt").Count | Should Be @($all).Count
    @($all).Count | Should BeGreaterThan 100 }
}

Describe 'Test-ServiceBound' {
  $dump = "User state[attributes:{id=0}`n services:{Service[label=My Home redirect, feedbackType[FEEDBACK_GENERIC], capabilities=9]}]"
  It 'matches by label' { Test-ServiceBound $dump 'a/b' | Should Be $true }
  It 'does not take the app name for the service label' {
    Test-ServiceBound 'services:{Service[label=My Home, x]}' 'a/b' | Should Be $false }
  It 'matches by component' { Test-ServiceBound 'bound a/b' 'a/b' | Should Be $true }
  It 'rejects other services' { Test-ServiceBound 'services:{Service[label=Other, x]}' 'a/b' | Should Be $false }
  It 'rejects empty' { Test-ServiceBound '' 'a/b' | Should Be $false }
}
