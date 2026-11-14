# Bigboss Desk Robot — AI Backend

Vercel serverless API for the Android desk robot.

## Routes

- `GET /api/health` — service health and configuration flags
- `POST /api/robot/chat` — generate one spoken reply using the configured AI provider
- `POST /api/robot/speech` — convert reply text into Indonesian MP3 speech using Azure AI Speech

Chat request:
```json
{ "message": "Halo, Bigboss", "device": "huawei-pad" }
```

Chat success response:
```json
{ "reply": "Iya, halo juga. Ada perlu apa?" }
```

Speech request:
```json
{ "text": "Selamat datang. Mau cari siapa?" }
```

Speech success response: binary MP3 audio (`audio/mpeg`), not JSON.

The chat route sends a single user message and the Bigboss persona prompt to an OpenAI-compatible Chat Completions API. It does not yet retain conversation history, visitor memory, response bank entries, or message relay data.

The speech route uses the Azure Speech REST API. The default voice is `id-ID-ArdiNeural` (Indonesian male). It applies a slightly slower speaking rate and lower pitch through SSML. Voice and prosody can be tuned after listening to a real sample.

## Configure Vercel

Import `Banquet-ID/bigboss-desk-robot` as a new Vercel project. Use the repository root as the project root and the default Node.js runtime for API routes.

AI environment variables:

- `AI_API_KEY` — secret key from the selected AI provider
- `AI_MODEL` — model identifier supported by that provider
- `AI_BASE_URL` — optional API base URL; defaults to `https://api.openai.com/v1`

Azure Speech environment variables:

- `AZURE_SPEECH_KEY` — secret key for an Azure Speech resource
- `AZURE_SPEECH_REGION` — resource region, for example `southeastasia` if that is where the resource was created
- `AZURE_SPEECH_VOICE` — optional voice name; defaults to `id-ID-ArdiNeural`

Other:

- `ROBOT_ALLOWED_ORIGIN` — optional exact origin for a future browser-based Back Office

After configuring variables, redeploy the Vercel project. Test `GET /api/health`, then test chat and speech endpoints. The Android app must call the speech route through the backend; never put Azure credentials in the APK.

## Security and operating limits

- Never put provider API keys in Android source, APK, or GitHub.
- This prototype has no durable rate limiting, device authentication, or usage quota enforcement. Do not publish the endpoints broadly until an access-control and rate-limit layer is added.
- `ROBOT_ALLOWED_ORIGIN` is a browser CORS control, not authentication. Native Android requests may omit the Origin header.
- Do not log visitor messages, names, photos, or other personal data.
- Use HTTPS only.

## Local development

Run `npm install`, configure environment variables locally (for example from a private `.env.local` file), then run `npm run dev`.
