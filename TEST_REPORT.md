# Retro 95 2.0 verification

Version: 2.0.0 (versionCode 2). Minimum Android 12 / API 31; target API 36.

## Verification scope

The v2 update adds WindowManager folding callbacks, safe-pane geometry, Android 12+ configuration, narrow-screen taskbar behavior and broader automated coverage. New checks are pending execution; this report will be updated with actual results before the v2 APK is delivered.

Planned checks:

- Calculator and activity regression tests.
- Hinge geometry: vertical, horizontal zero-height crease, multiple hinges, offscreen hinges and keyboard-reduced bounds.
- Live fold/unfold and narrow resizing with an open note retained; non-separating flat creases retain the full desktop.
- Native rendering: 280×653, 360×640, 411×891, 891×411, 600×960, 1280×800, 320×320 dp and enlarged fonts.
- Native emulator app launch, HOME declaration, all tools, note persistence, task restore and workspace bounds on Android APIs 31, 32, 35 and 36.

Physical devices, OEM Home-role behavior, physical hinge sensors, gesture animation behavior, work-profile restrictions and every Android version/manufacturer cannot be exhaustively verified here. Manual device checks should include setting/reverting the default launcher, folding/unfolding while editing, keyboard visibility, app launches and both navigation modes.

Version 1 results and screenshots in docs/verification and docs/screenshots are historical v1 evidence, not v2 evidence.
