package com.alhuda.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.alhuda.app.widget.WidgetUpdater
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LocaleChangeReceiver : BroadcastReceiver() {

    @Inject
    lateinit var widgetUpdater: WidgetUpdater

    override fun onReceive(
        context: Context,
        intent: Intent?,
    ) {
        if (intent?.action != Intent.ACTION_LOCALE_CHANGED) return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                widgetUpdater.update()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
