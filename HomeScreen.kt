package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HardwareMetrics
import com.example.data.OverlayConfig
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    metrics: HardwareMetrics,
    config: OverlayConfig,
    isOverlayServiceRunning: Boolean,
    hasOverlayPermission: Boolean,
    onToggleService: () -> Unit,
    onRequestPermission: () -> Unit,
    onNavigateCustomization: () -> Unit,
    onNavigateSimArena: () -> Unit,
    onLaunchGame: (String) -> Unit
) {
    val context = LocalContext.current
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0E17))
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // App Bar / Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (isOverlayServiceRunning) Color(0xFF00FF66) else Color(0xFF64748B))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "GAME TURBO FPS",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp
                        )
                    }
                    Text(
                        text = "عداد الفريمات ومراقب أداء الألعاب",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                IconButton(
                    onClick = onNavigateCustomization,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF131B2A))
                        .border(1.dp, Color(0xFF1E2D44), RoundedCornerShape(12.dp))
                        .testTag("settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Overlay Settings",
                        tint = Color(0xFF00E5FF)
                    )
                }
            }
        }

        // Permission Alert if not granted
        if (!hasOverlayPermission) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("permission_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF26190E)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF9800))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Warning",
                                tint = Color(0xFFFF9800),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "إذن الظهور فوق التطبيقات مطلوب",
                                color = Color(0xFFFFD54F),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "لإظهار عارض الفريمات وشريط التحكم داخل لعبة ببجي بدون إخفائه، يلزم منح إذن الظهور فوق الشاشة.",
                            color = Color(0xFFFFE0B2),
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onRequestPermission,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("grant_permission_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "منح الإذن الآن (Enable Overlay)",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // Master Launch Button Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            colors = if (isOverlayServiceRunning) listOf(
                                Color(0xFF003822),
                                Color(0xFF0E1A29)
                            ) else listOf(
                                Color(0xFF131B2A),
                                Color(0xFF182236)
                            )
                        )
                    )
                    .border(
                        width = 1.5.dp,
                        color = if (isOverlayServiceRunning) Color(0xFF00FF66) else Color(0xFF00E5FF).copy(alpha = 0.5f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(20.dp)
                    .testTag("master_launch_card")
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isOverlayServiceRunning) "العارض قيد التشغيل في الشاشة" else "العارض متوقف حالياً",
                        color = if (isOverlayServiceRunning) Color(0xFF00FF66) else Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Big glowing circular toggle
                    Box(
                        modifier = Modifier
                            .scale(if (isOverlayServiceRunning) pulseScale else 1f)
                            .size(88.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = if (isOverlayServiceRunning) listOf(
                                        Color(0xFF00FF66),
                                        Color(0xFF008033)
                                    ) else listOf(
                                        Color(0xFF00E5FF),
                                        Color(0xFF005866)
                                    )
                                )
                            )
                            .clickable(onClick = onToggleService)
                            .testTag("toggle_overlay_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isOverlayServiceRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = if (isOverlayServiceRunning) "Stop Overlay" else "Start Overlay",
                            tint = Color.Black,
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = if (isOverlayServiceRunning) "إيقاف عارض الفريمات" else "تشغيل عارض الفريمات (Start HUD)",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (isOverlayServiceRunning) "اسحب الشريط الجانبي المطوي لتوسيع اللوحة في اللعبة"
                        else "سيظهر شريط جانبي مدمج يمكنك إدخاله وإخراجه أثناء لعب ببجي",
                        color = Color(0xFF64748B),
                        fontSize = 11.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }

        // Live Hardware Specs Summary Grid
        item {
            Column {
                Text(
                    text = "مواصفات الجهاز الحية (LIVE TELEMETRY)",
                    color = Color(0xFF64748B),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DashboardMetricTile(
                        title = "معدل الإطارات",
                        value = "${metrics.fps.roundToInt()} FPS",
                        subtext = "شاشة ${metrics.displayHz}Hz",
                        accentColor = Color(0xFF00E5FF),
                        modifier = Modifier.weight(1f)
                    )
                    DashboardMetricTile(
                        title = "بينج اللعبة",
                        value = "${metrics.pingMs} ms",
                        subtext = if (metrics.pingMs < 50) "سلس جداً" else "متوسط",
                        accentColor = if (metrics.pingMs < 50) Color(0xFF00FF66) else Color(0xFFFFD600),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DashboardMetricTile(
                        title = "استهلاك الرام",
                        value = "${metrics.ramUsagePercent}%",
                        subtext = "${metrics.ramUsedMb} / ${metrics.ramTotalMb}MB",
                        accentColor = Color(0xFF818CF8),
                        modifier = Modifier.weight(1f)
                    )
                    DashboardMetricTile(
                        title = "حرارة البطارية",
                        value = "${metrics.batteryTempC.roundToInt()}°C",
                        subtext = "${metrics.batteryLevel}% شحن",
                        accentColor = if (metrics.batteryTempC > 40f) Color(0xFFFF1744) else Color(0xFFFFB300),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Interactive Simulation Arena Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF1E1430),
                                Color(0xFF101B2E)
                            )
                        )
                    )
                    .border(1.dp, Color(0xFF9C27B0).copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                    .clickable(onClick = onNavigateSimArena)
                    .padding(16.dp)
                    .testTag("sim_arena_banner")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.SportsEsports,
                                contentDescription = null,
                                tint = Color(0xFFE040FB),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ساحة تجربة ببجي التفاعلية",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "جرب سحب وإدخال شريط الفريمات ونقطة التصويب في بيئة لعب تحاكي ببجي بدون مغادرة التطبيق!",
                            color = Color(0xFFCBD5E1),
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFE040FB).copy(alpha = 0.2f))
                            .border(1.dp, Color(0xFFE040FB), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "تجربة الآن",
                            color = Color(0xFFE040FB),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Quick Game Launcher
        item {
            Column {
                Text(
                    text = "تشغيل الألعاب المباشر (LAUNCH GAMES)",
                    color = Color(0xFF64748B),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GameLaunchButton(
                        gameName = "PUBG Mobile",
                        subtext = "Global / العالمية",
                        packageName = "com.tencent.ig",
                        accentColor = Color(0xFFFFB300),
                        onClick = { onLaunchGame("com.tencent.ig") },
                        modifier = Modifier.weight(1f)
                    )
                    GameLaunchButton(
                        gameName = "PUBG Mobile KR",
                        subtext = "الكورية",
                        packageName = "com.pubg.krmobile",
                        accentColor = Color(0xFF00E5FF),
                        onClick = { onLaunchGame("com.pubg.krmobile") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GameLaunchButton(
                        gameName = "Free Fire",
                        subtext = "فري فاير",
                        packageName = "com.dts.freefireth",
                        accentColor = Color(0xFFFF5252),
                        onClick = { onLaunchGame("com.dts.freefireth") },
                        modifier = Modifier.weight(1f)
                    )
                    GameLaunchButton(
                        gameName = "Call of Duty",
                        subtext = "كول أوف ديوتي",
                        packageName = "com.activision.callofduty.shooter",
                        accentColor = Color(0xFF00E676),
                        onClick = { onLaunchGame("com.activision.callofduty.shooter") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun DashboardMetricTile(
    title: String,
    value: String,
    subtext: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF131B2A))
            .border(1.dp, Color(0xFF1E2D44), RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Column {
            Text(
                text = title,
                color = Color(0xFF94A3B8),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = accentColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtext,
                color = Color(0xFF64748B),
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun GameLaunchButton(
    gameName: String,
    subtext: String,
    packageName: String,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF131B2A))
            .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = gameName,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtext,
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp
                )
            }
            Icon(
                imageVector = Icons.Default.OpenInNew,
                contentDescription = "Launch $gameName",
                tint = accentColor,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
