# Visual Regression Test Suite

This directory contains automated visual regression tests for the MyLightsController UI. These tests verify that the UI renders and behaves consistently, which is especially important when migrating to Compose Multiplatform.

## Test Files

- **ScreenshotComparisonHelper.kt** - Utility for pixel-level bitmap comparison
- **MainScreenVisualTest.kt** - Tests for main screen navigation and rendering
- **SettingsScreenVisualTest.kt** - Tests for settings screen UI elements
- **DeviceRowVisualTest.kt** - Tests for individual device row components

## Running the Tests

### Prerequisites

Ensure you have:
- An Android emulator running or a physical device connected via USB
- Gradle and Android SDK configured
- USB debugging enabled (for physical devices)

### Run All Visual Tests

```bash
./gradlew connectedAndroidTest
```

### Run Specific Test Class

```bash
# Run only MainScreen tests
./gradlew connectedAndroidTest -Dorg.gradle.testng.concurrency=1 \
  -e class=com.doronyg.mylightscontroller.ui.tests.MainScreenVisualTest

# Run only SettingsScreen tests
./gradlew connectedAndroidTest -Dorg.gradle.testng.concurrency=1 \
  -e class=com.doronyg.mylightscontroller.ui.tests.SettingsScreenVisualTest

# Run only DeviceRow tests
./gradlew connectedAndroidTest -Dorg.gradle.testng.concurrency=1 \
  -e class=com.doronyg.mylightscontroller.ui.tests.DeviceRowVisualTest
```

### Run Specific Test Method

```bash
./gradlew connectedAndroidTest -Dorg.gradle.testng.concurrency=1 \
  -e class=com.doronyg.mylightscontroller.ui.tests.MainScreenVisualTest \
  -e method=testAllNavigationTabsExist
```

## Test Coverage

### MainScreenVisualTest
- ✅ Screen renders without crashing
- ✅ All 3 navigation tabs exist
- ✅ Main tab is selected by default
- ✅ Navigation between tabs works
- ✅ Settings screen elements visible after navigation
- ✅ Back navigation works
- ✅ No crashes during render

### SettingsScreenVisualTest
- ✅ Screen renders without crashing
- ✅ All settings controls visible (switches, sliders, cards)
- ✅ Voice controller service control present
- ✅ HA refresh interval control present
- ✅ Mock data toggles present
- ✅ Action cards present (QR, email export)
- ✅ Description text for all settings visible

### DeviceRowVisualTest
- ✅ Light on state renders correctly
- ✅ Light off state renders correctly
- ✅ Click behavior triggers callbacks
- ✅ Both on/off states render
- ✅ No crashes with callbacks

## Validation Workflow

### Before Migration to Compose Multiplatform

1. **Run baseline tests** on current app:
   ```bash
   ./gradlew connectedAndroidTest
   ```
   
2. **Document results**:
   - All tests passing? ✅
   - Any failures? ❌ Note them

3. **Take screenshots** (manually):
   - Launch app on emulator/device
   - Navigate through all screens
   - Toggle dark/light mode
   - Take side-by-side photos

### After Migration to Compose Multiplatform

1. **Run same tests**:
   ```bash
   ./gradlew connectedAndroidTest
   ```

2. **Compare results**:
   - All previously passing tests still pass? ✅
   - Any regressions? ❌ Fix before continuing

3. **Compare screenshots**:
   - Visual appearance identical to before?
   - Colors, spacing, fonts match?
   - Navigation behavior same?

## Common Issues

### Test hangs or times out
- Increase timeout: Add `composeTestRule.mainClock.advanceTimeBy(2000)`
- Check device isn't locked
- Verify USB connection for physical device

### UI elements not found
- Wait for UI to render: Use `composeTestRule.waitUntil { ... }`
- Check exact text match (case-sensitive)
- Verify element isn't behind overlay

### Compose test framework issues
- Ensure `androidx.compose.ui:ui-test-junit4` is installed
- Run `./gradlew --refresh-dependencies`
- Clear Gradle cache: `rm -rf ~/.gradle/caches`

## Test Behavior Details

All tests use mock data when needed:

### MainScreenVisualTest
- Uses default navigation (no data fetching)
- Tests UI state transitions
- Verifies screen switching works

### SettingsScreenVisualTest
- Uses empty callbacks (no actual settings changes)
- Tests that all UI controls are rendered
- No data persistence during tests

### DeviceRowVisualTest
- Uses hardcoded mock light devices:
  - `Bedroom Light` (on, brightness 200)
  - `Kitchen Light` (off, no brightness)
- Tests rendering and click behavior
- No network calls

## Integration with CI/CD

To run tests in CI pipeline:

```yaml
# Example GitHub Actions
- name: Run Visual Regression Tests
  run: |
    ./gradlew connectedAndroidTest \
      -DtestBuildType=debug \
      --info
```

## Future Enhancements

- [ ] Screenshot capture and comparison
- [ ] Performance benchmarking
- [ ] Accessibility tree validation
- [ ] Dark mode automated testing
- [ ] Landscape orientation testing
- [ ] Multi-device testing (phones, tablets)

## Notes

- Tests are designed to be deterministic (same result every run)
- No external dependencies (no network calls in tests)
- All UI state is local and self-contained
- Tests verify rendering, not business logic

