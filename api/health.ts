import type { VercelRequest, VercelResponse } from "@vercel/node";

export default function handler(_req: VercelRequest, res: VercelResponse) {
  res.setHeader("Cache-Control", "no-store");
  return res.status(200).json({
    status: "ok",
    service: "bigboss-desk-robot-api",
    aiConfigured: Boolean(process.env.AI_API_KEY && process.env.AI_MODEL),
    speechConfigured: Boolean(process.env.AZURE_SPEECH_KEY && process.env.AZURE_SPEECH_REGION),
    speechVoice: process.env.AZURE_SPEECH_VOICE || "id-ID-ArdiNeural"
  });
}
