package com.proxypulse.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proxypulse.app.data.region.Region
import com.proxypulse.app.ui.theme.Cyan
import com.proxypulse.app.ui.theme.Outline
import com.proxypulse.app.ui.theme.SurfaceElevated
import com.proxypulse.app.ui.theme.TextSecondary
import com.proxypulse.app.ui.theme.BgBottom

@Composable
fun FilterBar(
    selected: Region,
    counts: Map<Region, Int>,
    onSelect: (Region) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Chip("Все", counts[Region.ALL] ?: 0, selected == Region.ALL) { onSelect(Region.ALL) }
        Chip("🇪🇺 Европа", counts[Region.EUROPE] ?: 0, selected == Region.EUROPE) { onSelect(Region.EUROPE) }
        Chip("🌎 Америка", counts[Region.AMERICA] ?: 0, selected == Region.AMERICA) { onSelect(Region.AMERICA) }
    }
}

@Composable
private fun Chip(label: String, count: Int, active: Boolean, onClick: () -> Unit) {
    val bg by animateColorAsState(if (active) Cyan else SurfaceElevated, label = "bg")
    val fg = if (active) BgBottom else TextSecondary
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .border(1.dp, if (active) Color.Transparent else Outline, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(label, style = TextStyle(color = fg, fontWeight = FontWeight.SemiBold, fontSize = 13.sp))
        Text(
            count.toString(),
            style = TextStyle(
                color = if (active) BgBottom.copy(alpha = 0.7f) else TextSecondary.copy(alpha = 0.7f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
}
