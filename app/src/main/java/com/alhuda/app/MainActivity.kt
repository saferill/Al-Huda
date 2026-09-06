package com.alhuda.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.alhuda.app.alarm.AlarmActivity
import com.alhuda.app.core.domain.repository.SettingsRepository
import com.alhuda.app.core.presentation.AlHudaTheme
import com.alhuda.app.core.presentation.navigation.NavigationController
import com.alhuda.app.core.presentation.navigation.NavigationRoot
import com.alhuda.app.core.presentation.navigation.Route
import com.alhuda.app.core.presentation.navigation.deepLinkPatterns
import com.alhuda.app.core.presentation.navigation.deeplink.parseUriToRoute
import com.alhuda.app.di.LanguageSync
import com.alhuda.app.playback.PlaybackService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var languageSync: LanguageSync

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val initialSettings = runBlocking {
            languageSync.reconcile()
            settingsRepository.fetch()
        }

        val startingRoute = routeFromIntent(intent)
        intent = null

        setContent {
            val settings by settingsRepository.data.collectAsState(initial = initialSettings)

            AlHudaTheme(settings.themeColor, settings.displayScale) {
                NavigationRoot(
                    appIntroDone = initialSettings.appIntroDone,
                    startingRoute = startingRoute,
                )
            }
        }

        openAlarmScreenWhileSounding()
    }

    private fun openAlarmScreenWhileSounding() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                PlaybackService.activeAlarm.collect { alarm ->
                    if (alarm != null) startActivity(AlarmActivity.intent(this@MainActivity))
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        routeFromIntent(intent)?.let { NavigationController.navigateTo(it) }
    }

    private fun routeFromIntent(launchIntent: Intent?): Route? =
        launchIntent?.data?.let { runCatching { parseUriToRoute(it, deepLinkPatterns) }.getOrNull() }
}
