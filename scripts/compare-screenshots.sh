#!/bin/bash

# Visual Regression Comparison Test
# Pushes baseline to device and runs comparison test

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
APP_PACKAGE="com.doronyg.mylightscontroller"

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

cd "$PROJECT_ROOT"

# Get device
DEVICE=$(adb devices | grep -v "^List" | grep "device$" | awk '{print $1}' | head -1)
if [ -z "$DEVICE" ]; then
    log_error "No connected devices found"
    exit 1
fi

log_info "Device: $DEVICE"
echo ""

# Check if baseline exists locally
BASELINE_FILE="tests/visual_baselines/main_screen.png"
if [ ! -f "$BASELINE_FILE" ]; then
    log_error "Baseline not found: $BASELINE_FILE"
    log_error "Run capture-screenshots.sh first and approve baseline:"
    log_error "  cp tests/visual_baselines/actual/main_screen.png $BASELINE_FILE"
    exit 1
fi

log_success "Baseline found: $BASELINE_FILE"
echo ""

# Start comparison test in background (this will build and install the app)
log_info "Starting comparison test in background (will build/install app)..."
./gradlew connectedAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.doronyg.mylightscontroller.ui.tests.ScreenshotComparisonTest > /tmp/compare.log 2>&1 &
TEST_PID=$!

log_info "Test started (PID: $TEST_PID)"
echo ""

# Wait for build and app installation to complete (approximately 30-40 seconds)
log_info "Waiting for build and installation to complete (max 45 seconds)..."
WAIT_TIME=0
while [ $WAIT_TIME -lt 45 ]; do
    if adb -s "$DEVICE" shell "pm list packages" 2>/dev/null | grep -q "com.doronyg.mylightscontroller"; then
        log_success "App installed on device"
        break
    fi
    echo -n "."
    sleep 2
    WAIT_TIME=$((WAIT_TIME + 2))
done
echo ""

# Wait for test to create ready marker (signals it's waiting for baseline)
EXTERNAL_DIR="/sdcard/Android/data/$APP_PACKAGE/files/Download"
READY_MARKER="$EXTERNAL_DIR/ready.marker"

log_info "Waiting for test to signal ready for baseline (max 30 seconds)..."
READY_TIME=0
READY_FOUND=false
while [ $READY_TIME -lt 30 ]; do
    if adb -s "$DEVICE" shell "test -f $READY_MARKER" 2>/dev/null; then
        log_success "Test ready marker detected!"
        READY_FOUND=true
        break
    fi
    echo -n "."
    sleep 2
    READY_TIME=$((READY_TIME + 2))
done
echo ""

if [ "$READY_FOUND" = false ]; then
    log_error "Test never signaled ready"
    log_error "Test output:"
    tail -50 /tmp/compare.log
    exit 1
fi

# Push baseline to device with retries
DEVICE_BASELINES="/storage/emulated/0/Android/data/$APP_PACKAGE/files/Download/baselines"

# Create the directory first
log_info "Creating baseline directory on device..."
adb -s "$DEVICE" shell "mkdir -p '$DEVICE_BASELINES'" 2>/dev/null || true

log_info "Pushing baseline to device..."

MAX_RETRIES=10
RETRY_COUNT=0
PUSH_SUCCESS=false

while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    if adb -s "$DEVICE" push "$BASELINE_FILE" "$DEVICE_BASELINES/" > /dev/null 2>&1; then
        log_success "Baseline pushed successfully"
        # Sync to ensure file is flushed to storage
        adb -s "$DEVICE" shell "sync" 2>/dev/null || true
        PUSH_SUCCESS=true
        break
    fi

    RETRY_COUNT=$((RETRY_COUNT + 1))
    if [ $RETRY_COUNT -lt $MAX_RETRIES ]; then
        echo "   Retry $RETRY_COUNT/$MAX_RETRIES (waiting 2s)..."
        sleep 2
    fi
done

if [ "$PUSH_SUCCESS" = false ]; then
    log_error "Failed to push baseline to device after $MAX_RETRIES attempts"
    log_error "Test output:"
    tail -50 /tmp/compare.log
    exit 1
fi

log_success "Baseline pushed to: $DEVICE_BASELINES/main_screen.png"
echo ""

# Wait for test to complete (test has its own ~60s timeout for waiting on file)
log_info "Waiting for comparison test to complete..."
if ! wait $TEST_PID; then
    log_error "Test failed!"
    log_error "Test output:"
    tail -50 /tmp/compare.log
    exit 1
fi

log_success "Comparison test complete!"
