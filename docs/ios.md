# iOS app

The `iosApp/` Xcode project is a thin SwiftUI host around the shared Compose Multiplatform UI.
The UI and logic are the Kotlin `Shared` framework from `:shared`
(iOS code in `shared/src/iosMain`). Xcode builds that framework with Gradle in a pre-build step.

## Prerequisites

- **A Mac with Xcode** (tested with Xcode 26). Kotlin/Native uses Xcode's toolchain and iOS SDKs.
- **JDK 17+** for Gradle (see [common prerequisites](README.md#common-prerequisites)). Xcode
  doesn't inherit your shell's `JAVA_HOME`; the build script falls back to Android Studio's JDK.
- Optional: **XcodeGen** (`brew install xcodegen`), only to regenerate the Xcode project after
  editing `iosApp/project.yml`.

The first build downloads the Kotlin/Native toolchain (a few hundred MB) and takes several minutes.

## Run in the simulator

1. Open `iosApp/iosApp.xcodeproj` in Xcode.
2. Pick the **Lights** scheme and an iPhone simulator.
3. **Run** (⌘R).

From the command line:

```sh
./gradlew :shared:compileKotlinIosSimulatorArm64     # just check that the Kotlin code compiles
xcodebuild -project iosApp/iosApp.xcodeproj -scheme Lights \
  -destination 'generic/platform=iOS Simulator' build
```

## Run on an iPhone

1. In Xcode: target **Lights** → **Signing & Capabilities** → choose your **Team**
   (a free Apple ID works for your own devices; the app then expires after 7 days).
2. If the bundle ID `com.doronyg.mylightscontroller.ios` is taken, change it there.
3. Connect the iPhone, enable Developer Mode on it (Settings → Privacy & Security), select it, Run.

To keep your team ID out of git, set it in Xcode only and don't commit `project.pbxproj`, or set
`DEVELOPMENT_TEAM` in `project.yml` locally.

## Home Assistant secrets

Same as Android: entered in the app (onboarding → manual setup, or demo mode) and stored in the
app's preferences (`NSUserDefaults`) on the device.
Plain `http://` is allowed only for local-network addresses (e.g. `http://192.168.1.10:8123`);
use `https://` for anything else.

## What's not on iOS (yet)

| Feature | Status |
|---|---|
| QR setup scan | Not yet (would use AVFoundation); use manual setup |
| Voice control (hotword) | Android only |
| Name translation | Android only (ML Kit) |
| Export HA data | Opens the iOS share sheet (Mail, Files, AirDrop, ...) |

## Regenerating the Xcode project

The `.xcodeproj` is generated from `iosApp/project.yml` and committed so that XcodeGen isn't
required to build. After editing `project.yml`:

```sh
cd iosApp && xcodegen
```

The app icon comes from the Android Play Store icon:
`swift scripts/make_ios_icon.swift app/src/main/ic_bee_launcher-playstore.png iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/AppIcon.png`.

