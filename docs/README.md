# Build & install guides

| Guide | What you get | Where it runs |
|---|---|---|
| [Android](android.md) | APK for phones/tablets | Android 6.0+ (API 23) |
| [Desktop](desktop.md) | "Lights" app: `.dmg` (macOS), `.msi` / `.exe` (Windows) | macOS, Windows (Linux: run from source) |
| [Web, development](web.md) | Web app in a local dev server, demo mode | Your computer's browser |
| [Web on a NAS](nas.md) | Web app + gateway in Docker (Synology Container Manager) | Any browser on your home network |

## Common prerequisites

- **Git** and this repository.
- **JDK 17 or newer** to run Gradle. Android Studio's bundled JDK is fine
  (`JAVA_HOME` = `/Applications/Android Studio.app/Contents/jbr/Contents/Home` on macOS,
  `C:\Program Files\Android\Android Studio\jbr` on Windows).
- Use `./gradlew` on macOS/Linux and `gradlew.bat` (or `.\gradlew.bat` in PowerShell) on Windows.
- The **Android SDK is only needed for the Android app**. Without it, Gradle skips `:app` and the
  Android target automatically (force with `-Plights.android=false`).
- The first build downloads Gradle, dependencies and (for the web app) Node.js. Behind a corporate
  VPN/proxy, downloads may fail with SSL errors: turn the VPN off for the first build.

## How the apps get the Home Assistant secrets

| App | Where the HA URL + token are entered | Where they are stored |
|---|---|---|
| Android | In the app: onboarding (manual or QR scan) | App preferences on the device |
| Desktop | In the app: onboarding (manual) | `~/.mylightscontroller` (macOS/Linux), `%APPDATA%\Lights` (Windows), owner-only |
| Web (NAS) | First-run setup page, once, by the first user | On the NAS (`data/ha.properties`, mode 600); never in the browser |
| Web (dev) | `HA_URL` / `HA_TOKEN` environment variables | Your shell only |

**To create a Home Assistant token:**

1. Open Home Assistant and click your profile/user icon (bottom left).
2. Scroll to **Long-Lived Access Tokens**.
3. Click **Create Token**, give it a name (e.g. "Lights"), and click **Create**.
4. Copy the token immediately and save it somewhere safe (it's shown only once).
5. Secrets are never committed to the repository.

## Project modules

| Module | |
|---|---|
| `:shared` | UI and logic shared by all apps (Kotlin Multiplatform + Compose Multiplatform) |
| `:app` | Android app |
| `iosApp/` | iOS app (Xcode project hosting the `Shared` framework from `:shared`) |
| `:desktopApp` | Desktop app (Compose Desktop) |
| `:webApp` | Web app (Kotlin/Wasm) |
| `:gateway` | Server for the web app on a NAS (Ktor) |

Screenshot tests: see [VISUAL_REGRESSION.md](../VISUAL_REGRESSION.md).

