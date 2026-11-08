package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HardwareMetrics
import com.example.data.OverlayConfig
import com.example.ui.overlay.CrosshairView
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun GameArenaSimScreen(
    metrics: HardwareMetrics,
    config: OverlayConfig,
    onBack: () -> Unit
) {
    var score by remember { mutableIntStateOf(0) }
    var shotsFired by remember { mutableIntStateOf(0) }
    var isStressTesting by remember { mutableStateOf(false) }

    // Target animation
    val infiniteTransition = rememberInfiniteTransition(label = "game_sim")
    val targetAnimX by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "targetX"
    )

    // Simulating CPU load spike when stress testing
    LaunchedEffect(isStressTesting) {
        if (isStressTesting) {
            delay(4000)
            isStressTesting = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070B12))
    ) {
        // Full screen PUBG battleground canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Sky / Horizon gradient
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0B192C), Color(0xFF1E3E62), Color(0xFFE88D67)),
                    startY = 0f,
                    endY = h * 0.55f
                ),
                size = Size(w, h * 0.55f)
            )

            // Mountains / Landscape silhouettes
            val mountainPath = Path().apply {
                moveTo(0f, h * 0.55f)
                lineTo(w * 0.2f, h * 0.42f)
                lineTo(w * 0.35f, h * 0.49f)
                lineTo(w * 0.55f, h * 0.38f)
                lineTo(w * 0.75f, h * 0.50f)
                lineTo(w * 0.9f, h * 0.44f)
                lineTo(w, h * 0.55f)
                close()
            }
            drawPath(mountainPath, Color(0xFF152238))

            // Terrain / Ground
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF1F2E22), Color(0xFF141F16), Color(0xFF0D140F)),
                    startY = h * 0.55f,
                    endY = h
                ),
                topLeft = Offset(0f, h * 0.55f),
                size = Size(w, h * 0.45f)
            )

            // Grid perspective lines
            for (i in 0..8) {
                val startX = w * (i / 8f)
                drawLine(
                    color = Color(0x2200FF66),
                    start = Offset(w * 0.5f, h * 0.55f),
                    end = Offset(startX, h),
                    strokeWidth = 1.5f
                )
            }

            // Moving target drone (PUBG Practice Target)
            val targetCenterX = (w * 0.5f) + (targetAnimX * (w * 0.32f))
            val targetCenterY = h * 0.46f
            val targetRadius = 26f

            // Target outer circle
            drawCircle(Color(0xFFFF1744), targetRadius, Offset(targetCenterX, targetCenterY), style = Stroke(3f))
            drawCircle(Color.White, targetRadius * 0.65f, Offset(targetCenterX, targetCenterY), style = Stroke(2.5f))
            drawCircle(Color(0xFFFF1744), targetRadius * 0.3f, Offset(targetCenterX, targetCenterY))
        }

        // Center Crosshair (if enabled in settings)
        if (config.showCrosshair) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CrosshairView(
                    style = config.crosshairStyle,
                    colorType = config.crosshairColor,
                    sizeDp = config.crosshairSize
                )
            }
        } else {
            // Default tactical reticle if none selected
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(24.dp)) {
                    val c = Offset(size.width / 2f, size.height / 2f)
                    drawCircle(Color(0x88FFFFFF), radius = 2f, center = c)
                    drawLine(Color(0xAAFFFFFF), Offset(c.x - 10f, c.y), Offset(c.x - 4f, c.y), 1.5f)
                    drawLine(Color(0xAAFFFFFF), Offset(c.x + 4f, c.y), Offset(c.x + 10f, c.y), 1.5f)
                    drawLine(Color(0xAAFFFFFF), Offset(c.x, c.y - 10f), Offset(c.x, c.y - 4f), 1.5f)
                    drawLine(Color(0xAAFFFFFF), Offset(c.x, c.y + 4f), Offset(c.x, c.y + 10f), 1.5f)
                }
            }
        }

        // Top Game Simulation HUD Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 36.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0x88000000))
                        .testTag("sim_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Exit Simulation",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "PUBG ARENA SIMULATION",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "جرب سحب شريط الفريمات في الشاشة الكاملة",
                        color = Color(0xFF00E5FF),
                        fontSize = 10.sp
                    )
                }
            }

            // Score & Shots
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xAA0B1320))
                    .border(1.dp, Color(0xFF1E2D44), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "HITS: $score / $shotsFired",
                    color = Color(0xFFFFD600),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // In-game FPS & Resource corner preview badge (simulating in-game corner display)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 80.dp, end = 16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xDD0D131F))
                .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00FF66))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isStressTesting) "32 FPS" else "${metrics.fps.roundToInt()} FPS",
                        color = if (isStressTesting) Color(0xFFFF1744) else Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Text(
                    text = "${metrics.batteryTempC.roundToInt()}°C  •  ${metrics.pingMs}ms",
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Bottom Controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 32.dp, start = 20.dp, end = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stress test toggle button (simulates intense PUBG smoke grenade / hot drop frame drop)
                Button(
                    onClick = { isStressTesting = !isStressTesting },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isStressTesting) Color(0xFFFF1744) else Color(0xFF1E2D44)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("stress_test_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isStressTesting) "جاري ضغط الفريمات (Dropping...)" else "اختبار هبوط الفريمات (Drop FPS)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Fire Button
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0xFFFF5252), Color(0xFFB71C1C))
                            )
                        )
                        .clickable {
                            shotsFired++
                            // Check if hit near center
                            if (targetAnimX in -0.3f..0.3f) {
                                score++
                            }
                        }
                        .testTag("fire_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "FIRE",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Text(
                text = "💡 نصيحة: إذا كان عارض الفريمات مشغلاً، يمكنك سحب الشريط الجانبي من حافة الشاشة الآن لتجربة لوحة التحكم!",
                color = Color(0xFFE2E8F0),
                fontSize = 11.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0x99000000))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}
