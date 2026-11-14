package id.banquet.bigbossrobot

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import java.util.Locale
import kotlin.math.min

private const val AUDIO_PERMISSION_REQUEST = 701
private val RobotBlack = Color(0xFF000000)
private val RobotWhite = Color(0xFFFFFFFF)

private enum class FaceMood(val eyeScale: Float, val mouth: String) {
    IDLE(1.00f, "smile"),
    GREETING(1.12f, "smile"),
    LISTENING(1.04f, "small"),
    SPEAKING(0.94f, "open"),
    LAUGHING(0.78f, "laugh"),
    FLIRTY(1.08f, "smirk"),
    STERN(0.84f, "flat")
}

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {
    var faceMood by mutableStateOf(FaceMood.IDLE)
        private set
    var subtitle by mutableStateOf("...")
        private set
    var mouthPulse by mutableFloatStateOf(0f)
        private set

    private var speech: TextToSpeech? = null
    private var recognizer: SpeechRecognizer? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private var recognitionReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.BLACK
        window.navigationBarColor = android.graphics.Color.BLACK
        speech = TextToSpeech(this, this)
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            recognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
                setRecognitionListener(robotRecognitionListener)
            }
        } else {
            subtitle = "Fitur pengenal suara tidak tersedia."
        }
        setContent { RobotScreen(this) }
    }

    override fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) {
            subtitle = "Suara belum tersedia."
            return
        }
        val engine = speech ?: return
        val indonesia = engine.setLanguage(Locale("id", "ID"))
        if (indonesia == TextToSpeech.LANG_MISSING_DATA ||
            indonesia == TextToSpeech.LANG_NOT_SUPPORTED) {
            engine.setLanguage(Locale("id"))
        }
        engine.setSpeechRate(0.94f)
        engine.setPitch(0.88f)
        engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                mainHandler.post { faceMood = FaceMood.SPEAKING }
            }

            override fun onDone(utteranceId: String?) {
                mainHandler.post {
                    faceMood = FaceMood.IDLE
                    beginListening()
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                mainHandler.post {
                    faceMood = FaceMood.IDLE
                    beginListening()
                }
            }
        })
        speakLine("Oh, ada orang. Selamat datang. Mau cari siapa?")
    }

    private fun speakLine(line: String) {
        subtitle = line
        faceMood = FaceMood.GREETING
        val id = "robot-${System.currentTimeMillis()}"
        speech?.speak(line, TextToSpeech.QUEUE_FLUSH, null, id)
    }

    private fun beginListening() {
        if (isFinishing || isDestroyed || recognizer == null) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                AUDIO_PERMISSION_REQUEST
            )
            subtitle = "Izinkan akses microphone untuk ngobrol."
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "id-ID")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "id-ID")
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, false)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Bigboss Desk Robot sedang mendengarkan")
        }
        try {
            recognizer?.startListening(intent)
            subtitle = "Dengerin. Ngomong aja."
            faceMood = FaceMood.LISTENING
        } catch (_: Exception) {
            subtitle = "Microphone belum siap."
            faceMood = FaceMood.IDLE
            mainHandler.postDelayed({ beginListening() }, 2500)
        }
    }

    private val robotRecognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            mainHandler.post {
                faceMood = FaceMood.LISTENING
                subtitle = "Dengerin. Ngomong aja."
            }
        }
        override fun onBeginningOfSpeech() {
            mainHandler.post { faceMood = FaceMood.LISTENING }
        }
        override fun onRmsChanged(rmsdB: Float) {
            mainHandler.post { mouthPulse = (rmsdB / 12f).coerceIn(0f, 1f) }
        }
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {
            mainHandler.post { subtitle = "Bentar, gue pikirin..." }
        }
        override fun onError(error: Int) {
            mainHandler.post {
                faceMood = FaceMood.IDLE
                mouthPulse = 0f
                if (error == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS) {
                    subtitle = "Akses microphone belum diizinkan."
                } else {
                    subtitle = "Gue masih dengerin."
                    mainHandler.postDelayed({ beginListening() }, 1100)
                }
            }
        }
        override fun onResults(results: Bundle?) {
            val words = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val heard = words?.firstOrNull()?.trim().orEmpty()
            mainHandler.post {
                mouthPulse = 0f
                if (heard.isNotBlank()) handleVisitorSpeech(heard)
                else beginListening()
            }
        }
        override fun onPartialResults(partialResults: Bundle?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    private fun handleVisitorSpeech(heard: String) {
        val normalized = heard.lowercase(Locale("id", "ID"))
        val reply = when {
            normalized.contains("halo") || normalized.contains("hai") ||
                normalized.contains("selamat pagi") || normalized.contains("selamat siang") ->
                "Iya, halo juga. Ada perlu apa?"
            normalized.contains("andhry") || normalized.contains("andri") ||
                normalized.contains("pak andhry") ->
                "Si kacung? Gue juga lagi nyari. Mau titip pesan?"
            normalized.contains("siapa") && normalized.contains("lo") ->
                "Gue Bigboss. Yang punya ruangan ini. Lo siapa?"
            normalized.contains("bos") || normalized.contains("bigboss") ->
                "Bosnya di sini. Kacungnya belum tentu ada."
            normalized.contains("terima kasih") || normalized.contains("makasih") ->
                "Iya, sama-sama. Jangan lupa mampir lagi."
            else ->
                "Lo bilang, $heard. Otak AI gue belum disambung. Sabar, ya."
        }
        speakLine(reply)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == AUDIO_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                beginListening()
            } else {
                subtitle = "Mic ditolak. Gue jadi pajangan dulu."
                faceMood = FaceMood.IDLE
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            WindowInsetsControllerCompat(window, window.decorView).apply {
                hide(WindowInsetsCompat.Type.systemBars())
                systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    }

    override fun onDestroy() {
        recognizer?.cancel()
        recognizer?.destroy()
        recognizer = null
        speech?.stop()
        speech?.shutdown()
        speech = null
        mainHandler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}

@Composable
private fun RobotScreen(activity: MainActivity) {
    val view = LocalView.current
    androidx.compose.runtime.SideEffect {
        val controller = WindowInsetsControllerCompat(
            (view.context as ComponentActivity).window, view
        )
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    val transition = rememberInfiniteTransition(label = "robot-animation")
    val glance by transition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 5200
                -5f at 0
                0f at 1300 using FastOutSlowInEasing
                5f at 2600 using FastOutSlowInEasing
                0f at 3900 using FastOutSlowInEasing
                -5f at 5200
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "eye-glance"
    )
    val blink by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 4200
                1f at 0
                1f at 1850
                0.08f at 1900
                1f at 1980
                1f at 4200
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "blink"
    )
    val talkPulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 460
                0f at 0
                1f at 150
                0.12f at 330
                0f at 460
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "talk-pulse"
    )

    val mood = activity.faceMood
    Box(
        modifier = Modifier.fillMaxSize().background(RobotBlack),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Canvas(
                modifier = Modifier.fillMaxWidth(0.72f).height(250.dp)
            ) {
                val unit = min(size.width / 520f, size.height / 250f)
                val centerX = size.width / 2f
                val eyeWidth = 142f * unit
                val baseEyeHeight = 112f * unit
                val eyeHeight = baseEyeHeight * mood.eyeScale *
                    blink.coerceAtLeast(0.08f)
                val eyeTop = 36f * unit
                val gap = 82f * unit
                val gazeOffset = glance * unit
                val leftX = centerX - gap / 2f - eyeWidth + gazeOffset
                val rightX = centerX + gap / 2f + gazeOffset
                val eyeRadius = 38f * unit

                drawRoundRect(
                    color = RobotWhite,
                    topLeft = Offset(leftX, eyeTop + (baseEyeHeight - eyeHeight) / 2f),
                    size = Size(eyeWidth, eyeHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(eyeRadius)
                )
                drawRoundRect(
                    color = RobotWhite,
                    topLeft = Offset(rightX, eyeTop + (baseEyeHeight - eyeHeight) / 2f),
                    size = Size(eyeWidth, eyeHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(eyeRadius)
                )

                val mouthCenterY = 211f * unit
                val mouth = Path()
                when (mood.mouth) {
                    "open" -> {
                        val openHeight = (10f + talkPulse * 19f + activity.mouthPulse * 12f) * unit
                        mouth.addOval(
                            androidx.compose.ui.geometry.Rect(
                                centerX - 23f * unit, mouthCenterY - openHeight / 2f,
                                centerX + 23f * unit, mouthCenterY + openHeight / 2f
                            )
                        )
                    }
                    "laugh" -> {
                        mouth.moveTo(centerX - 48f * unit, mouthCenterY - 5f * unit)
                        mouth.quadraticTo(centerX, mouthCenterY + 48f * unit,
                            centerX + 48f * unit, mouthCenterY - 5f * unit)
                    }
                    "flat" -> {
                        mouth.moveTo(centerX - 42f * unit, mouthCenterY)
                        mouth.lineTo(centerX + 42f * unit, mouthCenterY)
                    }
                    "smirk" -> {
                        mouth.moveTo(centerX - 40f * unit, mouthCenterY + 2f * unit)
                        mouth.quadraticTo(centerX + 5f * unit, mouthCenterY + 27f * unit,
                            centerX + 48f * unit, mouthCenterY - 13f * unit)
                    }
                    else -> {
                        mouth.moveTo(centerX - 48f * unit, mouthCenterY - 7f * unit)
                        mouth.quadraticTo(centerX, mouthCenterY + 38f * unit,
                            centerX + 48f * unit, mouthCenterY - 7f * unit)
                    }
                }
                drawPath(
                    path = mouth,
                    color = RobotWhite,
                    style = if (mood.mouth == "open" || mood.mouth == "laugh")
                        androidx.compose.ui.graphics.drawscope.Fill
                    else Stroke(width = 7f * unit)
                )
            }

            Text(
                text = activity.subtitle,
                color = RobotWhite,
                fontSize = 30.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 18.dp, start = 24.dp, end = 24.dp)
            )
        }
    }
}
