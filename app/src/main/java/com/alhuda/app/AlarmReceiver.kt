package com.alhuda.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.alhuda.app.adhan.AdhanContract
import com.alhuda.app.adhan.AdhanFiringHandler
import com.alhuda.app.core.data.audio.SoftSoundPlayer
import com.alhuda.app.core.domain.model.adhan.Prayer
import com.alhuda.app.playback.SoftSoundContract
import com.alhuda.app.ramadan.RamadanNoticeContract
import com.alhuda.app.ramadan.RamadanNoticeHandler
import com.alhuda.app.reminder.ReminderContract
import com.alhuda.app.reminder.ReminderFiringHandler
import com.alhuda.app.widget.WidgetContract
import com.alhuda.app.widget.WidgetUpdater
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    private companion object {
        const val TAG = "AlarmReceiver"
    }

    @Inject
    lateinit var widgetUpdater: WidgetUpdater

    @Inject
    lateinit var adhanFiringHandler: AdhanFiringHandler

    @Inject
    lateinit var reminderFiringHandler: ReminderFiringHandler

    @Inject
    lateinit var ramadanNoticeHandler: RamadanNoticeHandler

    @Inject
    lateinit var softSoundPlayer: SoftSoundPlayer

    override fun onReceive(
        context: Context,
        intent: Intent?,
    ) {
        when (intent?.action) {
            WidgetContract.ACTION_WIDGET_UPDATE -> async { widgetUpdater.update() }

            AdhanContract.ACTION_ADHAN -> {
                val prayer = intent.prayer() ?: return
                val playSound = intent.getStringExtra(AdhanContract.EXTRA_PLAY_SOUND)?.toBoolean() ?: false
                val timestamp = intent.getStringExtra(AdhanContract.EXTRA_TIMESTAMP)?.toLongOrNull()
                    ?: System.currentTimeMillis()
                async { adhanFiringHandler.onAdhanFired(prayer, playSound, timestamp) }
            }

            AdhanContract.ACTION_PRE_ADHAN -> {
                val prayer = intent.prayer() ?: return
                val timestamp = intent.getStringExtra(AdhanContract.EXTRA_TIMESTAMP)?.toLongOrNull()
                    ?: System.currentTimeMillis()
                async { adhanFiringHandler.onPreAdhanFired(prayer, timestamp) }
            }

            AdhanContract.ACTION_CANCEL_ADHAN -> async { adhanFiringHandler.onCancelAdhan() }

            AdhanContract.ACTION_UNSILENCE -> async { adhanFiringHandler.onUnsilence() }

            AdhanContract.ACTION_ADHAN_REMIND -> {
                val prayer = intent.prayer() ?: return
                val minutes = intent.getStringExtra(AdhanContract.EXTRA_REMIND_MINUTES)?.toIntOrNull() ?: 0
                async { adhanFiringHandler.onAdhanRemindFired(prayer, minutes) }
            }

            ReminderContract.ACTION_REMINDER -> {
                val reminderId = intent.getStringExtra(ReminderContract.EXTRA_REMINDER_ID) ?: return
                val timestamp = intent.getStringExtra(ReminderContract.EXTRA_TIMESTAMP)?.toLongOrNull()
                    ?: System.currentTimeMillis()
                async { reminderFiringHandler.onReminderFired(reminderId, timestamp) }
            }

            ReminderContract.ACTION_PRE_REMINDER -> {
                val reminderId = intent.getStringExtra(ReminderContract.EXTRA_REMINDER_ID) ?: return
                val timestamp = intent.getStringExtra(ReminderContract.EXTRA_TIMESTAMP)?.toLongOrNull()
                    ?: System.currentTimeMillis()
                async { reminderFiringHandler.onPreReminderFired(reminderId, timestamp) }
            }

            ReminderContract.ACTION_CANCEL_REMINDER -> {
                val reminderId = intent.getStringExtra(ReminderContract.EXTRA_REMINDER_ID) ?: return
                async { reminderFiringHandler.onCancelReminder(reminderId) }
            }

            SoftSoundContract.ACTION_STOP_SOFT_SOUND -> softSoundPlayer.stop()

            RamadanNoticeContract.ACTION_RAMADAN_CHECK -> async { ramadanNoticeHandler.onCheckFired() }

            RamadanNoticeContract.ACTION_RAMADAN_REMIND_NEXT_YEAR -> async { ramadanNoticeHandler.onRemindNextYear() }

            RamadanNoticeContract.ACTION_RAMADAN_DONT_SHOW_AGAIN -> async { ramadanNoticeHandler.onDontShowAgain() }
        }
    }

    private fun Intent.prayer(): Prayer? =
        getStringExtra(AdhanContract.EXTRA_PRAYER)?.let { runCatching { Prayer.valueOf(it) }.getOrNull() }

    private fun async(block: suspend () -> Unit) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                block()
            } catch (e: Exception) {
                Log.e(TAG, "Alarm handling failed", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
