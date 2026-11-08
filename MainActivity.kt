package com.example

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.ConfigRepository
import com.example.service.GameOverlayService
import com.example.service.HardwareMonitor
import com.example.ui.screens.CustomizationScreen
import com.example.ui.screens.GameArenaSimScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.GameTurboTheme
import kotlinx.coroutines.launch

enum class AppScreen {
    HOME,
    CUSTOMIZATION,
    ARENA_SIM
}

class MainActivity : ComponentActivity() {

    private lateinit var configRepository: ConfigRepository
    private lateinit var hardwareMonitor: HardwareMonitor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        configRepository = ConfigRepository.getInstance(applicationContext)
        hardwareMonitor = HardwareMonitor(applicationContext)
        hardwareMonitor.start()

        setContent {
            GameTurboTheme {
                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()

                var currentScreen by remember { mutableStateOf(AppScreen.HOME) }
                var hasOverlayPermission by remember { mutableStateOf(checkOverlayPermission()) }
                var isServiceRunning by remember { mutableStateOf(isServiceRunning(GameOverlayService::class.java)) }

                val metrics by hardwareMonitor.metrics.collectAsState()
                val config by configRepository.config.collectAsState()

                // Keep permission & service state updated on lifecycle resume
                val lifecycleOwner = LocalLifecycleOwner.current
                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_RESUME) {
                            hasOverlayPermission = checkOverlayPermission()
                            isServiceRunning = isServiceRunning(GameOverlayService::class.java)
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }

                // Overlay permission launcher
                val overlayPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) {
                    hasOverlayPermission = checkOverlayPermission()
                    if (hasOverlayPermission) {
                        scope.launch {
                            snackbarHostState.showSnackbar("تم منح إذن الظهور فوق الشاشة بنجاح! يمكنك الآن تشغيل العارض.")
                        }
                    }
                }

                fun requestOverlayPermission() {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:$packageName")
                        )
                        overlayPermissionLauncher.launch(intent)
                    }
                }

                fun toggleOverlayService() {
                    if (!hasOverlayPermission) {
                        requestOverlayPermission()
                        return
                    }

                    if (isServiceRunning) {
                        stopService(Intent(this@MainActivity, GameOverlayService::class.java))
                        isServiceRunning = false
                        configRepository.updateConfig { it.copy(isEnabled = false) }
                        scope.launch {
                            snackbarHostState.showSnackbar("تم إيقاف عارض الفريمات")
                        }
                    } else {
                        val serviceIntent = Intent(this@MainActivity, GameOverlayService::class.java)
                        ContextCompat.startForegroundService(this@MainActivity, serviceIntent)
                        isServiceRunning = true
                        configRepository.updateConfig { it.copy(isEnabled = true) }
                        scope.launch {
                            snackbarHostState.showSnackbar("تم تشغيل عارض الفريمات! اسحب الشريط من جانب الشاشة في اللعبة.")
                        }
                    }
                }

                fun launchGame(packageName: String) {
                    val pm = packageManager
                    val launchIntent = pm.getLaunchIntentForPackage(packageName)
                    if (launchIntent != null) {
                        // Start overlay if not already running
                        if (!isServiceRunning && hasOverlayPermission) {
                            val serviceIntent = Intent(this@MainActivity, GameOverlayService::class.java)
                            ContextCompat.startForegroundService(this@MainActivity, serviceIntent)
                            isServiceRunning = true
                            configRepository.updateConfig { it.copy(isEnabled = true) }
                        }
                        startActivity(launchIntent)
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                "اللعبة غير مثبتة على الجهاز حالياً ($packageName). يمكنك تجربة العارض عبر ساحة ألعاب ببجي المدمجة!"
                            )
                        }
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { innerPadding ->
                    when (currentScreen) {
                        AppScreen.HOME -> {
                            HomeScreen(
                                metrics = metrics,
                                config = config,
                                isOverlayServiceRunning = isServiceRunning,
                                hasOverlayPermission = hasOverlayPermission,
                                onToggleService = { toggleOverlayService() },
                                onRequestPermission = { requestOverlayPermission() },
                                onNavigateCustomization = { currentScreen = AppScreen.CUSTOMIZATION },
                                onNavigateSimArena = { currentScreen = AppScreen.ARENA_SIM },
                                onLaunchGame = { pkg -> launchGame(pkg) }
                            )
                        }

                        AppScreen.CUSTOMIZATION -> {
                            BackHandler { currentScreen = AppScreen.HOME }
                            CustomizationScreen(
                                config = config,
                                onUpdateConfig = { updateBlock ->
                                    configRepository.updateConfig(updateBlock)
                                },
                                onBack = { currentScreen = AppScreen.HOME }
                            )
                        }

                        AppScreen.ARENA_SIM -> {
                            BackHandler { currentScreen = AppScreen.HOME }
                            GameArenaSimScreen(
                                metrics = metrics,
                                config = config,
                                onBack = { currentScreen = AppScreen.HOME }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun checkOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
    }

    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        @Suppress("DEPRECATION")
        val services = manager?.getRunningServices(Integer.MAX_VALUE) ?: return false
        for (service in services) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        hardwareMonitor.stop()
    }
}
