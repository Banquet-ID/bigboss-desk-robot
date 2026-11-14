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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlin.math.min

private val RobotBlack = Color(0xFF000000)
private val RobotWhite = Color(0xFFFFFFFF)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.BLACK
        window.navigationBarColor = android.graphics.Color.BLACK

        setContent {
            RobotScreen()
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
}

@Composable
private fun RobotScreen() {
    val view = LocalView.current
    SideEffect {
        val controller = WindowInsetsControllerCompat(
            (view.context as ComponentActivity).window,
            view
        )
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    val transition = rememberInfiniteTransition(label = "robot-idle")
    val glance = transition.animateFloat(
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
    val blink = transition.animateFloat(
        initialValue = 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 4200
                1f at 0
                1f at 1850
                0.12f at 1900
                1f at 1980
                1f at 4200
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "blink"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RobotBlack),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth(0.72f)
                    .height(250.dp)
            ) {
                val unit = min(size.width / 520f, size.height / 250f)
                val eyeWidth = 142f * unit
                val eyeHeight = 112f * unit * blink.value.coerceAtLeast(0.08f)
                val eyeTop = 36f * unit
                val gap = 82f * unit
                val centerX = size.width / 2f
                val gazeOffset = glance.value * unit
                val leftX = centerX - gap / 2f - eyeWidth + gazeOffset
                val rightX = centerX + gap / 2f + gazeOffset

                drawRoundRect(
                    color = RobotWhite,
                    topLeft = Offset(leftX, eyeTop + (112f * unit - eyeHeight) / 2f),
                    size = Size(eyeWidth, eyeHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(38f * unit)
                )
                drawRoundRect(
                    color = RobotWhite,
                    topLeft = Offset(rightX, eyeTop + (112f * unit - eyeHeight) / 2f),
                    size = Size(eyeWidth, eyeHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(38f * unit)
                )

                val smile = Path().apply {
                    moveTo(centerX - 48f * unit, 204f * unit)
                    quadraticTo(
                        centerX,
                        244f * unit,
                        centerX + 48f * unit,
                        204f * unit
                    )
                }
                drawPath(
                    path = smile,
                    color = RobotWhite,
                    style = Stroke(width = 7f * unit)
                )
            }

            Text(
                text = "Oh, ada orang.",
                color = RobotWhite,
                fontSize = 30.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 18.dp, start = 24.dp, end = 24.dp)
            )
        }
    }
}
