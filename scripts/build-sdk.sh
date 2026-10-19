#!/usr/bin/env bash
# v2 uses Jetpack WindowManager; Gradle resolves its runtime dependencies.
set -euo pipefail
cd "$(dirname "$0")/.."
./gradlew assembleDebug
