# Bigboss Desk Robot

Android tablet desk-robot prototype for the Bigboss Desk Robot.

## Current prototype
- Landscape, full-screen black display
- Two large white rounded eyes and a simple curved smile
- Subtitle directly below the face
- Idle eye movement, blinking, and expression changes while speaking/listening
- Android TextToSpeech greeting and Indonesian speech output
- Android SpeechRecognizer microphone input with runtime RECORD_AUDIO permission
- ConversationEngine abstraction with a local Indonesian fallback
- Optional HTTPS backend connector for AI replies
- No provider API key is stored in the Android app
- No logo, clock, status bar, decorative controls, or task-management features

## Conversation backend contract

When configured, the app sends:

`POST {API_BASE_URL}/api/robot/chat`

Request JSON:
```json
{
  "message": "visitor utterance",
  "device": "bigboss-desk-robot"
}
```

Expected response JSON:
```json
{
  "reply": "spoken response"
}
```

Set `RobotConfig.API_BASE_URL` in
`app/src/main/java/id/banquet/bigbossrobot/ConversationEngine.kt`
to the HTTPS base URL of the backend. Keep all AI provider credentials on that backend.
If the URL is blank, the app uses the local demo response engine. If the remote request
fails, it falls back to local responses.

## Build
Open this repository in Android Studio, allow Gradle sync, then run the `app` configuration
on an Android device or emulator.

- Package: `id.banquet.bigbossrobot`
- Minimum Android: API 26
- Compile SDK: 35

## Not connected yet
AI backend service, presence detection, camera/photo booth, visitor memory, Back Office,
music library, gossip manager, and Telegram message relay are not implemented yet.

Speech recognition depends on an available Android recognition service and device/network
support. Text-to-speech voice availability depends on the installed speech engine and
Indonesian voice data.

## Testing status
Source changes are committed to GitHub. An Android build and Huawei tablet device test
have not yet been run.


## AI backend (new)
A Vercel serverless chat endpoint is available at `api/robot/chat.ts`.
See [backend setup](backend/README.md) for environment variables and deployment notes.
The Android app remains on local fallback mode until an HTTPS backend URL is configured
in `RobotConfig.API_BASE_URL` in `ConversationEngine.kt`.
