# Visual Regression Testing

Automated screenshot capture and pixel-by-pixel comparison tests.

## Quick Start

### Capture New Baseline

```bash
./scripts/capture-screenshots.sh
```

Captures screenshot and saves to `tests/visual_baselines/main_screen.png`.

### Compare Against Baseline

```bash
./scripts/compare-screenshots.sh
```

Compares fresh UI capture against baseline. Pass/fail based on pixel difference threshold (5%).

## How It Works

### Capture Flow
1. Build and install app + test APK
2. Launch capture test
3. Wait for test ready marker
4. Pull screenshot from device
5. Save to `tests/visual_baselines/main_screen.png`

### Comparison Flow
1. Build and install app + test APK
2. Launch comparison test (test creates ready marker when initialized)
3. Script waits for ready marker, then pushes baseline file to device
4. Test loads baseline, captures current UI
5. Test performs pixel comparison (0% diff = pass, >5% diff = fail)
6. Test logs results and passes/fails accordingly

## Files

- `scripts/capture-screenshots.sh` - Captures and saves baseline screenshot
- `scripts/compare-screenshots.sh` - Compares baseline vs current UI
- `app/src/androidTest/java/.../ScreenshotCaptureTest.kt` - Captures screenshot
- `app/src/androidTest/java/.../ScreenshotComparisonTest.kt` - Compares against baseline
- `app/src/androidTest/java/.../ScreenshotUtils.kt` - Screenshot utilities
- `tests/visual_baselines/main_screen.png` - Baseline screenshot

## Requirements

- bash, adb, gradlew
- Connected Android device or emulator
- Device with external storage enabled

