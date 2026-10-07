# Lights Controller

An Android app for controlling Home Assistant lights and devices using a modern touch UI and optional voice commands.
Built with Kotlin and Jetpack Compose.

## Features

- Device control UI with responsive grid layout
- Accessibility-aware layouts for larger system font sizes
- Voice command support with hotword detection
- Turn devices on/off
- Adjust brightness (dim/brighten)
- Stop/cancel voice commands
- Home Assistant API integration
- Onboarding flow for configuring:
  - Home Assistant URL
  - Home Assistant access token
  - Voice service keys
- Runtime translation support using on-device ML Kit translation
- Translation preload/cache system for synchronous UI rendering
- Foreground voice service for continuous listening
- Responsive layouts for phones and tablets
- Modular architecture with separated packages:
  - `ha`
  - `services`
  - `ui`
  - `utils`
  - `onboarding`

## Technical Details

- Kotlin
- Jetpack Compose
- Coroutines + StateFlow
- Google ML Kit Translation
- Home Assistant REST API
- Material 3 UI

## Desktop app ("Lights")

The `:desktopApp` module runs the shared UI and logic on macOS, Windows and Linux (Compose Desktop).

Requirements: JDK 17+ to run Gradle (Android Studio's bundled JDK is fine). The Android SDK is
optional: without it, `:app` and the Android target are skipped automatically
(force with `-Plights.android=false`).

| Task | macOS / Linux | Windows |
|---|---|---|
| Run | `./gradlew :desktopApp:run` | `gradlew.bat :desktopApp:run` |
| Tests | `./gradlew :desktopApp:test` | `gradlew.bat :desktopApp:test` |
| Installer | `./gradlew :desktopApp:packageDmg` | `gradlew.bat :desktopApp:packageMsi` (or `packageExe`) |

- Installers can only be built on their own OS (jpackage can't cross-compile).
- Packaging tasks use a full JDK 21 (with `jpackage`), which Gradle downloads once if needed.
- Output: `desktopApp/build/compose/binaries/main/<format>/`.
- Settings (including the HA token): `~/.mylightscontroller` on macOS/Linux, `%APPDATA%\Lights` on Windows.
- Environment variables `HA_URL`, `HA_TOKEN`, `PORCUPINE_API_KEY` provide defaults; `MYLIGHTS_DEBUG=true` enables debug options.
- Not available on desktop yet: voice control, QR scanning (use manual setup), translation.

## Notes

- Configuration secrets are supplied locally and are not committed to source control.
- Translation support is best-effort and performed on-device.

## License

© 2026 Doron Yakovlev Golani. All rights reserved.
This software is private and may not be copied, modified, distributed, or used for any purpose without explicit permission from the author.
