# Bigboss Desk Robot

Android tablet display prototype for the Bigboss Desk Robot.

## v0.1 scope
- Landscape, full-screen black display
- Two large white rounded eyes
- Simple curved smile
- Subtitle directly below the face
- Idle eye movement and blinking
- Android TextToSpeech greeting on launch (Indonesian locale requested)
- Android SpeechRecognizer microphone input with runtime RECORD_AUDIO permission
- Simple Indonesian phrase-response loop (temporary local demo, not AI)
- Face changes to listening/speaking states; mouth pulse reacts to speech input level
- Subtitle displays listening state and spoken responses
- No logo, clock, status bar, decorative controls, or task-management features

## Build
Open this repository in Android Studio (Giraffe or newer), allow Gradle sync, then run the `app` configuration on an Android device or emulator.

- Package: `id.banquet.bigbossrobot`
- Minimum Android: API 26
- Compile SDK: 35

This is an early display + speech-output prototype. AI conversation, camera/presence detection, visitor memory, Back Office, and Telegram are not connected yet. Speech recognition depends on an available Android recognition service and network/device support. Android device TTS voice availability depends on the installed speech engine and Indonesian voice data.
