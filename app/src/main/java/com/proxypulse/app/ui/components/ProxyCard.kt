package com.proxypulse.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proxypulse.app.data.model.Proxy
import com.proxypulse.app.data.region.Region
import com.proxypulse.app.data.region.Regions
import com.proxypulse.app.ui.theme.BgBottom
import com.proxypulse.app.ui.theme.Brand
import com.proxypulse.app.ui.theme.Outline
import com.proxypulse.app.ui.theme.PingBad
import com.proxypulse.app.ui.theme.PingGood
import com.proxypulse.app.ui.theme.PingMid
import com.proxypulse.app.ui.theme.Surface
import com.proxypulse.app.ui.theme.SurfaceElevated
import com.proxypulse.app.ui.theme.TextMuted
import com.proxypulse.app.ui.theme.TextPrimary
import com.proxypulse.app.ui.theme.TextSecondary

@Composable
fun ProxyCard(
    proxy: Proxy,
    onAddToTelegram: (Proxy) -> Unit,
    onCopy: (Proxy) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Surface)
            .border(1.dp, Outline, RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Flag badge
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                Text(Regions.flagEmoji(proxy.country), fontSize = 24.sp)
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = "${proxy.host}:${proxy.port}",
                    style = TextStyle(
                        color = TextPrimary,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = regionLabel(proxy.country),
                    style = TextStyle(color = TextSecondary, fontSize = 12.sp),
                    maxLines = 1
                )
            }

            PingBadge(proxy.pingMs)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            // Primary action — opens Telegram with the enable-proxy dialog
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brand.accent)
                    .clickable { onAddToTelegram(proxy) },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.Send, contentDescription = null, tint = BgBottom, modifier = Modifier.size(18.dp))
                Text(
                    "  Добавить в Telegram",
                    style = TextStyle(color = BgBottom, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                )
            }

            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceElevated)
                    .border(1.dp, Outline, RoundedCornerShape(14.dp))
                    .clickable { onCopy(proxy) },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.ContentCopy, contentDescription = "Скопировать ссылку", tint = TextSecondary, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun PingBadge(ping: Int) {
    val color = when {
        ping < 0 -> TextMuted
        ping < 150 -> PingGood
        ping < 400 -> PingMid
        else -> PingBad
    }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.14f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(color))
        Text(
            text = if (ping < 0) "—" else "$ping ms",
            style = TextStyle(color = color, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        )
    }
}

private fun regionLabel(code: String): String {
    val region = Regions.regionOf(code)
    val name = when (region) {
        Region.EUROPE -> "Европа"
        Region.AMERICA -> "Америка"
        else -> "—"
    }
    return "$name · ${code.uppercase()}"
}
