# Bigboss Desk Robot — AI Backend

This is a Vercel serverless backend for the Android desk robot.

## Endpoint

`POST /api/robot/chat`

Request:
```json
{ "message": "Halo, Bigboss", "device": "bigboss-desk-robot" }
```

Success:
```json
{ "reply": "Iya, halo juga. Ada perlu apa?" }
```

The backend currently sends one user turn plus the Bigboss persona system prompt to an OpenAI-compatible Chat Completions API. Conversation history, visitor memory, response bank, and message relay are intentionally not enabled yet.

## Vercel environment variables

Configure these in the Vercel project settings (do not commit secrets):

- `AI_API_KEY` — provider API key
- `AI_MODEL` — model identifier accepted by the provider
- `AI_BASE_URL` — optional API base URL; defaults to `https://api.openai.com/v1`
- `ROBOT_ALLOWED_ORIGIN` — optional exact browser origin allowlist; native Android requests without an Origin header are allowed

The Android client should use the deployed HTTPS base URL in `RobotConfig.API_BASE_URL`.

## Important security notes

- Never put provider API keys in the Android app or GitHub.
- This first backend iteration does not include durable rate limiting or device authentication. Before public deployment, put it behind an appropriate access/rate-limit layer (for example, an API gateway or managed rate-limit service).
- Keep Vercel function logs free of visitor messages and personal information.
- Use HTTPS only.

## Local development

Install dependencies, configure environment variables locally, then run `npm run dev`.
