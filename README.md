# LightsController

An Android app to control Home Assistant devices via voice commands. 

## Supports:

- Hotword detection and voice command parsing
- Turning devices on/off
- Adjusting brightness (dim/brighten)
- Stop/cancel commands
- Integration with Home Assistant API

## Features

- Foreground service keeps listening for voice commands
- CommandParser handles intent, device, and brightness extraction
- Kotlin + Jetpack Compose UI for device control
- Configurable via build-time constants (not committed to source control)
- Modular design: `services`, `ha`, `ui`, `utils` packages

## Setup
1) Clone repo
2) Add system environment variables that will be used to populate the configuration - HA_URL, HA_TOKEN, PORCUPINE_API_KEY
3) Open the project in Android Studio and build.

## License

© 2026 Doron Yakovlev Golani. All rights reserved.  
This software is private and may not be copied, modified, distributed, or used for any purpose without explicit permission from the author.
