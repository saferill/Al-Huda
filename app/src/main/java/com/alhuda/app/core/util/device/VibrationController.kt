package com.alhuda.app.core.util.device

import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.content.getSystemService
import com.alhuda.app.core.domain.model.alarm.VibrationMode

object VibrationController {
    private const val ONCE_DURATION_MS = 800L

    private val CONTINUOUS_PATTERN = longArrayOf(0, 700, 2300, 1000, 2300, 1000, 1300, 1000, 1000)
    private const val CONTINUOUS_REPEAT_INDEX = 5

    private val ALARM_ATTRS = AudioAttributes.Builder()
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .setUsage(AudioAttributes.USAGE_ALARM)
        .build()

    fun vibrate(
        context: Context,
        mode: VibrationMode,
    ) {
        if (mode == VibrationMode.Off) return
        val vibrator = vibrator(context) ?: return
        val effect = when (mode) {
            VibrationMode.Continuous ->
                VibrationEffect.createWaveform(CONTINUOUS_PATTERN, CONTINUOUS_REPEAT_INDEX)

            else -> VibrationEffect.createOneShot(ONCE_DURATION_MS, VibrationEffect.DEFAULT_AMPLITUDE)
        }
        runCatching { vibrator.vibrate(effect, ALARM_ATTRS) }
    }

    fun stop(context: Context) {
        runCatching { vibrator(context)?.cancel() }
    }

    private fun vibrator(context: Context): Vibrator? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService<VibratorManager>()?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService()
        }
}
