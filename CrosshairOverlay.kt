package com.example.ui.overlay

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.data.CrosshairColor
import com.example.data.CrosshairStyle

@Composable
fun CrosshairView(
    style: CrosshairStyle,
    colorType: CrosshairColor,
    sizeDp: Int,
    modifier: Modifier = Modifier
) {
    val reticleColor = when (colorType) {
        CrosshairColor.GREEN -> Color(0xFF00FF66)
        CrosshairColor.RED -> Color(0xFFFF1744)
        CrosshairColor.CYAN -> Color(0xFF00F0FF)
        CrosshairColor.YELLOW -> Color(0xFFFFEA00)
    }

    Box(
        modifier = modifier.size(sizeDp.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val strokeWidth = 2.5f

            when (style) {
                CrosshairStyle.DOT -> {
                    drawCircle(
                        color = Color.Black.copy(alpha = 0.6f),
                        radius = (size.width / 6f) + 1.5f,
                        center = center
                    )
                    drawCircle(
                        color = reticleColor,
                        radius = size.width / 6f,
                        center = center
                    )
                }
                CrosshairStyle.CROSS -> {
                    val gap = size.width * 0.16f
                    val lineLen = size.width * 0.38f

                    // Shadow/outline for visibility on bright PUBG snow/sand maps
                    val shadowColor = Color.Black.copy(alpha = 0.7f)
                    val outlineStroke = strokeWidth + 2f

                    // Top
                    drawLine(shadowColor, Offset(center.x, center.y - gap), Offset(center.x, center.y - gap - lineLen), outlineStroke)
                    drawLine(reticleColor, Offset(center.x, center.y - gap), Offset(center.x, center.y - gap - lineLen), strokeWidth)

                    // Bottom
                    drawLine(shadowColor, Offset(center.x, center.y + gap), Offset(center.x, center.y + gap + lineLen), outlineStroke)
                    drawLine(reticleColor, Offset(center.x, center.y + gap), Offset(center.x, center.y + gap + lineLen), strokeWidth)

                    // Left
                    drawLine(shadowColor, Offset(center.x - gap, center.y), Offset(center.x - gap - lineLen, center.y), outlineStroke)
                    drawLine(reticleColor, Offset(center.x - gap, center.y), Offset(center.x - gap - lineLen, center.y), strokeWidth)

                    // Right
                    drawLine(shadowColor, Offset(center.x + gap, center.y), Offset(center.x + gap + lineLen, center.y), outlineStroke)
                    drawLine(reticleColor, Offset(center.x + gap, center.y), Offset(center.x + gap + lineLen, center.y), strokeWidth)

                    // Center dot
                    drawCircle(reticleColor, radius = 2f, center = center)
                }
                CrosshairStyle.SNIPER_CIRCLE -> {
                    val radius = size.width * 0.36f
                    drawCircle(
                        color = Color.Black.copy(alpha = 0.5f),
                        radius = radius,
                        center = center,
                        style = Stroke(width = strokeWidth + 1.5f)
                    )
                    drawCircle(
                        color = reticleColor,
                        radius = radius,
                        center = center,
                        style = Stroke(width = strokeWidth)
                    )
                    // Center dot
                    drawCircle(reticleColor, radius = 2.5f, center = center)
                }
                CrosshairStyle.CHEVRON -> {
                    val path = Path().apply {
                        moveTo(center.x - size.width * 0.28f, center.y + size.height * 0.22f)
                        lineTo(center.x, center.y - size.height * 0.18f)
                        lineTo(center.x + size.width * 0.28f, center.y + size.height * 0.22f)
                    }
                    drawPath(
                        path = path,
                        color = Color.Black.copy(alpha = 0.6f),
                        style = Stroke(width = strokeWidth + 2f)
                    )
                    drawPath(
                        path = path,
                        color = reticleColor,
                        style = Stroke(width = strokeWidth)
                    )
                    drawCircle(reticleColor, radius = 2f, center = center)
                }
            }
        }
    }
}
