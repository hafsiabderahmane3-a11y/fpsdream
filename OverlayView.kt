package com.example.ui.overlay

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.NorthWest
import androidx.compose.material.icons.filled.SouthEast
import androidx.compose.material.icons.filled.SouthWest
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AccentTheme
import com.example.data.CornerAnchor
import com.example.data.DisplayMode
import com.example.data.EdgeSide
import com.example.data.HardwareMetrics
import com.example.data.OverlayConfig
import kotlin.math.roundToInt

@Composable
fun GameOverlayRoot(
    metrics: HardwareMetrics,
    config: OverlayConfig,
    onUpdateConfig: ((OverlayConfig) -> OverlayConfig) -> Unit,
    onTrimMemory: () -> Unit,
    onMoveOverlay: (Float, Float) -> Unit,
    onCloseService: () -> Unit
) {
    val accentColor = when (config.accentTheme) {
        AccentTheme.CYAN -> Color(0xFF00E5FF)
        AccentTheme.CRIMSON -> Color(0xFFFF1744)
        AccentTheme.GREEN -> Color(0xFF00E676)
        AccentTheme.GOLD -> Color(0xFFFFD600)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(config.opacity)
    ) {
        when (config.displayMode) {
            DisplayMode.EDGE_TAB -> {
                // Collapsed Edge Tab (like Game Turbo handle)
                EdgeTabHandle(
                    metrics = metrics,
                    edgeSide = config.edgeSide,
                    accentColor = accentColor,
                    onExpand = {
                        onUpdateConfig { it.copy(displayMode = DisplayMode.EXPANDED_HUD) }
                    },
                    onSwitchSide = {
                        onUpdateConfig {
                            it.copy(edgeSide = if (it.edgeSide == EdgeSide.LEFT) EdgeSide.RIGHT else EdgeSide.LEFT)
                        }
                    },
                    onDragY = { dy -> onMoveOverlay(0f, dy) }
                )
            }

            DisplayMode.CORNER_PILL -> {
                // Floating Corner Pill
                CornerFloatingPill(
                    metrics = metrics,
                    config = config,
                    accentColor = accentColor,
                    onExpand = {
                        onUpdateConfig { it.copy(displayMode = DisplayMode.EXPANDED_HUD) }
                    },
                    onDrag = { dx, dy -> onMoveOverlay(dx, dy) }
                )
            }

            DisplayMode.EXPANDED_HUD -> {
                // Expanded Game Turbo Dashboard
                ExpandedGamingHud(
                    metrics = metrics,
                    config = config,
                    accentColor = accentColor,
                    onCollapseToEdge = {
                        onUpdateConfig { it.copy(displayMode = DisplayMode.EDGE_TAB) }
                    },
                    onSwitchToCornerPill = {
                        onUpdateConfig { it.copy(displayMode = DisplayMode.CORNER_PILL) }
                    },
                    onUpdateConfig = onUpdateConfig,
                    onTrimMemory = onTrimMemory,
                    onCloseService = onCloseService
                )
            }
        }
    }
}

/**
 * Collapsed Edge Tab Handle - sits on the screen edge like Xiaomi Game Turbo / ROG Armoury Crate
 */
@Composable
fun EdgeTabHandle(
    metrics: HardwareMetrics,
    edgeSide: EdgeSide,
    accentColor: Color,
    onExpand: () -> Unit,
    onSwitchSide: () -> Unit,
    onDragY: (Float) -> Unit
) {
    val alignment = if (edgeSide == EdgeSide.LEFT) Alignment.CenterStart else Alignment.CenterEnd
    val cornerShape = if (edgeSide == EdgeSide.LEFT) {
        RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 48.dp),
        contentAlignment = alignment
    ) {
        Row(
            modifier = Modifier
                .clip(cornerShape)
                .background(
                    Brush.horizontalGradient(
                        colors = if (edgeSide == EdgeSide.LEFT) listOf(
                            Color(0xEE0B0F19),
                            Color(0xCC151D2A)
                        ) else listOf(
                            Color(0xCC151D2A),
                            Color(0xEE0B0F19)
                        )
                    )
                )
                .border(1.dp, accentColor.copy(alpha = 0.5f), cornerShape)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        onDragY(dragAmount.y)
                    }
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onExpand
                )
                .padding(horizontal = 8.dp, vertical = 12.dp)
                .testTag("edge_tab_handle"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (edgeSide == EdgeSide.RIGHT) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowLeft,
                    contentDescription = "Expand HUD",
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Glowing pulsating status pip
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${metrics.fps.roundToInt()}",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "FPS",
                    color = accentColor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${metrics.batteryTempC.roundToInt()}°C",
                    color = if (metrics.batteryTempC > 40f) Color(0xFFFF1744) else Color(0xFF94A3B8),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Monospace
                )
            }

            if (edgeSide == EdgeSide.LEFT) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "Expand HUD",
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Compact Floating Corner Pill
 */
