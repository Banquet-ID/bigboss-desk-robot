# Bigboss Desk Robot

Android tablet display prototype for the Bigboss Desk Robot.

## v0.1 scope
- Landscape, full-screen black display
- Two large white rounded eyes
- Simple curved smile
- Subtitle directly below the face
- Idle eye movement and blinking
- Expression preview cycle: idle, curious, speaking, laughing, flirty, and stern
- Expression preview is currently automatic; later it will be driven by presence detection and conversation state
- No logo, clock, status bar, decorative controls, or task-management features

## Build
Open this repository in Android Studio (Giraffe or newer), allow Gradle sync, then run the `app` configuration on an Android device or emulator.

- Package: `id.banquet.bigbossrobot`
- Minimum Android: API 26
- Compile SDK: 35

This is the display shell only. Camera, speech input/output, AI conversation, visitor memory, Back Office, and Telegram are intentionally not connected yet. The current expression cycle is a visual demo.
