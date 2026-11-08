package com.example.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AccentTheme
import com.example.data.CornerAnchor
import com.example.data.CrosshairColor
import com.example.data.CrosshairStyle
import com.example.data.DisplayMode
import com.example.data.EdgeSide
import com.example.data.OverlayConfig
import com.example.ui.overlay.CrosshairView
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomizationScreen(
    config: OverlayConfig,
    onUpdateConfig: ((OverlayConfig) -> OverlayConfig) -> Unit,
    onBack: () -> Unit
) {
    val themeAccent = when (config.accentTheme) {
        AccentTheme.CYAN -> Color(0xFF00E5FF)
        AccentTheme.CRIMSON -> Color(0xFFFF1744)
        AccentTheme.GREEN -> Color(0xFF00E676)
        AccentTheme.GOLD -> Color(0xFFFFD600)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0E17))
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "تخصيص عارض الفريمات (Overlay Settings)",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0E1524))
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(6.dp)) }

            // Theme Color Selector
            item {
                SettingsSection(title = "لون الواجهة والتأثيرات (THEME COLOR)") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ThemeColorOption(
                            name = "Cyan",
                            color = Color(0xFF00E5FF),
                            isSelected = config.accentTheme == AccentTheme.CYAN,
                            onClick = { onUpdateConfig { it.copy(accentTheme = AccentTheme.CYAN) } },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeColorOption(
                            name = "Crimson",
                            color = Color(0xFFFF1744),
                            isSelected = config.accentTheme == AccentTheme.CRIMSON,
                            onClick = { onUpdateConfig { it.copy(accentTheme = AccentTheme.CRIMSON) } },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeColorOption(
                            name = "Razer",
                            color = Color(0xFF00E676),
                            isSelected = config.accentTheme == AccentTheme.GREEN,
                            onClick = { onUpdateConfig { it.copy(accentTheme = AccentTheme.GREEN) } },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeColorOption(
                            name = "Gold",
                            color = Color(0xFFFFD600),
                            isSelected = config.accentTheme == AccentTheme.GOLD,
                            onClick = { onUpdateConfig { it.copy(accentTheme = AccentTheme.GOLD) } },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Display Mode Selector
            item {
                SettingsSection(title = "طريقة العرض الافتراضية (DISPLAY MODE)") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ModeSelectionTile(
                            title = "شريط الحافة المطوي (Edge Tab)",
                            description = "شريط مدمج على حافة الشاشة ينزلق ويدخل بلمسة واحدة مثل Game Turbo في الهواتف المخصصة للألعاب",
                            isSelected = config.displayMode == DisplayMode.EDGE_TAB,
                            accentColor = themeAccent,
                            onClick = { onUpdateConfig { it.copy(displayMode = DisplayMode.EDGE_TAB) } }
                        )

                        ModeSelectionTile(
                            title = "كبسولة زاوية الشاشة (Corner Floating Pill)",
                            description = "كبسولة صغيرة عائمة تعرض الفريمات والحرارة بجانب خريطة ببجي بدون حجب الرؤية",
                            isSelected = config.displayMode == DisplayMode.CORNER_PILL,
                            accentColor = themeAccent,
                            onClick = { onUpdateConfig { it.copy(displayMode = DisplayMode.CORNER_PILL) } }
                        )

                        ModeSelectionTile(
                            title = "لوحة التحكم الموسعة (Expanded Gaming HUD)",
                            description = "عرض كامل مع مقاييس السرعة والرسم البياني ومقاييس المعالج والرام",
                            isSelected = config.displayMode == DisplayMode.EXPANDED_HUD,
                            accentColor = themeAccent,
                            onClick = { onUpdateConfig { it.copy(displayMode = DisplayMode.EXPANDED_HUD) } }
                        )
                    }
                }
            }

            // Edge Side & Corner Positions
            item {
                SettingsSection(title = "موقع العرض على الشاشة (POSITIONING)") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "حافة الشريط المطوي", color = Color(0xFFCBD5E1), fontSize = 13.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ChoicePill(
                                    label = "يسار (Left)",
                                    isSelected = config.edgeSide == EdgeSide.LEFT,
                                    accentColor = themeAccent,
                                    onClick = { onUpdateConfig { it.copy(edgeSide = EdgeSide.LEFT) } }
                                )
                                ChoicePill(
                                    label = "يمين (Right)",
                                    isSelected = config.edgeSide == EdgeSide.RIGHT,
                                    accentColor = themeAccent,
                                    onClick = { onUpdateConfig { it.copy(edgeSide = EdgeSide.RIGHT) } }
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "زاوية الكبسولة العائمة", color = Color(0xFFCBD5E1), fontSize = 13.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                ChoicePill(
                                    label = "أعلى اليمين",
                                    isSelected = config.cornerAnchor == CornerAnchor.TOP_RIGHT,
                                    accentColor = themeAccent,
                                    onClick = { onUpdateConfig { it.copy(cornerAnchor = CornerAnchor.TOP_RIGHT) } }
                                )
                                ChoicePill(
                                    label = "أعلى اليسار",
                                    isSelected = config.cornerAnchor == CornerAnchor.TOP_LEFT,
                                    accentColor = themeAccent,
                                    onClick = { onUpdateConfig { it.copy(cornerAnchor = CornerAnchor.TOP_LEFT) } }
                                )
                            }
                        }
                    }
                }
            }

            // Transparency / Opacity Slider
            item {
                SettingsSection(title = "شفافية العارض (TRANSPARENCY)") {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "درجة وضوح اللوحة", color = Color(0xFFCBD5E1), fontSize = 13.sp)
                            Text(
                                text = "${(config.opacity * 100).roundToInt()}%",
                                color = themeAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Slider(
                            value = config.opacity,
                            onValueChange = { onUpdateConfig { c -> c.copy(opacity = it) } },
                            valueRange = 0.25f..1.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = themeAccent,
                                activeTrackColor = themeAccent,
                                inactiveTrackColor = Color(0xFF1E2D44)
                            )
                        )
                    }
                }
            }

            // Metrics Toggles
            item {
                SettingsSection(title = "المقاييس المعروضة (DISPLAYED METRICS)") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        MetricToggleRow("معدل الإطارات (Real FPS Counter)", config.showFps) {
                            onUpdateConfig { it.copy(showFps = !it.showFps) }
                        }
                        MetricToggleRow("استهلاك المعالج (CPU Load %)", config.showCpu) {
                            onUpdateConfig { it.copy(showCpu = !it.showCpu) }
                        }
                        MetricToggleRow("استهلاك الذاكرة (RAM Usage)", config.showRam) {
                            onUpdateConfig { it.copy(showRam = !it.showRam) }
                        }
                        MetricToggleRow("حرارة وحالة البطارية (Battery Temp)", config.showTemp) {
                            onUpdateConfig { it.copy(showTemp = !it.showTemp) }
                        }
                        MetricToggleRow("بينج وسرعة الاستجابة (Network Ping)", config.showPing) {
                            onUpdateConfig { it.copy(showPing = !it.showPing) }
                        }
                    }
                }
            }

            // Precision Crosshair Settings
            item {
                SettingsSection(title = "نقطة التصويب المخصصة لـ ببجي (CUSTOM RETICLE)") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "تفعيل نقطة التصويب (Crosshair)",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "تثبيت علامة تصويب وسط الشاشة لتحسين إطلاق النار من الورك (Hip Fire)",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )
                            }
                            Switch(
                                checked = config.showCrosshair,
                                onCheckedChange = { isChecked ->
                                    onUpdateConfig { it.copy(showCrosshair = isChecked) }
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = themeAccent)
                            )
                        }

                        if (config.showCrosshair) {
                            // Live Crosshair Preview Box
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(90.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF0F172A))
                                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                CrosshairView(
                                    style = config.crosshairStyle,
                                    colorType = config.crosshairColor,
                                    sizeDp = config.crosshairSize
                                )
                                Text(
                                    text = "معاينة حية (Live Preview)",
                                    color = Color(0xFF475569),
                                    fontSize = 10.sp,
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(8.dp)
                                )
                            }

                            // Style selection
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ChoicePill(
                                    label = "نقطة (Dot)",
                                    isSelected = config.crosshairStyle == CrosshairStyle.DOT,
                                    accentColor = themeAccent,
                                    onClick = { onUpdateConfig { it.copy(crosshairStyle = CrosshairStyle.DOT) } },
                                    modifier = Modifier.weight(1f)
                                )
                                ChoicePill(
                                    label = "صليب (Cross)",
                                    isSelected = config.crosshairStyle == CrosshairStyle.CROSS,
                                    accentColor = themeAccent,
                                    onClick = { onUpdateConfig { it.copy(crosshairStyle = CrosshairStyle.CROSS) } },
                                    modifier = Modifier.weight(1f)
                                )
                                ChoicePill(
                                    label = "قناص (Circle)",
                                    isSelected = config.crosshairStyle == CrosshairStyle.SNIPER_CIRCLE,
                                    accentColor = themeAccent,
                                    onClick = { onUpdateConfig { it.copy(crosshairStyle = CrosshairStyle.SNIPER_CIRCLE) } },
                                    modifier = Modifier.weight(1f)
                                )
                                ChoicePill(
                                    label = "سهم (Chevron)",
                                    isSelected = config.crosshairStyle == CrosshairStyle.CHEVRON,
                                    accentColor = themeAccent,
                                    onClick = { onUpdateConfig { it.copy(crosshairStyle = CrosshairStyle.CHEVRON) } },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Color selection
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ChoicePill(
                                    label = "أخضر نيون",
                                    isSelected = config.crosshairColor == CrosshairColor.GREEN,
                                    accentColor = Color(0xFF00FF66),
                                    onClick = { onUpdateConfig { it.copy(crosshairColor = CrosshairColor.GREEN) } },
                                    modifier = Modifier.weight(1f)
                                )
                                ChoicePill(
                                    label = "أحمر قرمزي",
                                    isSelected = config.crosshairColor == CrosshairColor.RED,
                                    accentColor = Color(0xFFFF1744),
                                    onClick = { onUpdateConfig { it.copy(crosshairColor = CrosshairColor.RED) } },
                                    modifier = Modifier.weight(1f)
                                )
                                ChoicePill(
                                    label = "أزرق سماوي",
                                    isSelected = config.crosshairColor == CrosshairColor.CYAN,
                                    accentColor = Color(0xFF00F0FF),
                                    onClick = { onUpdateConfig { it.copy(crosshairColor = CrosshairColor.CYAN) } },
                                    modifier = Modifier.weight(1f)
                                )
                                ChoicePill(
                                    label = "أصفر لامع",
                                    isSelected = config.crosshairColor == CrosshairColor.YELLOW,
                                    accentColor = Color(0xFFFFEA00),
                                    onClick = { onUpdateConfig { it.copy(crosshairColor = CrosshairColor.YELLOW) } },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Size slider
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "حجم علامة التصويب", color = Color(0xFFCBD5E1), fontSize = 12.sp)
                                    Text(text = "${config.crosshairSize} dp", color = themeAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Slider(
                                    value = config.crosshairSize.toFloat(),
                                    onValueChange = { onUpdateConfig { c -> c.copy(crosshairSize = it.roundToInt()) } },
                                    valueRange = 12f..36f,
                                    colors = SliderDefaults.colors(thumbColor = themeAccent, activeTrackColor = themeAccent)
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF131B2A))
            .border(1.dp, Color(0xFF1E2D44), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Text(
            text = title,
            color = Color(0xFF64748B),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        content()
    }
}

@Composable
fun ThemeColorOption(
    name: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) color.copy(alpha = 0.2f) else Color(0xFF182236))
            .border(
                1.5.dp,
                if (isSelected) color else Color(0xFF26354D),
                RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = name,
                color = if (isSelected) Color.White else Color(0xFF94A3B8),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ModeSelectionTile(
    title: String,
    description: String,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) accentColor.copy(alpha = 0.12f) else Color(0xFF182236))
            .border(
                1.5.dp,
                if (isSelected) accentColor else Color(0xFF26354D),
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = if (isSelected) Color.White else Color(0xFFE2E8F0),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
            if (isSelected) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(accentColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.Black,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ChoicePill(
    label: String,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) accentColor.copy(alpha = 0.2f) else Color(0xFF1A2438))
            .border(
                1.dp,
                if (isSelected) accentColor else Color(0xFF2A3850),
                RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) accentColor else Color(0xFF94A3B8),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun MetricToggleRow(
    title: String,
    isChecked: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, color = Color(0xFFCBD5E1), fontSize = 13.sp)
        Switch(
            checked = isChecked,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF00E5FF),
                checkedTrackColor = Color(0xFF005866)
            )
        )
    }
}
