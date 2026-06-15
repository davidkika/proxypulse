package com.proxypulse.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proxypulse.app.ui.theme.Cyan
import com.proxypulse.app.ui.theme.Indigo
import com.proxypulse.app.ui.theme.TextSecondary
import com.proxypulse.app.ui.theme.TextPrimary

/** The animated mark: a heartbeat line with a glowing dot running into a plane. */
@Composable
fun PulseLogo(modifier: Modifier = Modifier, size: Dp = 44.dp) {
    val infinite = rememberInfiniteTransition(label = "logo")
    val progress by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )

    Canvas(modifier.size(size)) {
        val s = this.size.minDimension / 48f

        val pulse = Path().apply {
            moveTo(6f * s, 24f * s)
            lineTo(11f * s, 24f * s)
            lineTo(14f * s, 16f * s)
            lineTo(17f * s, 32f * s)
            lineTo(20f * s, 24f * s)
        }
        val plane = Path().apply {
            moveTo(20f * s, 35f * s)
            lineTo(40f * s, 24f * s)
            lineTo(20f * s, 13f * s)
            lineTo(20f * s, 20f * s)
            lineTo(31f * s, 24f * s)
            lineTo(20f * s, 28f * s)
            close()
        }

        drawPath(
            path = pulse,
            color = Cyan,
            style = Stroke(width = 3f * s, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        drawPath(
            path = plane,
            brush = Brush.linearGradient(
                colors = listOf(Cyan, Indigo),
                start = Offset(20f * s, 13f * s),
                end = Offset(40f * s, 35f * s)
            )
        )

        // Glowing dot travelling along the pulse line.
        val pm = PathMeasure().apply { setPath(pulse, false) }
        if (pm.length > 0f) {
            val pos = pm.getPosition(progress * pm.length)
            drawCircle(color = Cyan.copy(alpha = 0.35f), radius = 5.5f * s, center = pos)
            drawCircle(color = Color.White, radius = 2.4f * s, center = pos)
        }
    }
}

/** Logo + wordmark + tagline, used in the header. */
@Composable
fun BrandHeader(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PulseLogo(size = 46.dp)
        Column {
            Row {
                Text(
                    "Proxy",
                    style = TextStyle(
                        color = TextPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        letterSpacing = 0.5.sp
                    )
                )
                Text(
                    "Pulse",
                    style = TextStyle(
                        brush = Brush.linearGradient(listOf(Cyan, Indigo)),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        letterSpacing = 0.5.sp
                    )
                )
            }
            Text(
                "Живые MTProto-прокси · ЕС + Америка",
                style = TextStyle(color = TextSecondary, fontSize = 12.sp)
            )
        }
    }
}
