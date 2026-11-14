# Bigboss Desk Robot — AI Backend

Vercel serverless API for the Android desk robot.

## Routes

- `GET /api/health` — service health and whether AI environment variables are present
- `POST /api/robot/chat` — generate one spoken reply

Chat request:
```json
{ "message": "Halo, Bigboss", "device": "bigboss-desk-robot" }
```

Success response:
```json
{ "reply": "Iya, halo juga. Ada perlu apa?" }
```

The chat route sends a single user message and the Bigboss persona prompt to an OpenAI-compatible Chat Completions API. It does not yet retain conversation history, visitor memory, response bank entries, or message relay data.

## Configure Vercel

Import `Banquet-ID/bigboss-desk-robot` as a new Vercel project. Use the repository root as the project root and the default Node.js runtime for API routes.

Add environment variables in Vercel Project Settings:

- `AI_API_KEY` — secret key from the selected AI provider
- `AI_MODEL` — model identifier supported by that provider
- `AI_BASE_URL` — optional API base URL; defaults to `https://api.openai.com/v1`
- `ROBOT_ALLOWED_ORIGIN` — optional exact origin for a future browser-based Back Office

Examples of compatible base URLs include provider endpoints that implement `/chat/completions`. Confirm the exact base URL and model name in the chosen provider's documentation.

After deployment:
1. Open `/api/health` and confirm `status: ok`.
2. Confirm `aiConfigured: true` after setting both AI variables.
3. Test `POST /api/robot/chat` with a small message.
4. Only then configure the HTTPS deployment URL in the Android app's `RobotConfig.API_BASE_URL`.

## Security and operating limits

- Never put provider API keys in Android source, APK, or GitHub.
- This prototype has no durable rate limiting, device authentication, or usage quota enforcement. Do not publish the chat endpoint broadly until an access-control and rate-limit layer is added.
- `ROBOT_ALLOWED_ORIGIN` is a browser CORS control, not authentication. Native Android requests may omit the Origin header.
- Do not log visitor messages, names, photos, or other personal data.
- Use HTTPS only.

## Local development

Run `npm install`, configure environment variables locally (for example from a private `.env.local` file), then run `npm run dev`.
