package com.example.data

data class HardwareMetrics(
    val fps: Float = 60.0f,
    val fpsAverage: Float = 60.0f,
    val fpsMin1Percent: Float = 55.0f,
    val fpsHistory: List<Float> = emptyList(),
    val displayHz: Int = 60,
    val cpuUsagePercent: Int = 24,
    val ramUsedMb: Long = 3200,
    val ramTotalMb: Long = 8192,
    val ramUsagePercent: Int = 39,
    val batteryTempC: Float = 36.5f,
    val batteryLevel: Int = 85,
    val isCharging: Boolean = false,
    val pingMs: Int = 28,
    val thermalStatus: String = "Normal"
)
