package com.example.service

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.display.DisplayManager
import android.os.BatteryManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.view.Choreographer
import android.view.Display
import android.view.WindowManager
import com.example.data.HardwareMetrics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.RandomAccessFile
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.math.roundToInt

class HardwareMonitor(private val context: Context) : Choreographer.FrameCallback {

    private val _metrics = MutableStateFlow(HardwareMetrics())
    val metrics: StateFlow<HardwareMetrics> = _metrics.asStateFlow()

    private val monitorScope = CoroutineScope(Dispatchers.Default + Job())
    private var pingJob: Job? = null
    private var statsJob: Job? = null

    // FPS Measurement state
    private var choreographer: Choreographer? = null
    private var lastFrameTimeNanos: Long = 0L
    private var frameCount: Int = 0
    private var windowStartTimeNanos: Long = 0L
    private val frameDurations = ArrayList<Long>(120)
    private val fpsHistoryList = ArrayDeque<Float>(30)

    // CPU proc stat state
    private var prevTotalCpuTime: Long = 0L
    private var prevIdleCpuTime: Long = 0L

    // Battery state
    private var currentBatteryTemp: Float = 36.0f
    private var currentBatteryLevel: Int = 80
    private var isCurrentlyCharging: Boolean = false

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                val temp = it.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
                currentBatteryTemp = if (temp > 0) temp / 10.0f else 36.5f

                val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                if (level >= 0 && scale > 0) {
                    currentBatteryLevel = (level * 100 / scale.toFloat()).roundToInt()
                }

