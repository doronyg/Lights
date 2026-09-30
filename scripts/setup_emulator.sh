#!/bin/bash
# Create a dedicated Android emulator for visual regression testing
# This script finds Android SDK tools and creates/configures the emulator
#
# Works on: macOS, Linux, and in GitHub Actions
# Requires: Android SDK installed and ANDROID_SDK_ROOT set (or auto-detects)

# Note: We use explicit error handling instead of 'set -e' to provide better messages

# ============================================================================
# CONFIGURATION
# ============================================================================

EMULATOR_NAME="VisualRegressionTest"
API_LEVEL="34"
ABI="arm64-v8a"
DEVICE_TYPE="pixel_6_pro"

# ============================================================================
# FIND ANDROID SDK
# ============================================================================

find_android_sdk() {
    # GitHub Actions sets ANDROID_HOME
    if [ -n "$ANDROID_HOME" ] && [ -d "$ANDROID_HOME" ]; then
        echo "$ANDROID_HOME"
        return 0
    fi

    # Check environment variable first
    if [ -n "$ANDROID_SDK_ROOT" ] && [ -d "$ANDROID_SDK_ROOT" ]; then
        echo "$ANDROID_SDK_ROOT"
        return 0
    fi

    # macOS common location
    if [ -d "$HOME/Library/Android/sdk" ]; then
        echo "$HOME/Library/Android/sdk"
        return 0
    fi

    # Linux common location
    if [ -d "$HOME/Android/sdk" ]; then
        echo "$HOME/Android/sdk"
        return 0
    fi

    return 1
}

# ============================================================================
# FIND TOOLS
# ============================================================================

find_tool() {
    local tool_name=$1
    local sdk_root=$2

    # Try cmdline-tools/latest first
    if [ -f "$sdk_root/cmdline-tools/latest/bin/$tool_name" ]; then
        echo "$sdk_root/cmdline-tools/latest/bin/$tool_name"
        return 0
    fi

    # Try older cmdline-tools structure (numbered directories like "9.0", "10.0", etc)
    if [ -d "$sdk_root/cmdline-tools" ]; then
        for dir in "$sdk_root"/cmdline-tools/*; do
            if [ -f "$dir/bin/$tool_name" ]; then
                echo "$dir/bin/$tool_name"
                return 0
            fi
        done
    fi

    # Try tools directory (deprecated but might exist)
    if [ -f "$sdk_root/tools/bin/$tool_name" ]; then
        echo "$sdk_root/tools/bin/$tool_name"
        return 0
    fi

    # Try in PATH
    if command -v "$tool_name" &> /dev/null; then
        command -v "$tool_name"
        return 0
    fi

    return 1
}

# ============================================================================
# MAIN SCRIPT
# ============================================================================

echo "📱 Setting up Visual Regression Test Emulator"
echo "=============================================="
echo ""
echo "Emulator Name: $EMULATOR_NAME"
echo "API Level: $API_LEVEL"
echo "ABI: $ABI"
echo "Device Type: $DEVICE_TYPE"
echo ""

# Find SDK
echo "🔍 Finding Android SDK..."
ANDROID_SDK=$(find_android_sdk)
if [ $? -ne 0 ]; then
    echo "❌ ERROR: Could not find Android SDK"
    echo ""
    echo "Please set ANDROID_SDK_ROOT environment variable:"
    echo "  export ANDROID_SDK_ROOT=/path/to/android/sdk"
    echo ""
    echo "Or install Android SDK from: https://developer.android.com/studio"
    exit 1
fi

echo "   Found at: $ANDROID_SDK"
echo ""

# Check if emulator already exists
echo "✓ Checking for existing emulator..."
if [ -d "$HOME/.android/avd/$EMULATOR_NAME.avd" ]; then
    echo "   ✅ Emulator '$EMULATOR_NAME' already exists"
    echo ""
    echo "To start it:"
    echo "  $ANDROID_SDK/emulator/emulator -avd $EMULATOR_NAME -no-snapshot-save"
    exit 0
fi

# Find tools
echo "🔍 Finding sdkmanager and avdmanager..."
SDKMANAGER=$(find_tool "sdkmanager" "$ANDROID_SDK")
AVDMANAGER=$(find_tool "avdmanager" "$ANDROID_SDK")

echo ""
echo "Results:"
if [ -n "$SDKMANAGER" ]; then
    echo "   ✅ sdkmanager: $SDKMANAGER"
else
    echo "   ❌ sdkmanager: NOT FOUND"
fi

if [ -n "$AVDMANAGER" ]; then
    echo "   ✅ avdmanager: $AVDMANAGER"
else
    echo "   ❌ avdmanager: NOT FOUND"
fi

if [ -z "$SDKMANAGER" ] || [ -z "$AVDMANAGER" ]; then
    echo ""
    echo "❌ ERROR: Could not find Android tools"
    echo ""
    echo "Checked locations:"
    echo "   • $ANDROID_SDK/cmdline-tools/latest/bin/"
    echo "   • $ANDROID_SDK/cmdline-tools/*/bin/"
    echo "   • $ANDROID_SDK/tools/bin/"
    echo "   • System PATH"
    echo ""
    echo "Available cmdline-tools directories:"
    if [ -d "$ANDROID_SDK/cmdline-tools" ]; then
        ls -la "$ANDROID_SDK/cmdline-tools/" || echo "   (none)"
    else
        echo "   (cmdline-tools directory doesn't exist)"
    fi
    echo ""
    echo "Please install cmdline-tools:"
    echo ""
    echo "1. Download from:"
    echo "   https://developer.android.com/studio#command-tools"
    echo ""
    echo "2. Extract to your SDK cmdline-tools directory:"
    echo "   mkdir -p $ANDROID_SDK/cmdline-tools"
    echo "   unzip ~/Downloads/commandlinetools-*.zip -d $ANDROID_SDK/cmdline-tools/"
    echo ""
    echo "3. Rename extracted directory to 'latest':"
    echo "   mv $ANDROID_SDK/cmdline-tools/cmdline-tools $ANDROID_SDK/cmdline-tools/latest"
    echo ""
    echo "4. Verify installation:"
    echo "   ls $ANDROID_SDK/cmdline-tools/latest/bin/"
    echo ""
    echo "5. Run this script again:"
    echo "   $0"
    echo ""
    exit 1
fi

# Install system image
echo "📥 Installing system image (android-$API_LEVEL, $ABI)..."
if "$SDKMANAGER" "system-images;android-$API_LEVEL;google_apis;$ABI" --verbose 2>&1 | tail -5; then
    echo "   ✅ System image installed"
else
    echo "   ⚠️  System image installation had warnings (continuing...)"
fi
echo ""

# Create AVD
echo "🛠️  Creating Android Virtual Device..."
echo "   Name: $EMULATOR_NAME"
echo "   Target: android-$API_LEVEL"
echo "   ABI: $ABI"
echo ""

if "$AVDMANAGER" create avd \
    -n "$EMULATOR_NAME" \
    -k "system-images;android-$API_LEVEL;google_apis;$ABI" \
    -d "$DEVICE_TYPE" \
    -c 2048M \
    -f; then
    echo ""
    echo "✅ Emulator created successfully!"
    echo ""
    echo "To start the emulator, run:"
    echo "  $ANDROID_SDK/emulator/emulator -avd $EMULATOR_NAME -no-snapshot-save"
else
    echo ""
    echo "❌ ERROR: Failed to create emulator"
    exit 1
fi














