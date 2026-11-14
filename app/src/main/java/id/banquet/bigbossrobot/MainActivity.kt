package id.banquet.bigbossrobot

import android.os.Bundle
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
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
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.delay
import kotlin.math.min

private val RobotBlack = Color(0xFF000000)
private val RobotWhite = Color(0xFFFFFFFF)

private data class FaceMood(
    val name: String,
    val subtitle: String,
    val eyeScale: Float,
    val eyeTilt: Float,
    val mouth: String
)

private val demoMoods = listOf(
    FaceMood("IDLE", "Oh, ada orang.", 1.00f, 0f, "smile"),
    FaceMood("CURIOUS", "Mau apa? Jangan cuma lihat-lihat.", 1.12f, -0.10f, "small"),
    FaceMood("SPEAKING", "Selamat datang. Ada perlu apa?", 0.92f, 0f, "open"),
    FaceMood("LAUGHING", "Hahaha. Lumayan juga lo.", 0.72f, 0.12f, "laugh"),
    FaceMood("FLIRTY", "Tumben. Kangen gue, ya?", 1.08f, -0.16f, "smirk"),
    FaceMood("STERN", "Dilarang berisik. Bos lagi kerja.", 0.82f, 0.18f, "flat")
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.BLACK
        window.navigationBarColor = android.graphics.Color.BLACK
        setContent { RobotScreen() }
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
}

@Composable
private fun RobotScreen() {
    val view = LocalView.current
    SideEffect {
        val controller = WindowInsetsControllerCompat(
            (view.context as ComponentActivity).window, view
        )
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    var moodIndex by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(4200)
            moodIndex = (moodIndex + 1) % demoMoods.size
        }
    }
    val mood = demoMoods[moodIndex]

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
                durationMillis = 520
                0f at 0
                1f at 180
                0.15f at 360
                0f at 520
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "talk-pulse"
    )

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
                        val openHeight = (12f + talkPulse * 17f) * unit
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
                    "small" -> {
                        mouth.moveTo(centerX - 25f * unit, mouthCenterY + 4f * unit)
                        mouth.quadraticTo(centerX, mouthCenterY + 22f * unit,
                            centerX + 25f * unit, mouthCenterY + 4f * unit)
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
                text = mood.subtitle,
                color = RobotWhite,
                fontSize = 30.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 18.dp, start = 24.dp, end = 24.dp)
            )
        }
    }
}
