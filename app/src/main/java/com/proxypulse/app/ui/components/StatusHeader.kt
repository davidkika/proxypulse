package com.proxypulse.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proxypulse.app.ui.theme.BgBottom
import com.proxypulse.app.ui.theme.Brand
import com.proxypulse.app.ui.theme.Cyan
import com.proxypulse.app.ui.theme.Outline
import com.proxypulse.app.ui.theme.PingGood
import com.proxypulse.app.ui.theme.SurfaceElevated
import com.proxypulse.app.ui.theme.TextMuted
import com.proxypulse.app.ui.theme.TextPrimary
import com.proxypulse.app.ui.theme.TextSecondary

@Composable
fun StatusHeader(
    liveCount: Int,
    lastUpdated: Long,
    nextRefreshInSec: Long,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceElevated)
            .border(1.dp, Outline, RoundedCornerShape(20.dp))
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Pulsing live dot
        LivePulseDot()

        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = liveCount.toString(),
                    style = TextStyle(
                        brush = Brand.accent,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 30.sp
                    )
                )
                Text(
                    "  прокси в сети",
                    style = TextStyle(color = TextSecondary, fontSize = 13.sp),
                    modifier = Modifier.padding(bottom = 5.dp)
                )
            }
            Text(
                text = subtitle(lastUpdated, nextRefreshInSec, isRefreshing),
                style = TextStyle(color = TextMuted, fontSize = 12.sp)
            )
        }

        RefreshButton(isRefreshing = isRefreshing, onClick = onRefresh)
    }
}

@Composable
private fun LivePulseDot() {
    val t = rememberInfiniteTransition(label = "live")
    val scale by t.animateFloat(
        0.6f, 1f,
        infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Reverse),
        label = "s"
    )
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(14.dp)) {
        Box(
            Modifier
                .size((14 * scale).dp)
                .clip(CircleShape)
                .background(PingGood.copy(alpha = 0.25f))
        )
        Box(
            Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(PingGood)
        )
    }
}

@Composable
private fun RefreshButton(isRefreshing: Boolean, onClick: () -> Unit) {
    val t = rememberInfiniteTransition(label = "spin")
    val angle by t.animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Restart),
        label = "a"
    )
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(Cyan, com.proxypulse.app.ui.theme.Indigo)))
            .clickable(enabled = !isRefreshing, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Refresh,
            contentDescription = "Обновить",
            tint = BgBottom,
            modifier = Modifier
                .size(22.dp)
                .then(if (isRefreshing) Modifier.rotate(angle) else Modifier)
        )
    }
}

private fun subtitle(lastUpdated: Long, nextSec: Long, refreshing: Boolean): String {
    if (refreshing) return "Проверяем прокси…"
    if (lastUpdated == 0L) return "Ещё не обновлялось"
    val ago = (System.currentTimeMillis() - lastUpdated) / 1000
    val agoText = when {
        ago < 60 -> "только что"
        ago < 3600 -> "${ago / 60} мин назад"
        else -> "${ago / 3600} ч назад"
    }
    val mm = nextSec / 60
    val ss = nextSec % 60
    return "Обновлено $agoText · след. через %02d:%02d".format(mm, ss)
}
