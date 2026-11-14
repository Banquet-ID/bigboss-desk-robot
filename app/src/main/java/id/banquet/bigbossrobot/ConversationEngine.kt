package id.banquet.bigbossrobot

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.Locale
import java.util.concurrent.Executors

/**
 * Pluggable conversation layer.
 * The Android app never stores an AI provider key; remote calls go through our backend.
 */
interface ConversationEngine {
    fun reply(message: String, callback: (Result<String>) -> Unit)
}

object RobotConfig {
    // Set this to the HTTPS backend base URL when the backend is deployed.
    // Keep provider API keys on the backend, never in this Android app.
    const val API_BASE_URL = "https://bigboss-desk-robot.vercel.app"
    const val REQUEST_TIMEOUT_MS = 15_000
}

class LocalConversationEngine : ConversationEngine {
    override fun reply(message: String, callback: (Result<String>) -> Unit) {
        callback(runCatching { localReply(message) })
    }

    private fun localReply(message: String): String {
        val text = message.lowercase(Locale("id", "ID"))
        return when {
            listOf("halo", "hai", "selamat pagi", "selamat siang", "selamat sore")
                .any(text::contains) -> "Iya, halo juga. Ada perlu apa?"
            text.contains("andhry") || text.contains("andri") ->
                "Si kacung? Gue juga lagi nyari. Mau titip pesan?"
            text.contains("siapa") && (text.contains("lo") || text.contains("kamu")) ->
                "Gue Bigboss. Yang punya ruangan ini. Lo siapa?"
            text.contains("bos") || text.contains("bigboss") ->
                "Bosnya di sini. Kacungnya belum tentu ada."
            text.contains("terima kasih") || text.contains("makasih") ->
                "Iya, sama-sama. Jangan lupa mampir lagi."
            else -> "Lo bilang, $message. Otak AI gue belum disambung. Sabar, ya."
        }
    }
}

/**
 * Expected backend contract:
 * POST {API_BASE_URL}/api/robot/chat
 * JSON request:  { "message": "...", "device": "bigboss-desk-robot" }
 * JSON response: { "reply": "..." }
 */
class RemoteConversationEngine(
    private val baseUrl: String,
    private val fallback: ConversationEngine = LocalConversationEngine()
) : ConversationEngine {
    private val executor = Executors.newSingleThreadExecutor()

    override fun reply(message: String, callback: (Result<String>) -> Unit) {
        executor.execute {
            val result = runCatching {
                require(baseUrl.startsWith("https://")) {
                    "Backend harus menggunakan HTTPS."
                }
                val connection = (URL(baseUrl.trimEnd('/') + "/api/robot/chat")
                    .openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = RobotConfig.REQUEST_TIMEOUT_MS
                    readTimeout = RobotConfig.REQUEST_TIMEOUT_MS
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json; charset=utf-8")
                    setRequestProperty("Accept", "application/json")
                }
                try {
                    val payload = JSONObject()
                        .put("message", message)
                        .put("device", "huawei-pad")
                        .toString()
                    connection.outputStream.use {
                        it.write(payload.toByteArray(StandardCharsets.UTF_8))
                    }
                    val status = connection.responseCode
                    val stream = if (status in 200..299) connection.inputStream
                        else connection.errorStream
                    val body = stream?.bufferedReader(StandardCharsets.UTF_8)?.use { it.readText() }
                        .orEmpty()
                    check(status in 200..299) { "Backend error ($status)" }
                    val reply = JSONObject(body).optString("reply").trim()
                    check(reply.isNotBlank()) { "Backend tidak mengirim field reply." }
                    reply
                } finally {
                    connection.disconnect()
                }
            }
            if (result.isSuccess) callback(result)
            else fallback.reply(message, callback)
        }
    }
}
