# Android

Requires: JDK 17+, **Android SDK** (install Android Studio, or set `ANDROID_HOME`), a device or
emulator with Android 6.0+ (API 23).

## Build & install (debug)

Easiest: open the project in **Android Studio**, pick the `app` run configuration and a device, press **Run**.

From the command line:

```zsh
./gradlew :app:assembleDebug      # -> app/build/outputs/apk/debug/app-debug.apk
./gradlew :app:installDebug       # build + install on the connected device/emulator
```

Or install the APK yourself:

```zsh
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

On the phone: enable **Developer options → USB debugging**, connect it, and accept the prompt.
To sideload without a cable, copy the APK to the phone and open it (allow "Install unknown apps"
for the file manager when asked).

## Release build (signed)

The release build is signed with your keystore, given by environment variables:

| Variable | |
|---|---|
| `RELEASE_STORE_FILE` | Path to the keystore (`.jks`) |
| `RELEASE_STORE_PASSWORD` | Keystore password |
| `RELEASE_KEY_ALIAS` | Key alias |
| `RELEASE_KEY_PASSWORD` | Key password |

Create a keystore once (keep it and its passwords safe; you need the same key for every update):

```zsh
keytool -genkeypair -v -keystore ~/keys/lights.jks -alias lights -keyalg RSA -keysize 4096 -validity 10000
```

Build:

```zsh
export RELEASE_STORE_FILE=~/keys/lights.jks RELEASE_KEY_ALIAS=lights
read -rs "RELEASE_STORE_PASSWORD?Store password: "; export RELEASE_STORE_PASSWORD; echo
read -rs "RELEASE_KEY_PASSWORD?Key password: "; export RELEASE_KEY_PASSWORD; echo
./gradlew :app:assembleRelease    # -> app/build/outputs/apk/release/app-release.apk
```

Before releasing a new version, raise `versionCode` (and `versionName`) in `app/build.gradle.kts`,
otherwise Android won't install it over the old one.

## First run: Home Assistant setup

The onboarding screen offers:

- **Enter details manually**: HA URL (e.g. `http://192.168.1.10:8123`), long-lived access token,
  and optionally a Porcupine API key (for the voice hotword).
- **Scan QR code**: on a device that's already set up, open **Settings → Show Setup QR** and scan it.
- **Use demo mode**: sample devices, no Home Assistant needed.

The phone must be able to reach Home Assistant (same Wi-Fi, or your remote HA address).

## Tests

```zsh
./gradlew :shared:testAndroidHostTest       # shared unit tests on the JVM
./gradlew :app:connectedDebugAndroidTest    # instrumentation tests (device/emulator needed)
```

