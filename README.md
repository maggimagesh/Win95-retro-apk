# Retro 95 Launcher 2.0

A native Android HOME launcher with a Windows 95-inspired desktop, Start menu, taskbar, original pixel icons and beveled tool windows. Version **2.0.0** supports **Android 12 / API 31 and newer**, across manufacturers, screen sizes and CPU architectures. It targets Android 16 / API 36. There are no device-model checks or native libraries.

## Device and layout support

- Standard phones, narrow foldable cover screens, flip and book foldables, tablets, portrait and landscape.
- Resizable activity with no orientation lock or fixed aspect ratio. Layout follows the current app window, including split screen where the OS allows launchers to use it.
- Jetpack WindowManager observes folding changes while the activity is started. A separating fold or fully occluding hinge places the desktop, taskbar and tool windows in the largest clear pane, with an 8 dp hinge margin. Top/left wins equal-area ties. This deliberate single-pane layout keeps controls off the hinge; it does not provide two independent desktops. An unfolded, non-separating crease uses the full available screen. Multiple reported barriers are supported.
- System-bar, display-cutout and keyboard insets reduce the usable desktop. The icon grid reflows with width and font size; tool windows and the scrollable Start menu stay within their workspace.
- Below 360 dp usable width the taskbar clock is hidden; **Start → Date / Time** remains available. Below 280 dp Start uses an icon with an accessible label. Main and title-bar controls have 48 dp touch targets.
- English interface; dates and clock formatting follow Android locale settings. This release does not include translated UI strings.

Compatibility is designed for Android devices that allow a third-party HOME app. OEM policies, managed devices and unusual launch surfaces can restrict that role. Android TV, Wear OS and Android Auto are outside this launcher's scope. There is no claim that every physical device has been tested; see [TEST_REPORT.md](TEST_REPORT.md).

## Features

- Android default-home chooser; Programs explorer with installed-app search, pin/unpin shortcuts and app information.
- Profile-aware app discovery and launching through LauncherApps. Android-permitted work apps are badged; paused profiles fail gracefully. Secure Folder and Android Private Space are not supported.
- Autosaving Notepad with share-sheet export, arithmetic Calculator, Run program/web lookup, month calendar and Control Panel.
- Five desktop colors, standard/large icons, minimize/restore/maximize/close. Notes, current tool, search, calculator and minimized/maximized state survive supported activity recreation. Live resizing and fold changes retain the existing tool view.
- No ads, analytics, account, network permission, broad package permission or broad file permission. Notes/settings stay in app-private preferences; backup is disabled.

The classic theme applies to this launcher's screens. Android settings, notifications, lock screen, recents, keyboard, permission dialogs and installed apps keep their own appearance. Gesture animations are controlled by Android/OEM software. This is an Android launcher, not a Windows emulator. One built-in tool window is open at a time; dragging windows and Android widgets are not implemented. Recycle Bin explains shortcut removal. My Documents opens Android's document picker.

## Download and install

[Download Retro95-2.0.0.apk](apk/Retro95-2.0.0.apk?raw=true)

1. Open the APK on an Android 12+ phone or tablet. If prompted, allow installation from the browser or file manager used for this APK. OEM sideloading controls may also apply.
2. Open **Retro 95 → Control Panel → Set Retro 95 as default launcher** and confirm the Android home chooser.
3. Open **My Computer** or **Start → Programs** to launch apps; hold an app to pin it.
4. To return to another launcher, select **Control Panel → Choose another home app**, then your preferred launcher (for example One UI Home or Pixel Launcher). Android's default-apps settings also expose the Home app choice.

### Upgrading from 1.0

The package remains `dev.magesh.win95`, with versionCode 2. The distributed APK uses a development certificate. The original 1.0 private signing key is unavailable, so an installed 1.0 build with a different certificate must first be uninstalled. **Before uninstalling, use Notepad → Share note to export any notes.** Uninstallation erases local notes, pins and settings. Future builds must use the same private signing key for in-place upgrades. Never commit a private keystore.

## Build and verify

Requires JDK 21 (Android 16 Robolectric tests; app sources use Java 17), Gradle 8.13, Android SDK platform 36 and build-tools 36.0.0. Jetpack WindowManager 1.5.1 is the runtime folding dependency. Gradle resolves and packages its transitive dependencies.

```sh
export ANDROID_HOME=/path/to/android-sdk
export JAVA_HOME=/path/to/jdk-21
sdkmanager 'platforms;android-36' 'build-tools;36.0.0' 'platform-tools'
./gradlew testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest
# With an attached Android 12+ device or emulator:
./gradlew connectedDebugAndroidTest
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`. The scripts in `scripts/` delegate to Gradle; v1's dependency-free SDK-only build path no longer applies. GitHub Actions builds the APK and runs emulator suites on API 31, 32, 35 and 36. Layout tests cover narrow phones, large fonts, landscape, tablets, short windows and synthetic folding changes. Synthetic posture tests are not physical foldable certification.

## Source layout

- `DesktopActivity.java`: desktop, window lifecycle, WindowManager listener, app discovery and built-in tools.
- `DesktopGeometry.java`: contiguous pane selection for hinges and folds.
- `RetroStyle.java`: beveled controls and original pixel icons.
- `Calculator.java`: bounded BigDecimal arithmetic parser.
- `app/src/test/`: arithmetic, activity, adaptive geometry, recreation and native-render tests.
- `app/src/androidTest/`: native Android instrumentation checks.

Independent project; not affiliated with Microsoft, Samsung or Google. Windows is a Microsoft trademark. No Microsoft artwork, fonts, operating-system files or binaries are bundled.
