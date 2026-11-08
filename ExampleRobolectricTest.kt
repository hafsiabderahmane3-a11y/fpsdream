package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Game Turbo FPS", appName)
  }

  @Test
  fun `verify config repository defaults`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = com.example.data.ConfigRepository.getInstance(context)
    val config = repo.config.value
    assertEquals(com.example.data.DisplayMode.EDGE_TAB, config.displayMode)
    assertEquals(true, config.showFps)
    assertEquals(true, config.showCpu)
  }

  @Test
  fun `verify config repository updates`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = com.example.data.ConfigRepository.getInstance(context)
    repo.updateConfig { it.copy(opacity = 0.75f, showCrosshair = true) }
    assertEquals(0.75f, repo.config.value.opacity, 0.01f)
    assertEquals(true, repo.config.value.showCrosshair)
  }
}
