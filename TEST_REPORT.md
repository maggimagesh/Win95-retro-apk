# Retro 95 — Verification report

Date: 19 October 2026. Version: 1.0.0. Package: `dev.magesh.win95`.

## Result and limits

The source builds into a signed APK. The automated suite passes 17 tests, and Android lint reports zero errors with eight warnings. These are host-based tests, including real native Skia rendering under Robolectric; they are **not tests performed on a Samsung phone**.

No Galaxy S24 Ultra / One UI 8.5 device was connected. This environment had no `/dev/kvm`. An Android 16 x86_64 emulator was installed and attempted with software CPU emulation, but it did not reach a usable boot. Consequently installation on a running Android system, system role selection, real installed-app launching, Samsung gestures and hardware-specific behavior remain unverified. The included device suites compile but were not executed. This build must not be described as completely tested or guaranteed bug-free.

## Executed checks

| Check | Environment | Result |
|---|---|---|
| Production compilation and APK packaging | JDK 17, Android SDK 36, build-tools 36.0.0 | Passed |
| Gradle app and Android test APK builds | Gradle 8.13, AGP 8.13.0, JDK 21 | Passed |
| Calculator regression tests | JUnit 4.13.2, 6 tests | Passed |
| Activity behavior and saved state | Robolectric 4.16, API 35, 7 tests | Passed |
| Layout geometry and native rendering | Robolectric 4.16, API 36, 4 tests | Passed |
| Android lint | API 36 project | 0 errors, 8 warnings |
| APK signature | Android apksigner, APK Signature Scheme v3 | Passed |
| ZIP structure and resource table | Python zipfile, Android aapt | Passed |
| APK alignment | Android zipalign, 4-byte alignment | Passed |
| Shell syntax and manifest/resource XML parsing | bash and XML parser | Passed |
| Git staged whitespace check | git diff --cached --check | Passed |

Calculator tests cover operator precedence, brackets/unary signs, decimal accuracy, bounded division, malformed expressions, zero division and the input length limit. Activity tests cover Start-menu toggling, saved notes, minimize/task restore, search restoration, opening/closing all built-in tools, minimized-state restoration and calculator error handling.

The render tests open the desktop, Start menu and each tool, assert that windows and the menu fit their workspace, and export desktop/menu/calculator/calendar PNGs. Representative renders were inspected visually; landscape tools intentionally scroll when their content exceeds the available height.

| Render scenario | Logical viewport | Font scale |
|---|---|---|
| S24-like portrait | 411 × 891 dp | 100% |
| Landscape | 891 × 411 dp | 100% |
| Compact phone | 360 × 640 dp | 100% |
| Larger accessibility font | 411 × 891 dp | 150% |

These are logical viewport simulations, not a recreation of Samsung firmware, gesture navigation, status bars or a physical display cutout.

The lint warnings are: one API-specific attribute warning for the Android 13+ Back opt-in, one Android 12+ data-extraction configuration advisory, one XML-tool constructor advisory for a view created only in Java, and five English string/localization warnings. Modern Back uses Android's platform dispatcher; the legacy `onBackPressed` fallback is restricted to older Android behavior and has a documented lint suppression.

## Device checks still required

All items below are **not executed**:

1. Install on the S24 Ultra, open normally, select Retro 95 in the Android Home role chooser, press Home and reboot to verify the default remains selected.
2. Verify every expected personal-profile app appears; launch several real apps, return Home, and use Samsung Recents and gesture navigation. Check the three-button navigation mode separately.
3. Pin/unpin apps, rename/update/uninstall an app, and verify shortcuts refresh without a crash. Check any permitted work profile both running and paused.
4. Write a note; test keyboard opening, rotation, process termination, reopening and reboot. Confirm the note and desktop settings persist; exercise Share.
5. Exercise portrait/landscape, font and display zoom settings, split screen, cutout/status/nav insets and Samsung DeX if used.
6. Test Home selection cancellation, all Control Panel system intents, file picker open/cancel, unavailable handlers and Run app/web lookup.
7. Use the launcher through a long idle period, screen lock/unlock and repeated app switching; observe crash logs and memory use.
8. Switch back through Control Panel → Choose another home app → One UI Home and verify normal Samsung Home works.

## Scope

The desktop and built-in tools use the classic theme. A normal launcher does not replace One UI's lock screen, notifications, recents, permission dialogs, keyboard, system settings or other apps. There is one built-in tool window at a time; window dragging, Android widgets, Secure Folder and Android Private Space are outside this version's supported features. The Recycle Bin is informational. Original pixel artwork is included; Windows software and Microsoft assets are not.

The APK is development-signed. It is architecture-independent Java bytecode, with minimum API 30 and target API 36. It requests no permissions. Preserve a stable private signing key for later releases; the repository never includes a private key.

## Delivered APK

SHA-256: `b950cdb148877623ee510059c15b22e0faa49d49dd360c961187c8eb3eba26f4`.

The project is published to https://github.com/maggimagesh/Win95-retro-apk. The results above are local verification results; see the repository Actions tab for independent CI results.