@Composable
fun CornerFloatingPill(
    metrics: HardwareMetrics,
    config: OverlayConfig,
    accentColor: Color,
    onExpand: () -> Unit,
    onDrag: (Float, Float) -> Unit
) {
    val alignment = when (config.cornerAnchor) {
        CornerAnchor.TOP_LEFT -> Alignment.TopStart
        CornerAnchor.TOP_RIGHT -> Alignment.TopEnd
        CornerAnchor.BOTTOM_LEFT -> Alignment.BottomStart
        CornerAnchor.BOTTOM_RIGHT -> Alignment.BottomEnd
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = alignment
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xEE0D131F))
                .border(1.2.dp, accentColor.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount.x, dragAmount.y)
                    }
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onExpand
                )
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .testTag("corner_floating_pill"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // FPS badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${metrics.fps.roundToInt()}",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = " FPS",
                    color = accentColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(text = "•", color = Color(0xFF475569))

            // Temp
            Text(
                text = "${metrics.batteryTempC.roundToInt()}°C",
                color = if (metrics.batteryTempC > 41f) Color(0xFFFF1744) else Color(0xFFCBD5E1),
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold
            )

            Text(text = "•", color = Color(0xFF475569))

            // Ping
            Text(
                text = "${metrics.pingMs}ms",
                color = when {
                    metrics.pingMs < 45 -> Color(0xFF00E676)
                    metrics.pingMs < 85 -> Color(0xFFFFD600)
                    else -> Color(0xFFFF1744)
                },
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Full Expanded Gaming HUD Dashboard
 */
@Composable
fun ExpandedGamingHud(
    metrics: HardwareMetrics,
    config: OverlayConfig,
    accentColor: Color,
    onCollapseToEdge: () -> Unit,
    onSwitchToCornerPill: () -> Unit,
    onUpdateConfig: ((OverlayConfig) -> OverlayConfig) -> Unit,
    onTrimMemory: () -> Unit,
    onCloseService: () -> Unit
) {
    val isLeft = config.edgeSide == EdgeSide.LEFT

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x66000000))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onCollapseToEdge
            ),
        contentAlignment = if (isLeft) Alignment.CenterStart else Alignment.CenterEnd
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 340.dp)
                .fillMaxHeight(0.92f)
                .padding(8.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { /* prevent closing when clicking inside */ }
                )
                .testTag("expanded_gaming_hud"),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xF20E1524),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, accentColor.copy(alpha = 0.7f)),
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(accentColor)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "GAME TURBO HUD",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Switch to corner pill
                        IconButton(
                            onClick = onSwitchToCornerPill,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CropFree,
                                contentDescription = "Corner Pill Mode",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Close/Minimize
                        IconButton(
                            onClick = onCollapseToEdge,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Minimize HUD",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Big FPS Section & Sparkline Graph
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF131D2E))
                        .border(1.dp, Color(0xFF1E2D44), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    text = "REAL-TIME FPS",
                                    color = Color(0xFF64748B),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = "${metrics.fps.roundToInt()}",
                                        color = accentColor,
                                        fontSize = 38.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = " FPS",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(accentColor.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${metrics.displayHz}Hz Screen",
                                        color = accentColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Avg: ${metrics.fpsAverage.roundToInt()} | 1% Low: ${metrics.fpsMin1Percent.roundToInt()}",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Real-time FPS Sparkline Chart
                        FpsSparklineChart(
                            history = metrics.fpsHistory,
                            targetHz = metrics.displayHz,
                            accentColor = accentColor,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 4 Hardware Stats Cards (CPU, RAM, Temp, Ping)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        title = "CPU LOAD",
                        value = "${metrics.cpuUsagePercent}%",
                        subtext = "Active cores",
                        color = if (metrics.cpuUsagePercent > 80) Color(0xFFFF1744) else accentColor,
                        progress = metrics.cpuUsagePercent / 100f,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "RAM USED",
                        value = "${metrics.ramUsagePercent}%",
                        subtext = "${metrics.ramUsedMb}MB",
                        color = if (metrics.ramUsagePercent > 85) Color(0xFFFF1744) else Color(0xFF00E676),
                        progress = metrics.ramUsagePercent / 100f,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        title = "BATTERY TEMP",
                        value = "${metrics.batteryTempC.roundToInt()}°C",
                        subtext = metrics.thermalStatus,
                        color = if (metrics.batteryTempC > 41f) Color(0xFFFF1744) else Color(0xFFFFB300),
                        progress = (metrics.batteryTempC / 50f).coerceIn(0f, 1f),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "PUBG PING",
                        value = "${metrics.pingMs}ms",
                        subtext = if (metrics.pingMs < 50) "Excellent" else if (metrics.pingMs < 90) "Good" else "High Ping",
                        color = if (metrics.pingMs < 50) Color(0xFF00E676) else if (metrics.pingMs < 90) Color(0xFFFFD600) else Color(0xFFFF1744),
                        progress = (1f - (metrics.pingMs / 200f)).coerceIn(0.1f, 1f),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quick Game Turbo Actions Row
                Text(
                    text = "GAME TURBO TOOLS",
                    color = Color(0xFF64748B),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // RAM Boost button
                    QuickActionButton(
                        icon = Icons.Default.FlashOn,
                        label = "Boost RAM",
                        accentColor = Color(0xFFFFD600),
                        onClick = onTrimMemory,
                        modifier = Modifier.weight(1f)
                    )

                    // Crosshair toggle button
                    QuickActionButton(
                        icon = if (config.showCrosshair) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        label = if (config.showCrosshair) "Reticle ON" else "Reticle OFF",
                        accentColor = if (config.showCrosshair) Color(0xFF00FF66) else Color(0xFF64748B),
                        onClick = {
                            onUpdateConfig { it.copy(showCrosshair = !it.showCrosshair) }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Opacity Slider
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "HUD Transparency",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                        Text(
                            text = "${(config.opacity * 100).roundToInt()}%",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Slider(
                        value = config.opacity,
                        onValueChange = { newOpacity ->
                            onUpdateConfig { it.copy(opacity = newOpacity) }
                        },
                        valueRange = 0.25f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = accentColor,
                            activeTrackColor = accentColor,
                            inactiveTrackColor = Color(0xFF1E2D44)
                        ),
                        modifier = Modifier.height(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom Dock / Close bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isLeft) "Dock: Left Edge" else "Dock: Right Edge",
                        color = Color(0xFF64748B),
                        fontSize = 11.sp
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1A2438))
                                .clickable {
                                    onUpdateConfig {
                                        it.copy(edgeSide = if (it.edgeSide == EdgeSide.LEFT) EdgeSide.RIGHT else EdgeSide.LEFT)
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Switch Edge",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x33FF1744))
                                .clickable(onClick = onCloseService)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Exit Overlay",
                                color = Color(0xFFFF5252),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtext: String,
    color: Color,
    progress: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF131D2E))
            .border(1.dp, Color(0xFF1E2D44), RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        Column {
            Text(
                text = title,
                color = Color(0xFF64748B),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = value,
                    color = color,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = subtext,
                    color = Color(0xFF94A3B8),
                    fontSize = 9.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = color,
                trackColor = Color(0xFF1E293B),
            )
        }
    }
}

@Composable
fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF162032))
            .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = accentColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun FpsSparklineChart(
    history: List<Float>,
    targetHz: Int,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (history.size < 2) return@Canvas

        val maxFps = (targetHz.toFloat()).coerceAtLeast(60f)
        val minFps = 0f
        val range = maxFps - minFps

        val w = size.width
        val h = size.height

        // Draw target line (e.g. 60 or 90 FPS)
        val targetY = h - ((targetHz.toFloat() / maxFps) * h).coerceIn(0f, h)
        drawLine(
            color = Color(0xFF334155),
            start = Offset(0f, targetY),
            end = Offset(w, targetY),
            strokeWidth = 1f,
            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
        )

        val path = Path()
        val fillPath = Path()

        val stepX = w / (history.size - 1).coerceAtLeast(1)

        history.forEachIndexed { index, fps ->
            val clampedFps = fps.coerceIn(0f, maxFps)
            val x = index * stepX
            val y = h - ((clampedFps / maxFps) * (h - 4f)) - 2f

            if (index == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, h)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }

        fillPath.lineTo(w, h)
        fillPath.close()

        // Fill gradient
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    accentColor.copy(alpha = 0.25f),
                    accentColor.copy(alpha = 0.0f)
                ),
                startY = 0f,
                endY = h
            )
        )

        // Draw line
        drawPath(
            path = path,
            color = accentColor,
            style = Stroke(width = 2.5f, cap = StrokeCap.Round)
        )
    }
}
