#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
./gradlew assembleDebug assembleDebugAndroidTest
# Run with an attached Android 12+ device/emulator: ./gradlew connectedDebugAndroidTest
