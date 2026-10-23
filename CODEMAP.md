<!-- built-at: 357c7591d987ebd7c3c6f1d6c6ab491b954be2d3 -->
<!-- updated: 2026-10-22 -->
# CODEMAP

## Overview
- Debloat an Android TV / Fire TV over ADB and run an Apple TV-style launcher ("My Home") on top of Amazon's protected home.
- Java Android app (Gradle, minSdk 26, targetSdk 29) + PowerShell (Windows) and bash/Python (macOS/Linux) tooling.
- Runs: build APK with `launcher/build-and-install.ps1|.sh`, sideload over network ADB; scripts talk to the TV via adb or `tools/tv.py`.

## Entry points
- `launcher/app/.../MainActivity.java`: the home screen (HOME + LEANBACK_LAUNCHER).
- `launcher/app/.../HomeRedirectService.java`: accessibility service that replaces Amazon's home on Fire OS.
- `launcher/build-and-install.ps1` (Windows) / `launcher/build-and-install.sh` (bash): build + install + enable redirect.
- `tools/debloat-loop.ps1`, `tools/fire-check.ps1`, `tools/fire-home-off.ps1`, `tools/keepalive.ps1` (Windows); `tools/disable.sh`, `tools/restore.sh`, `tools/tv.py` (bash/Python).

## Data flow
- Home pressed → Amazon `com.amazon.tv.launcher` HomeActivity window → TYPE_WINDOW_STATE_CHANGED → HomeRedirectService.
- HomeRedirectPolicy decides (settings grace, dedupe, loop guard) → service finds tile labelled app_name in Amazon's apps row → ACTION_CLICK → Amazon starts MainActivity (~0.6 s).
- Tile missing → black TYPE_ACCESSIBILITY_OVERLAY + startActivity (Android 9 delays it ~5 s after Home).
- Boot: onServiceConnected opens MainActivity; boot guard (3 min, until first key) bounces self-starting apps under a 3 s cover.
- MainActivity → AppAdapter → dock_apps (config.xml) → one row of banner tiles → launch app.
- Debloat: list file → fire-lib Read-DebloatList (never-list filter) → `pm disable-user` one by one → health check → restore line in `backup/restore.ps1`.

## Config surface
- Env: `TV_HOST` (TV ip:port, required by .ps1 scripts and tv.py), `JAVA_HOME`, `ANDROID_HOME`.
- Gradle: `-PlauncherApplicationId` (default `com.andrewsaad.myhome`; Java namespace stays `com.gathin.tvhome`).
- `launcher/local.properties` (sdk.dir, optional `tvhome.userName`), git-ignored.
- `res/values/config.xml` (dock_apps, columns), `res/values/strings.xml` (app_name must stay unique: tile matched by exact label).
- TV secure settings: `enabled_accessibility_services`, `accessibility_enabled` (written by scripts / setEnabled).
- No secrets in the repo; `backup/` is git-ignored.

## Boundaries
- Never disable or touch `com.amazon.tv.launcher` / `firehomestarter` (protected; hosts Settings). Never-list lives in `tools/fire-lib.ps1`.
- HomeRedirectPolicy and AccessibilityServiceList stay pure Java (no android.*) so JUnit runs on the JVM.
- Components use full class names: `<appId>/com.gathin.tvhome.MainActivity`, never `<appId>/.MainActivity`.
- `examples/*/debloat.txt` are per-device data; scripts take `-ListFile`.
- Tests: `./gradlew testDebugUnitTest lintDebug` (Git Bash; gradlew.bat fails here), `Invoke-Pester tests`.

Exhaustive per-file map: CODEMAP_FULL.jsonl, keyed by path — look up with `node ~/.claude/scripts/doc-lookup.js CODEMAP_FULL.jsonl <path>`, never read the whole file.
