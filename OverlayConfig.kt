package com.example.data

enum class DisplayMode {
    EDGE_TAB,     // Collapsed gaming tab on screen edge (slide to expand)
    CORNER_PILL,  // Compact floating pill in screen corner
    EXPANDED_HUD  // Full HUD open with all meters and quick boost tools
}

enum class EdgeSide {
    LEFT,
    RIGHT
}

enum class CornerAnchor {
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT
}

enum class AccentTheme {
    CYAN,
    CRIMSON,
    GREEN,
    GOLD
}

enum class CrosshairStyle {
    DOT,
    CROSS,
    SNIPER_CIRCLE,
    CHEVRON
}

enum class CrosshairColor {
    GREEN,
    RED,
    CYAN,
    YELLOW
}

data class OverlayConfig(
    val isEnabled: Boolean = false,
    val displayMode: DisplayMode = DisplayMode.EDGE_TAB,
    val edgeSide: EdgeSide = EdgeSide.LEFT,
    val cornerAnchor: CornerAnchor = CornerAnchor.TOP_RIGHT,
    val accentTheme: AccentTheme = AccentTheme.CYAN,
    val opacity: Float = 0.90f,
    val showFps: Boolean = true,
    val showCpu: Boolean = true,
    val showRam: Boolean = true,
    val showTemp: Boolean = true,
    val showPing: Boolean = true,
    val showCrosshair: Boolean = false,
    val crosshairStyle: CrosshairStyle = CrosshairStyle.CROSS,
    val crosshairColor: CrosshairColor = CrosshairColor.GREEN,
    val crosshairSize: Int = 20,
    val refreshIntervalMs: Long = 300L
)
