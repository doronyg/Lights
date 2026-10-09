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

## Platforms

| Platform | Module | Guide |
|---|---|---|
| Android | `:app` | [docs/android.md](docs/android.md) |
| iOS (iPhone, iPad) | `iosApp/` + `:shared` | [docs/ios.md](docs/ios.md) |
| Desktop: macOS, Windows ("Lights") | `:desktopApp` | [docs/desktop.md](docs/desktop.md) |
| Web (Kotlin/Wasm), development | `:webApp` | [docs/web.md](docs/web.md) |
| Web on a NAS (Docker gateway) | `:webApp` + `:gateway` | [docs/nas.md](docs/nas.md) |

UI and logic are shared by all platforms in `:shared` (Kotlin Multiplatform + Compose Multiplatform).
Prerequisites, and how each app gets the Home Assistant secrets: [docs/README.md](docs/README.md).

## Notes

- Configuration secrets are supplied locally and are not committed to source control.
- Translation support is best-effort and performed on-device.

## License

© 2026 Doron Yakovlev Golani. All rights reserved.
This software is private and may not be copied, modified, distributed, or used for any purpose without explicit permission from the author.
