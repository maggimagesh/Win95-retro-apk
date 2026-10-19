# Retro 95 2.0 verification

Version **2.0.0**, versionCode **2**, minimum Android **12 / API 31**, target API **36**.

[Successful GitHub Actions run](https://github.com/maggimagesh/Win95-retro-apk/actions/runs/39268320514), tested source commit `70a5822394773fc6e89374432d414c3a6e255e76`, executed 19 October 2026.

## Executed results

| Check | Result |
| --- | --- |
| Calculator regression | 6 passed |
| Activity behavior and recreation | 7 passed |
| Hinge geometry, live fold/unfold, resize, insets and keyboard | 9 passed |
| Native-render responsive layouts | 8 passed |
| Android 12 / API 31 emulator | 4 passed |
| Android 12L / API 32 emulator | 4 passed |
| Android 15 / API 35 emulator | 4 passed |
| Android 16 / API 36 emulator | 4 passed |
| Debug APK and instrumentation APK build | Passed |
| Android lint | 0 errors, 16 warnings |
| APK identity, minimum API and signing verification | Passed |

**30 unit/layout tests and 16 emulator test executions passed.** Device checks verify launch, HOME declaration, all tools, note persistence, minimize/restore and window bounds.

Layout renders cover 280×653, 360×640, 411×891, 891×411, 600×960, 1280×800 and 320×320 dp, plus 1.5× font scaling. Folding tests cover vertical hinges, horizontal zero-height folds, multiple hinges, offscreen barriers, non-separating creases, keyboard-reduced bounds, live fold/unfold and resizing with an open note retained. Folding features are synthetic test inputs, not physical sensor certification.

Lint warnings include English text/localization, platform-version attributes, custom view construction, backup guidance and intentional absolute positioning of pane coordinates. English UI is documented; warnings do not prevent packaging.

## Delivered APK

- File: `apk/Retro95-2.0.0.apk`
- Size: 6,830,227 bytes
- Package: `dev.magesh.win95`
- SHA-256: `f48df1d80e7be7156d1f81a5ac25a71bbb4f58a11800172890c4652d94775b74`
- APK signature verification: passed, v2 scheme, RSA 2048-bit Android development certificate.
- Certificate SHA-256: `53fff5ccbf83e36ffb10cd8d45e51173cc4496d629b2f2bb5c85b7afc34865a8`

This APK is copied directly from the successful build artifact and checked against the above hash before publication. The publishing workflow does not rebuild it.

## Limits and physical-device checks

Physical phones, OEM Home-role behavior, physical hinge sensors, gesture animations, work-profile restrictions and every Android version/manufacturer cannot be exhaustively verified here. Android 13/14 were not separately emulated in this run. Manual checks should include setting/reverting the default launcher, folding/unfolding while editing, keyboard visibility, launching installed apps and both navigation modes. Android 12+ compatibility is the implementation target; these results do not certify every device or future OS release.

The original 1.0 signing key is unavailable. Export Notepad text before uninstalling a differently signed v1 build; uninstalling deletes notes, pins and settings. See README for installation.

The files in `docs/verification/` and old screenshots in `docs/screenshots/` are historical v1 evidence. Current detailed reports and all v2 renders are downloadable from the linked Actions run.
