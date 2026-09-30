#!/bin/bash

# Simple Visual Regression Test - Screenshot Capture Only
#
# Captures screenshots from device test
# Uses Gradle to build and run tests automatically
#
# Usage:
#   ./scripts/capture-screenshots.sh

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
APP_PACKAGE="com.doronyg.mylightscontroller"
EXTERNAL_DIR="/sdcard/Android/data/$APP_PACKAGE/files/Download"
SCREENSHOT_FILE="$EXTERNAL_DIR/screenshots/main_screen.png"
MARKER_FILE="$EXTERNAL_DIR/ready.marker"
STOP_SIGNAL="$EXTERNAL_DIR/stop.marker"
OUTPUT_DIR="tests/visual_baselines/actual"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[✓]${NC} $1"
}

log_error() {
    echo -e "${RED}[✗]${NC} $1"
}

# Create output directory
mkdir -p "$OUTPUT_DIR"

log_info "Building and running screenshot capture test..."
echo ""

# Change to project root
cd "$PROJECT_ROOT"

# Get device
DEVICE=$(adb devices | grep -v "^List" | grep "device$" | awk '{print $1}' | head -1)
if [ -z "$DEVICE" ]; then
    log_error "No connected devices found"
    exit 1
fi

log_info "Device: $DEVICE"
log_info "Saving screenshots to: $EXTERNAL_DIR"
echo ""

# Run test via Gradle (builds, installs, and runs)
log_info "Building and running ScreenshotCaptureTest..."
log_info "Build log: /tmp/test.log"
echo ""

./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.doronyg.mylightscontroller.ui.tests.ScreenshotCaptureTest#test_CaptureMainScreenshot 2>&1 | tee /tmp/test.log &
TEST_PID=$!

log_info "Gradle process started (PID: $TEST_PID)"

# Wait for test to initialize
sleep 3

# Check if gradle process is still running
if ! kill -0 $TEST_PID 2>/dev/null; then
    log_error "Gradle process failed to start or crashed immediately!"
    log_error "Build error:"
    tail -50 /tmp/test.log
    exit 1
fi

echo ""

# Poll for screenshot marker
log_info "Waiting for screenshot ready marker (max 70 seconds)..."
ELAPSED=0
TIMEOUT=70
FOUND=false

while [ $ELAPSED -lt $TIMEOUT ]; do
    if adb -s "$DEVICE" shell "test -f $MARKER_FILE" 2>/dev/null; then
        log_success "Ready marker detected!"
        FOUND=true
        break
    fi

    PROGRESS=$((ELAPSED * 100 / TIMEOUT))
    echo -n "."

    sleep 2
    ELAPSED=$((ELAPSED + 2))
done

echo ""

if [ "$FOUND" = false ]; then
    log_error "Timeout waiting for screenshot marker"
    log_error "Test output:"
    tail -50 /tmp/test.log
    exit 1
fi

# Pull screenshot
log_info "Pulling screenshot from device..."
adb -s "$DEVICE" pull "$SCREENSHOT_FILE" "$OUTPUT_DIR/" > /dev/null 2>&1 || {
    log_error "Failed to pull screenshot"
    exit 1
}

if [ ! -f "$OUTPUT_DIR/main_screen.png" ]; then
    log_error "Screenshot file not found after pull"
    exit 1
fi

log_success "Screenshot saved: $OUTPUT_DIR/main_screen.png"
echo ""

# Send stop signal
log_info "Sending stop signal to test..."
adb -s "$DEVICE" shell "touch $STOP_SIGNAL" 2>/dev/null || {
    log_error "Failed to send stop signal"
    exit 1
}

log_success "Stop signal sent"
echo ""

# Wait for test to finish
log_info "Waiting for gradle test process to complete..."
if ! wait $TEST_PID; then
    GRADLE_EXIT=$?
    log_error "Gradle process failed with exit code: $GRADLE_EXIT"
    log_error "Check build log for details:"
    tail -50 /tmp/test.log
    exit 1
fi

log_success "Capture complete!"
echo ""
echo "Screenshot saved to: $OUTPUT_DIR/main_screen.png"











