#!/usr/bin/env bash
# Dependency-free fallback using Google's Android SDK build tools and JDK 17.
set -euo pipefail
cd "$(dirname "$0")/.."
: "${ANDROID_HOME:?Set ANDROID_HOME to your Android SDK}"
TOOLS="$ANDROID_HOME/build-tools/36.0.0"
JAR="$ANDROID_HOME/platforms/android-36/android.jar"
OUT=app/build/sdk
mkdir -p "$OUT/classes" "$OUT/gen" "$OUT/dex"
"$TOOLS/aapt2" compile --dir app/src/main/res -o "$OUT/resources.zip"
sed 's/<manifest /<manifest package="dev.magesh.win95" /' app/src/main/AndroidManifest.xml > "$OUT/AndroidManifest.xml"
"$TOOLS/aapt2" link -o "$OUT/base.apk" -I "$JAR" --manifest "$OUT/AndroidManifest.xml" --java "$OUT/gen" --min-sdk-version 30 --target-sdk-version 36 --version-code 1 --version-name 1.0.0 "$OUT/resources.zip"
find app/src/main/java "$OUT/gen" -name '*.java' > "$OUT/sources.txt"
"$JAVA_HOME/bin/javac" --release 17 -cp "$JAR" -d "$OUT/classes" @"$OUT/sources.txt"
"$JAVA_HOME/bin/jar" cf "$OUT/classes.jar" -C "$OUT/classes" .
"$TOOLS/d8" --lib "$JAR" --min-api 30 --output "$OUT/dex" "$OUT/classes.jar"
python3 - "$OUT" <<'PY'
import sys,zipfile,shutil
from pathlib import Path
p=Path(sys.argv[1]);shutil.copyfile(p/'base.apk',p/'unsigned.apk')
with zipfile.ZipFile(p/'unsigned.apk','a',zipfile.ZIP_DEFLATED) as z:
 for f in (p/'dex').glob('*.dex'): z.write(f,f.name)
PY
"$TOOLS/zipalign" -f 4 "$OUT/unsigned.apk" "$OUT/aligned.apk"
KEY="${RETRO95_KEYSTORE:-$HOME/.android/debug.keystore}"
if [ ! -f "$KEY" ]; then
 mkdir -p "$(dirname "$KEY")"
 "$JAVA_HOME/bin/keytool" -genkeypair -keystore "$KEY" -storepass android -keypass android -alias androiddebugkey -dname 'CN=Android Debug,O=Android,C=US' -keyalg RSA -keysize 2048 -validity 10000
fi
"$TOOLS/apksigner" sign --ks "$KEY" --ks-pass pass:android --key-pass pass:android --ks-key-alias androiddebugkey --out "$OUT/signed.apk" "$OUT/aligned.apk"
"$TOOLS/apksigner" verify --verbose "$OUT/signed.apk"
mv -f "$OUT/signed.apk" "$OUT/Retro95-1.0.0.apk"
printf 'APK: %s/app/build/sdk/Retro95-1.0.0.apk\n' "$PWD"