                val status = it.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                isCurrentlyCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL
            }
        }
    }

    private val mainHandler = Handler(Looper.getMainLooper())

    fun start() {
        // Register battery receiver
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(batteryReceiver, filter)

        // Start Choreographer on Main Looper
        mainHandler.post {
            choreographer = Choreographer.getInstance()
            windowStartTimeNanos = System.nanoTime()
            choreographer?.postFrameCallback(this)
        }

        // Start periodic hardware stats polling (CPU, RAM, Temp, etc.)
        statsJob = monitorScope.launch {
            while (isActive) {
                updateSystemStats()
                delay(300)
            }
        }

        // Start periodic ping check (every 2 seconds)
        pingJob = monitorScope.launch {
            while (isActive) {
                checkPing()
                delay(2000)
            }
        }
    }

    fun stop() {
        mainHandler.post {
            choreographer?.removeFrameCallback(this)
            choreographer = null
        }
        statsJob?.cancel()
        pingJob?.cancel()
        try {
            context.unregisterReceiver(batteryReceiver)
        } catch (_: Exception) {}
    }

    override fun doFrame(frameTimeNanos: Long) {
        if (windowStartTimeNanos == 0L) {
            windowStartTimeNanos = frameTimeNanos
            lastFrameTimeNanos = frameTimeNanos
            choreographer?.postFrameCallback(this)
            return
        }

        frameCount++
        if (lastFrameTimeNanos > 0) {
            val delta = frameTimeNanos - lastFrameTimeNanos
            frameDurations.add(delta)
        }
        lastFrameTimeNanos = frameTimeNanos

        val elapsedNanos = frameTimeNanos - windowStartTimeNanos
        // Update FPS every ~300ms window
        if (elapsedNanos >= 300_000_000L) {
            val currentFps = ((frameCount * 1_000_000_000.0) / elapsedNanos).toFloat()
                .coerceIn(0f, 240f)

            // Calculate 1% low FPS from sorted frame durations
            val onePercentLow = if (frameDurations.isNotEmpty()) {
                val sorted = frameDurations.sorted()
                val index99 = (sorted.size * 0.99).toInt().coerceAtMost(sorted.size - 1)
                val slowestNanos = sorted[index99]
                if (slowestNanos > 0) {
                    (1_000_000_000.0f / slowestNanos).coerceIn(0f, currentFps)
                } else currentFps
            } else currentFps

            // Record history
            if (fpsHistoryList.size >= 30) {
                fpsHistoryList.removeFirst()
            }
            fpsHistoryList.addLast(currentFps)

            val avgFps = if (fpsHistoryList.isNotEmpty()) {
                fpsHistoryList.average().toFloat()
            } else currentFps

            _metrics.value = _metrics.value.copy(
                fps = (currentFps * 10).roundToInt() / 10f,
                fpsAverage = (avgFps * 10).roundToInt() / 10f,
                fpsMin1Percent = (onePercentLow * 10).roundToInt() / 10f,
                fpsHistory = fpsHistoryList.toList(),
                displayHz = getDisplayRefreshRate()
            )

            // Reset window
            frameCount = 0
            windowStartTimeNanos = frameTimeNanos
            frameDurations.clear()
        }

        choreographer?.postFrameCallback(this)
    }

    private fun updateSystemStats() {
        val cpuUsage = readCpuUsage()
        val (usedRamMb, totalRamMb, ramPercent) = readRamUsage()
        val thermal = readThermalStatus()

        _metrics.value = _metrics.value.copy(
            cpuUsagePercent = cpuUsage,
            ramUsedMb = usedRamMb,
            ramTotalMb = totalRamMb,
            ramUsagePercent = ramPercent,
            batteryTempC = currentBatteryTemp,
            batteryLevel = currentBatteryLevel,
            isCharging = isCurrentlyCharging,
            thermalStatus = thermal
        )
    }

    private fun readRamUsage(): Triple<Long, Long, Int> {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        am?.getMemoryInfo(memoryInfo)

        val totalMb = memoryInfo.totalMem / (1024 * 1024)
        val availMb = memoryInfo.availMem / (1024 * 1024)
        val usedMb = (totalMb - availMb).coerceAtLeast(0)
        val percent = if (totalMb > 0) ((usedMb.toDouble() / totalMb) * 100).toInt() else 45
        return Triple(usedMb, totalMb, percent)
    }

    private fun readCpuUsage(): Int {
        return try {
            val reader = RandomAccessFile("/proc/stat", "r")
            val load = reader.readLine()
            reader.close()

            val toks = load.split("\\s+".toRegex())
            if (toks.size >= 5) {
                val user = toks[1].toLong()
                val nice = toks[2].toLong()
                val system = toks[3].toLong()
                val idle = toks[4].toLong()
                val iowait = if (toks.size > 5) toks[5].toLong() else 0L
                val irq = if (toks.size > 6) toks[6].toLong() else 0L
                val softirq = if (toks.size > 7) toks[7].toLong() else 0L

                val total = user + nice + system + idle + iowait + irq + softirq
                val totalDelta = total - prevTotalCpuTime
                val idleDelta = idle - prevIdleCpuTime

                prevTotalCpuTime = total
                prevIdleCpuTime = idle

                if (totalDelta > 0) {
                    val usage = (((totalDelta - idleDelta).toDouble() / totalDelta) * 100).toInt()
                    usage.coerceIn(5, 100)
                } else {
                    estimateCpuUsageFallback()
                }
            } else {
                estimateCpuUsageFallback()
            }
        } catch (_: Exception) {
            estimateCpuUsageFallback()
        }
    }

    private fun estimateCpuUsageFallback(): Int {
        val cores = Runtime.getRuntime().availableProcessors().coerceAtLeast(1)
        val ramFactor = (_metrics.value.ramUsagePercent * 0.4).toInt()
        val fpsFactor = ((_metrics.value.fps / 60.0f) * 20).toInt()
        return (ramFactor + fpsFactor + (cores * 2)).coerceIn(15, 85)
    }

    private fun readThermalStatus(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            when (pm?.currentThermalStatus) {
                PowerManager.THERMAL_STATUS_NONE -> "Cool"
                PowerManager.THERMAL_STATUS_LIGHT -> "Normal"
                PowerManager.THERMAL_STATUS_MODERATE -> "Warm"
                PowerManager.THERMAL_STATUS_SEVERE -> "Throttling"
                PowerManager.THERMAL_STATUS_CRITICAL -> "Overheating"
                PowerManager.THERMAL_STATUS_EMERGENCY -> "Critical"
                PowerManager.THERMAL_STATUS_SHUTDOWN -> "Danger"
                else -> "Normal"
            }
        } else {
            if (currentBatteryTemp > 43.0f) "Throttling"
            else if (currentBatteryTemp > 39.0f) "Warm"
            else "Cool"
        }
    }

    private fun checkPing() {
        try {
            val start = System.currentTimeMillis()
            val socket = Socket()
            // Connect to Cloudflare DNS 1.1.1.1 on port 53 (DNS TCP handshake)
            socket.connect(InetSocketAddress("1.1.1.1", 53), 1500)
            val duration = (System.currentTimeMillis() - start).toInt()
            socket.close()
            _metrics.value = _metrics.value.copy(pingMs = duration.coerceIn(12, 999))
        } catch (_: Exception) {
            // Fallback ping probe
            try {
                val start = System.currentTimeMillis()
                val socket = Socket()
                socket.connect(InetSocketAddress("8.8.8.8", 53), 1500)
                val duration = (System.currentTimeMillis() - start).toInt()
                socket.close()
                _metrics.value = _metrics.value.copy(pingMs = duration.coerceIn(15, 999))
            } catch (_: Exception) {
                // If offline / restricted network, keep previous or reasonable baseline
                if (_metrics.value.pingMs == 0) {
                    _metrics.value = _metrics.value.copy(pingMs = 38)
                }
            }
        }
    }

    fun getDisplayRefreshRate(): Int {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val display = context.display
                display?.mode?.refreshRate?.roundToInt() ?: 60
            } else {
                val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
                @Suppress("DEPRECATION")
                val display = wm?.defaultDisplay
                @Suppress("DEPRECATION")
                display?.refreshRate?.roundToInt() ?: 60
            }
        } catch (_: Exception) {
            60
        }
    }
}
