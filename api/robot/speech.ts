import type { VercelRequest, VercelResponse } from "@vercel/node";

const MAX_TEXT_LENGTH = 1800;
const DEFAULT_VOICE = "id-ID-ArdiNeural";

function xmlEscape(value: string): string {
  return value
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&apos;");
}

function sendError(res: VercelResponse, status: number, error: string) {
  return res.status(status).json({ error });
}

export default async function handler(req: VercelRequest, res: VercelResponse) {
  res.setHeader("Cache-Control", "no-store");
  res.setHeader("X-Content-Type-Options", "nosniff");

  if (req.method === "OPTIONS") {
    res.setHeader("Allow", "POST, OPTIONS");
    return res.status(204).end();
  }
  if (req.method !== "POST") {
    res.setHeader("Allow", "POST");
    return sendError(res, 405, "Method tidak didukung.");
  }

  const text = typeof req.body?.text === "string" ? req.body.text.trim() : "";
  if (!text) return sendError(res, 400, "Teks tidak boleh kosong.");
  if (text.length > MAX_TEXT_LENGTH) {
    return sendError(res, 413, "Teks terlalu panjang.");
  }

  const key = process.env.AZURE_SPEECH_KEY;
  const region = process.env.AZURE_SPEECH_REGION;
  if (!key || !region) {
    return sendError(res, 503, "Layanan suara belum dikonfigurasi.");
  }

  // Keep the voice configurable server-side; never expose the Azure key in the APK.
  const voice = process.env.AZURE_SPEECH_VOICE || DEFAULT_VOICE;
  const safeRegion = region.trim().toLowerCase();
  if (!/^[a-z0-9]+$/.test(safeRegion)) {
    return sendError(res, 500, "Konfigurasi region suara tidak valid.");
  }

  const ssml = `<speak version="1.0" xml:lang="id-ID"><voice name="${xmlEscape(voice)}"><prosody rate="-4%" pitch="-8%">${xmlEscape(text)}</prosody></voice></speak>`;
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), 25000);

  try {
    const upstream = await fetch(
      `https://${safeRegion}.tts.speech.microsoft.com/cognitiveservices/v1`,
      {
        method: "POST",
        headers: {
          "Ocp-Apim-Subscription-Key": key,
          "Content-Type": "application/ssml+xml",
          "X-Microsoft-OutputFormat": "audio-24khz-48kbitrate-mono-mp3",
          "User-Agent": "BigbossDeskRobot"
        },
        body: ssml,
        signal: controller.signal
      }
    );

    if (!upstream.ok) {
      console.error("Azure Speech returned status", upstream.status);
      return sendError(res, 502, "Mesin suara sedang tidak merespons.");
    }

    const audio = await upstream.arrayBuffer();
    if (audio.byteLength === 0) {
      return sendError(res, 502, "Mesin suara mengirim audio kosong.");
    }

    res.setHeader("Content-Type", "audio/mpeg");
    res.setHeader("Content-Length", String(audio.byteLength));
    res.setHeader("Content-Disposition", 'inline; filename="bigboss.mp3"');
    return res.status(200).send(Buffer.from(audio));
  } catch (error) {
    const timedOut = error instanceof Error && error.name === "AbortError";
    console.error("Azure Speech request failed", timedOut ? "timeout" : "network error");
    return sendError(res, 502, "Koneksi ke mesin suara gagal.");
  } finally {
    clearTimeout(timeout);
  }
}
