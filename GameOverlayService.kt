package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.MainActivity
import com.example.data.ConfigRepository
import com.example.data.CornerAnchor
import com.example.data.DisplayMode
import com.example.data.EdgeSide
import com.example.ui.overlay.CrosshairView
import com.example.ui.overlay.GameOverlayRoot
import com.example.ui.theme.GameTurboTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class GameOverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var configRepository: ConfigRepository
    private lateinit var hardwareMonitor: HardwareMonitor

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    private var overlayComposeView: ComposeView? = null
    private var crosshairComposeView: ComposeView? = null

    private lateinit var overlayParams: WindowManager.LayoutParams
    private lateinit var crosshairParams: WindowManager.LayoutParams

    private val lifecycleOwner = OverlayLifecycleOwner()

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        configRepository = ConfigRepository.getInstance(applicationContext)
        hardwareMonitor = HardwareMonitor(applicationContext)

        lifecycleOwner.onCreate()
        startForegroundServiceNotification()
        hardwareMonitor.start()

        initOverlayWindows()
        observeConfigAndMetrics()
    }

    private fun startForegroundServiceNotification() {
        val channelId = "game_turbo_channel"
        val channelName = "Game Turbo FPS Monitor"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows active FPS and performance monitor"
                setShowBadge(false)
            }
            val nm = getSystemService(NotificationManager::class.java)
            nm?.createNotificationChannel(channel)
        }

        val launchIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Game Turbo FPS Active")
            .setContentText("Real-time FPS and hardware monitor running")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()

        startForeground(1001, notification)
    }

    private fun initOverlayWindows() {
        // Main Gaming HUD overlay params
        overlayParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 300
        }

        overlayComposeView = ComposeView(this).apply {
            lifecycleOwner.attachToView(this)
            setContent {
                GameTurboTheme {
                    val metrics by hardwareMonitor.metrics.collectAsState()
                    val config by configRepository.config.collectAsState()

                    GameOverlayRoot(
                        metrics = metrics,
                        config = config,
                        onUpdateConfig = { updateBlock ->
                            configRepository.updateConfig(updateBlock)
                        },
                        onTrimMemory = {
                            performHaptic()
                            val freedMb = configRepository.trimMemory()
                            android.widget.Toast.makeText(
                                applicationContext,
                                "⚡ Game Turbo: Cleaned $freedMb MB RAM!",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        },
                        onMoveOverlay = { dx, dy ->
                            overlayParams.x += dx.toInt()
                            overlayParams.y += dy.toInt()
                            try {
                                windowManager.updateViewLayout(this@apply, overlayParams)
                            } catch (_: Exception) {}
                        },
                        onCloseService = {
                            stopSelf()
                        }
                    )
                }
            }
        }

        try {
            windowManager.addView(overlayComposeView, overlayParams)
        } catch (_: Exception) {}

        // Precision Crosshair overlay params (placed at center, NOT touchable so PUBG passes through)
        crosshairParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }

        crosshairComposeView = ComposeView(this).apply {
            lifecycleOwner.attachToView(this)
            setContent {
                val config by configRepository.config.collectAsState()
                if (config.showCrosshair) {
                    CrosshairView(
                        style = config.crosshairStyle,
                        colorType = config.crosshairColor,
                        sizeDp = config.crosshairSize
                    )
                }
            }
        }

        try {
            windowManager.addView(crosshairComposeView, crosshairParams)
        } catch (_: Exception) {}
    }

    private fun observeConfigAndMetrics() {
        serviceScope.launch {
            configRepository.config.collectLatest { config ->
                // Adjust overlay window layout based on displayMode
                when (config.displayMode) {
                    DisplayMode.EXPANDED_HUD -> {
                        // Expanded mode requires full screen touch area so user can interact with meters and sliders
                        overlayParams.width = WindowManager.LayoutParams.MATCH_PARENT
                        overlayParams.height = WindowManager.LayoutParams.MATCH_PARENT
                        overlayParams.x = 0
                        overlayParams.y = 0
                        overlayParams.gravity = Gravity.CENTER
                    }
                    DisplayMode.EDGE_TAB -> {
                        overlayParams.width = WindowManager.LayoutParams.WRAP_CONTENT
                        overlayParams.height = WindowManager.LayoutParams.WRAP_CONTENT
                        overlayParams.gravity = if (config.edgeSide == EdgeSide.LEFT) {
                            Gravity.START or Gravity.CENTER_VERTICAL
                        } else {
                            Gravity.END or Gravity.CENTER_VERTICAL
                        }
                        overlayParams.x = 0
                    }
                    DisplayMode.CORNER_PILL -> {
                        overlayParams.width = WindowManager.LayoutParams.WRAP_CONTENT
                        overlayParams.height = WindowManager.LayoutParams.WRAP_CONTENT
                        overlayParams.gravity = when (config.cornerAnchor) {
                            CornerAnchor.TOP_LEFT -> Gravity.TOP or Gravity.START
                            CornerAnchor.TOP_RIGHT -> Gravity.TOP or Gravity.END
                            CornerAnchor.BOTTOM_LEFT -> Gravity.BOTTOM or Gravity.START
                            CornerAnchor.BOTTOM_RIGHT -> Gravity.BOTTOM or Gravity.END
                        }
                        overlayParams.x = 16
                        overlayParams.y = 80
                    }
                }

                try {
                    overlayComposeView?.let {
                        windowManager.updateViewLayout(it, overlayParams)
                    }
                } catch (_: Exception) {}

                // Crosshair visibility
                crosshairComposeView?.visibility = if (config.showCrosshair) View.VISIBLE else View.GONE
            }
        }
    }

    private fun performHaptic() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(40)
            }
        } catch (_: Exception) {}
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        hardwareMonitor.stop()
        lifecycleOwner.onDestroy()

        try {
            overlayComposeView?.let { windowManager.removeView(it) }
        } catch (_: Exception) {}

        try {
            crosshairComposeView?.let { windowManager.removeView(it) }
        } catch (_: Exception) {}

        configRepository.updateConfig { it.copy(isEnabled = false) }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

/**
 * Custom Lifecycle & SavedState owner to host Jetpack Compose in a Service WindowManager
 */
class OverlayLifecycleOwner : LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val store = ViewModelStore()

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry
    override val viewModelStore: ViewModelStore get() = store

    fun onCreate() {
        savedStateRegistryController.performRestore(Bundle())
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        store.clear()
    }

    fun attachToView(view: View) {
        view.setViewTreeLifecycleOwner(this)
        view.setViewTreeViewModelStoreOwner(this)
        view.setViewTreeSavedStateRegistryOwner(this)
    }
}
