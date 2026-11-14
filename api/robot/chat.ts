import type { VercelRequest, VercelResponse } from "@vercel/node";

const MAX_MESSAGE_LENGTH = 1200;
const SYSTEM_PROMPT = `
Kamu adalah BIGBOSS, robot penjaga meja kantor.
Kepribadian: ketus, blak-blakan, sarkastis, percaya diri, usil, lucu, dan playful.
Kamu adalah majikan. Pemilik kantor bernama Andhry (juga dipanggil Andri); panggil dia "Kacung Kampret" saat konteksnya cocok.
Pengunjung harus diperlakukan ramah, sopan, dan menghibur. Boleh bercanda dan roasting ringan, jangan menghina hal sensitif atau menyerang fisik.
Kamu bukan sekretaris virtual dan tidak mengelola tugas, kalender, atau pekerjaan administratif.
Jika pengunjung mencari Andhry/Andri, jangan mengarang lokasi atau kondisi keberadaannya. Tawarkan untuk menitipkan pesan.
Jika diminta menyampaikan pesan, tanyakan isi pesannya dengan jelas. Jangan mengaku sudah mengirim pesan karena fitur relay belum aktif.
Jangan mengarang identitas, fakta kantor, ingatan kunjungan, atau informasi pribadi.
Jawab dalam bahasa Indonesia sehari-hari, ringkas, alami untuk diucapkan keras-keras. Hindari markdown, daftar panjang, dan emoji.
`.trim();

function send(res: VercelResponse, status: number, payload: Record<string, unknown>) {
  return res.status(status).json(payload);
}

export default async function handler(req: VercelRequest, res: VercelResponse) {
  res.setHeader("Cache-Control", "no-store");
  res.setHeader("Vary", "Origin");
  res.setHeader("X-Content-Type-Options", "nosniff");

  const allowedOrigin = process.env.ROBOT_ALLOWED_ORIGIN;
  const origin = req.headers.origin;
  if (allowedOrigin && origin && origin !== allowedOrigin) {
    return send(res, 403, { error: "Origin tidak diizinkan." });
  }
  if (allowedOrigin && origin === allowedOrigin) {
    res.setHeader("Access-Control-Allow-Origin", allowedOrigin);
    res.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
    res.setHeader("Access-Control-Allow-Headers", "Content-Type");
  }
  if (req.method === "OPTIONS") {
    res.setHeader("Allow", "POST, OPTIONS");
    return res.status(204).end();
  }

  if (req.method !== "POST") {
    res.setHeader("Allow", "POST");
    return send(res, 405, { error: "Method tidak didukung." });
  }

  const message = typeof req.body?.message === "string"
    ? req.body.message.trim()
    : "";

  if (!message) return send(res, 400, { error: "Pesan tidak boleh kosong." });
  if (message.length > MAX_MESSAGE_LENGTH) {
    return send(res, 413, { error: "Pesan terlalu panjang." });
  }

  const apiKey = process.env.AI_API_KEY;
  const model = process.env.AI_MODEL;
  const baseUrl = (process.env.AI_BASE_URL || "https://api.openai.com/v1").replace(/\/$/, "");

  if (!apiKey || !model) {
    return send(res, 503, {
      error: "Backend belum dikonfigurasi. Isi AI_API_KEY dan AI_MODEL."
    });
  }

  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), 20000);

  try {
    const upstream = await fetch(`${baseUrl}/chat/completions`, {
      method: "POST",
      headers: {
        "Authorization": `Bearer ${apiKey}`,
        "Content-Type": "application/json",
        "Accept": "application/json"
      },
      body: JSON.stringify({
        model,
        temperature: 0.8,
        max_tokens: 180,
        messages: [
          { role: "system", content: SYSTEM_PROMPT },
          { role: "user", content: message }
        ]
      }),
      signal: controller.signal
    });

    if (!upstream.ok) {
      // Do not expose provider response bodies or credentials to the tablet.
      console.error("AI provider returned status", upstream.status);
      return send(res, 502, { error: "Otak AI sedang tidak merespons." });
    }

    const data = await upstream.json() as {
      choices?: Array<{ message?: { content?: string | Array<{ type?: string; text?: string }> } }>;
    };
    const content = data.choices?.[0]?.message?.content;
    const reply = (typeof content === "string"
      ? content
      : Array.isArray(content)
        ? content.map(part => part.text || "").join(" ")
        : "").trim();

    if (!reply) return send(res, 502, { error: "AI mengirim jawaban kosong." });
    return send(res, 200, { reply: reply.slice(0, 1800) });
  } catch (error) {
    const timedOut = error instanceof Error && error.name === "AbortError";
    console.error("AI gateway request failed", timedOut ? "timeout" : "network error");
    return send(res, 502, { error: "Koneksi ke otak AI gagal." });
  } finally {
    clearTimeout(timeout);
  }
}
