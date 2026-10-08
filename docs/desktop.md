# Desktop ("Lights")

The same app as on Android, for macOS and Windows (Compose Desktop). Linux works from source.

Requires: JDK 17+ to run Gradle. No Android SDK needed.

## Run from source (any OS)

```zsh
./gradlew :desktopApp:run          # Windows: gradlew.bat :desktopApp:run
```

## Build an installer

Installers can only be built **on their own OS** (macOS → `.dmg`, Windows → `.msi`/`.exe`).
Packaging uses a full JDK 21 with `jpackage`; Gradle downloads it once if you don't have one.

| OS | Command | Output |
|---|---|---|
| macOS | `./gradlew :desktopApp:packageDmg` | `desktopApp/build/compose/binaries/main/dmg/Lights-1.0.0.dmg` |
| Windows | `gradlew.bat :desktopApp:packageMsi` | `desktopApp\build\compose\binaries\main\msi\Lights-1.0.0.msi` |
| Windows (alt.) | `gradlew.bat :desktopApp:packageExe` | `desktopApp\build\compose\binaries\main\exe\Lights-1.0.0.exe` |

The installer contains its own Java runtime: users don't need Java.

For a new version, raise `packageVersion` (`MAJOR.MINOR.BUILD`) in `desktopApp/build.gradle.kts`.
On Windows, a higher version upgrades (replaces) the installed one.

## Install

**macOS**
1. Open the `.dmg`, drag **Lights** to **Applications**.
2. The app isn't signed by Apple, so the first launch is blocked: **right-click Lights → Open → Open**
   (or System Settings → Privacy & Security → **Open Anyway**). Only needed once.

**Windows**
1. Run the `.msi` (or `.exe`). It installs for the current user only (no admin rights) and adds a
   Start menu entry and a desktop shortcut.
2. The installer isn't signed, so SmartScreen may warn: **More info → Run anyway**.

Uninstall: macOS, delete the app from Applications; Windows, Settings → Apps → Lights → Uninstall.

## First run: Home Assistant setup

Choose **Enter details manually** and enter the HA URL (e.g. `http://192.168.1.10:8123`) and a
long-lived access token. QR scanning isn't available on desktop. **Use demo mode** works without HA.

Settings, including the token, are stored (readable by your user only) in:

- macOS / Linux: `~/.mylightscontroller/`
- Windows: `%APPDATA%\Lights\`

Delete that folder to start over.

Optional environment variables (defaults for the setup screen, mostly for development):
`HA_URL`, `HA_TOKEN`, `PORCUPINE_API_KEY`; `MYLIGHTS_DEBUG=true` shows debug options.

## Tests

```zsh
./gradlew :desktopApp:test
```

Not available on desktop yet: voice control, QR scanning, translation.

