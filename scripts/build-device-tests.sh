#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
: "${ANDROID_HOME:?}"; : "${JAVA_HOME:?}"
TOOLS="$ANDROID_HOME/build-tools/36.0.0"; JAR="$ANDROID_HOME/platforms/android-36/android.jar"; OUT=app/build/sdk-tests
mkdir -p "$OUT/classes" "$OUT/dex"
cat > "$OUT/AndroidManifest.xml" <<'XML'
<manifest xmlns:android="http://schemas.android.com/apk/res/android" package="dev.magesh.win95.test"><uses-sdk android:minSdkVersion="30" android:targetSdkVersion="36"/><application android:label="Retro95 Tests"/><instrumentation android:name="dev.magesh.win95.LauncherInstrumentation" android:targetPackage="dev.magesh.win95" android:label="Retro95 regression"/></manifest>
XML
"$TOOLS/aapt2" link -o "$OUT/base.apk" -I "$JAR" --manifest "$OUT/AndroidManifest.xml"
"$JAVA_HOME/bin/javac" --release 17 -cp "$JAR:app/build/sdk/classes" -d "$OUT/classes" app/src/androidTest/java/dev/magesh/win95/LauncherInstrumentation.java
"$JAVA_HOME/bin/jar" cf "$OUT/classes.jar" -C "$OUT/classes" .
"$TOOLS/d8" --lib "$JAR" --classpath app/build/sdk/classes.jar --min-api 30 --output "$OUT/dex" "$OUT/classes.jar"
python3 - "$OUT" <<'PY'
import sys,zipfile,shutil
from pathlib import Path
p=Path(sys.argv[1]);shutil.copyfile(p/'base.apk',p/'unsigned.apk')
with zipfile.ZipFile(p/'unsigned.apk','a',zipfile.ZIP_DEFLATED) as z:
 for f in (p/'dex').glob('*.dex'):z.write(f,f.name)
PY
"$TOOLS/zipalign" -f 4 "$OUT/unsigned.apk" "$OUT/aligned.apk"
"$TOOLS/apksigner" sign --ks "${RETRO95_KEYSTORE:-$HOME/.android/debug.keystore}" --ks-pass pass:android --out "$OUT/tests.apk" "$OUT/aligned.apk"
