package me.tewodros.dael

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.tewodros.dael.audio.LocalSpeaker
import me.tewodros.dael.audio.Speaker
import me.tewodros.dael.data.Repo
import me.tewodros.dael.ui.DaelApp
import me.tewodros.dael.ui.DaelTheme

class MainActivity : ComponentActivity() {
    private lateinit var repo: Repo
    private lateinit var speaker: Speaker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repo = Repo(this)
        speaker = Speaker(this)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            DaelTheme {
                CompositionLocalProvider(LocalSpeaker provides speaker) {
                    DaelApp(repo = repo, onExitApp = {
                        runCatching { stopLockTask() }
                        finishAndRemoveTask()
                    })
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        hideSystemBars()
        lifecycleScope.launch {
            if (repo.settings.first().kiosk) {
                DaelAdmin.allowLockTask(this@MainActivity)
                runCatching { startLockTask() }
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemBars()
    }

    private fun hideSystemBars() {
        WindowInsetsControllerCompat(window, window.decorView).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }
    }

    override fun onDestroy() {
        speaker.shutdown()
        super.onDestroy()
    }
}
