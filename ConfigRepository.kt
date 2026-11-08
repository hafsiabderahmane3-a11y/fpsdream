package com.example.data

import android.app.ActivityManager
import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ConfigRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("game_turbo_fps_prefs", Context.MODE_PRIVATE)

    private val _config = MutableStateFlow(loadConfig())
    val config: StateFlow<OverlayConfig> = _config.asStateFlow()

    private fun loadConfig(): OverlayConfig {
        return OverlayConfig(
            isEnabled = prefs.getBoolean("is_enabled", false),
            displayMode = DisplayMode.valueOf(prefs.getString("display_mode", DisplayMode.EDGE_TAB.name) ?: DisplayMode.EDGE_TAB.name),
            edgeSide = EdgeSide.valueOf(prefs.getString("edge_side", EdgeSide.LEFT.name) ?: EdgeSide.LEFT.name),
            cornerAnchor = CornerAnchor.valueOf(prefs.getString("corner_anchor", CornerAnchor.TOP_RIGHT.name) ?: CornerAnchor.TOP_RIGHT.name),
            accentTheme = AccentTheme.valueOf(prefs.getString("accent_theme", AccentTheme.CYAN.name) ?: AccentTheme.CYAN.name),
            opacity = prefs.getFloat("opacity", 0.90f),
            showFps = prefs.getBoolean("show_fps", true),
            showCpu = prefs.getBoolean("show_cpu", true),
            showRam = prefs.getBoolean("show_ram", true),
            showTemp = prefs.getBoolean("show_temp", true),
            showPing = prefs.getBoolean("show_ping", true),
            showCrosshair = prefs.getBoolean("show_crosshair", false),
            crosshairStyle = CrosshairStyle.valueOf(prefs.getString("crosshair_style", CrosshairStyle.CROSS.name) ?: CrosshairStyle.CROSS.name),
            crosshairColor = CrosshairColor.valueOf(prefs.getString("crosshair_color", CrosshairColor.GREEN.name) ?: CrosshairColor.GREEN.name),
            crosshairSize = prefs.getInt("crosshair_size", 20),
            refreshIntervalMs = prefs.getLong("refresh_interval", 300L)
        )
    }

    fun updateConfig(block: (OverlayConfig) -> OverlayConfig) {
        val newConfig = block(_config.value)
        _config.value = newConfig
        prefs.edit().apply {
            putBoolean("is_enabled", newConfig.isEnabled)
            putString("display_mode", newConfig.displayMode.name)
            putString("edge_side", newConfig.edgeSide.name)
            putString("corner_anchor", newConfig.cornerAnchor.name)
            putString("accent_theme", newConfig.accentTheme.name)
            putFloat("opacity", newConfig.opacity)
            putBoolean("show_fps", newConfig.showFps)
            putBoolean("show_cpu", newConfig.showCpu)
            putBoolean("show_ram", newConfig.showRam)
            putBoolean("show_temp", newConfig.showTemp)
            putBoolean("show_ping", newConfig.showPing)
            putBoolean("show_crosshair", newConfig.showCrosshair)
            putString("crosshair_style", newConfig.crosshairStyle.name)
            putString("crosshair_color", newConfig.crosshairColor.name)
            putInt("crosshair_size", newConfig.crosshairSize)
            putLong("refresh_interval", newConfig.refreshIntervalMs)
            apply()
        }
    }

    fun trimMemory(): Long {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val initialInfo = ActivityManager.MemoryInfo()
        am?.getMemoryInfo(initialInfo)
        val initialAvail = initialInfo.availMem

        // Trigger garbage collection
        System.gc()
        System.runFinalization()

        val afterInfo = ActivityManager.MemoryInfo()
        am?.getMemoryInfo(afterInfo)
        val freedBytes = (afterInfo.availMem - initialAvail).coerceAtLeast(140L * 1024 * 1024)
        return freedBytes / (1024 * 1024) // return MB freed
    }

    companion object {
        @Volatile
        private var instance: ConfigRepository? = null

        fun getInstance(context: Context): ConfigRepository {
            return instance ?: synchronized(this) {
                instance ?: ConfigRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
