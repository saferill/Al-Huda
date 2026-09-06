package com.alhuda.app.playback

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.PowerManager
import android.os.SystemClock
import android.provider.Settings
import android.telephony.PhoneStateListener
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.VisibleForTesting
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import com.alhuda.app.MainActivity
import com.alhuda.app.R
import com.alhuda.app.adhan.AdhanContract
import com.alhuda.app.alarm.AlarmActivity
import com.alhuda.app.core.data.audio.VolumeKeyDismissMonitor
import com.alhuda.app.core.data.locale.withAppLocale
import com.alhuda.app.core.domain.model.adhan.Prayer
import com.alhuda.app.core.domain.model.alarm.VibrationMode
import com.alhuda.app.core.domain.usecase.EnsureNotificationChannelsUseCase
import com.alhuda.app.core.util.device.CallStateInspector
import com.alhuda.app.core.util.device.VibrationController
import com.alhuda.app.playback.PlaybackService.Companion.FADE_IN_MIN_DURATION_MS
import com.alhuda.app.playback.PlaybackService.Companion.markAlarmHandled
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PlaybackService :
    Service(),
    MediaPlayer.OnPreparedListener,
    MediaPlayer.OnCompletionListener,
    MediaPlayer.OnErrorListener,
    AudioManager.OnAudioFocusChangeListener {

    companion object {
        private const val TAG = "PlaybackService"

        const val ACTION_PLAY = "com.alhuda.app.action.ADHAN_PLAY"
        const val ACTION_STOP = "com.alhuda.app.action.ADHAN_STOP"

        const val EXTRA_TITLE = "title"
        const val EXTRA_BODY = "body"
        const val EXTRA_SOUND_URI = "sound_uri"
        const val EXTRA_CHANNEL_ID = "channel_id"
        const val EXTRA_VOLUME_PERCENT = "volume_percent"
        const val EXTRA_FADE_IN_VOLUME = "fade_in_volume"
        const val EXTRA_USE_MEDIA_USAGE = "use_media_usage"
        const val EXTRA_FULL_SCREEN = "full_screen"
        const val EXTRA_FORCE_LAUNCH_ACTIVITY = "force_launch_activity"
        const val EXTRA_VIBRATION = "vibration"
        const val EXTRA_VOLUME_BUTTON_STOPS = "volume_button_stops"
        const val EXTRA_TIME_LABEL = "time_label"
        const val EXTRA_HEADER = "header"
        const val EXTRA_IS_REMINDER = "is_reminder"
        const val EXTRA_LOOP = "loop"
        const val EXTRA_LANGUAGE_TAGS = "language_tags"

        private const val NOTIFICATION_ID = 0xADA2

        private val _activeAlarm = MutableStateFlow<ActiveAlarm?>(null)
        val activeAlarm: StateFlow<ActiveAlarm?> = _activeAlarm.asStateFlow()

        private var lastAlarmId = 0L

        private var lastTraceNotificationId = 0

        private fun nextTraceNotificationId(): Int {
            val now = System.currentTimeMillis().toInt()
            lastTraceNotificationId = if (now > lastTraceNotificationId) now else lastTraceNotificationId + 1
            return lastTraceNotificationId
        }

        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        internal fun startAlarm(
            prayer: Prayer?,
            timeLabel: String,
            title: String,
            header: String,
            isReminder: Boolean,
        ): ActiveAlarm =
            ActiveAlarm(
                id = ++lastAlarmId,
                prayer = prayer,
                timeLabel = timeLabel,
                title = title,
                header = header,
                isReminder = isReminder,
            ).also { _activeAlarm.value = it }

        fun markAlarmHandled(id: Long) {
            _activeAlarm.update { if (it?.id == id) null else it }
        }

        private val VOLUME_KEY_STREAMS = intArrayOf(
            AudioManager.STREAM_ALARM,
            AudioManager.STREAM_MUSIC,
            AudioManager.STREAM_RING,
            AudioManager.STREAM_NOTIFICATION,
            AudioManager.STREAM_SYSTEM,
        )

        private const val LOOP_CAP_MS = 5 * 60 * 1000L

        private const val INTERRUPTION_CAP_MS = LOOP_CAP_MS

        private const val FADE_IN_MS = 5_000L
        private const val FADE_IN_STEP_MS = 200L

        private const val FADE_IN_MIN_DURATION_MS = FADE_IN_MS * 4

        fun start(
            context: Context,
            extras: Bundle,
        ) {
            val intent = Intent(context, PlaybackService::class.java).apply {
                action = ACTION_PLAY
                putExtras(extras)
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            context.startService(Intent(context, PlaybackService::class.java).setAction(ACTION_STOP))
        }
    }

    private var player: MediaPlayer? = null
    private var focusRequest: AudioFocusRequest? = null
    private var telephonyCallback: TelephonyCallback? = null
    private var phoneStateListener: PhoneStateListener? = null
    private var volumeReceiver: BroadcastReceiver? = null
    private var volumeKeyMonitor: VolumeKeyDismissMonitor? = null
    private var playbackStream = AudioManager.STREAM_ALARM
    private var wasPlayingBeforeCall = false
    private var volumePercent = -1
    private var shouldLoop = false
    private var fadeInVolume = false

    private var savedStreamVolume = -1

    private var fadeInStartedAt = 0L
    private val fadeInRunnable = object : Runnable {
        override fun run() {
            val mp = player ?: return
            val fraction = ((SystemClock.elapsedRealtime() - fadeInStartedAt).toFloat() / FADE_IN_MS).coerceIn(0f, 1f)
            runCatching { mp.setVolume(fraction, fraction) }
            if (fraction < 1f) {
                mainHandler.postDelayed(this, FADE_IN_STEP_MS)
            } else {
                fadeInStartedAt = 0L
            }
        }
    }

    private var continuousVibration = false

    private var lingerDetails: LingerDetails? = null

    private var stopped = false
    private val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private val loopCapRunnable = Runnable { cleanupAndStop(leaveLingering = true) }

    private val interruptionCapRunnable = Runnable { cleanupAndStop(leaveLingering = true) }

    private val audioManager: AudioManager? by lazy { getSystemService() }
    private val telephonyManager: TelephonyManager? by lazy { getSystemService() }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        if (intent?.action != ACTION_PLAY) {
            cleanupAndStop()
            return START_NOT_STICKY
        }

        if (!stopped) postLingeringTrace()
        teardownPlayback()
        stopped = false

        val channelId = intent.getStringExtra(EXTRA_CHANNEL_ID)
        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty()
        val body = intent.getStringExtra(EXTRA_BODY)
        val prayerName = intent.getStringExtra(AdhanContract.EXTRA_PRAYER).orEmpty()
        val timeLabel = intent.getStringExtra(EXTRA_TIME_LABEL).orEmpty()
        val header = intent.getStringExtra(EXTRA_HEADER).orEmpty()
        val isReminder = intent.getBooleanExtra(EXTRA_IS_REMINDER, false)
        val fullScreen = intent.getBooleanExtra(EXTRA_FULL_SCREEN, true)
        val forceLaunchActivity = intent.getBooleanExtra(EXTRA_FORCE_LAUNCH_ACTIVITY, false)
        val volumeButtonStops = intent.getBooleanExtra(EXTRA_VOLUME_BUTTON_STOPS, false)
        val useMediaUsage = intent.getBooleanExtra(EXTRA_USE_MEDIA_USAGE, false)
        val languageTags = intent.getStringExtra(EXTRA_LANGUAGE_TAGS).orEmpty()
        playbackStream = if (useMediaUsage) AudioManager.STREAM_MUSIC else AudioManager.STREAM_ALARM

        val alarm = startAlarm(
            prayer = prayerName.takeIf { it.isNotEmpty() }?.let { runCatching { Prayer.valueOf(it) }.getOrNull() },
            timeLabel = timeLabel,
            title = title,
            header = header,
            isReminder = isReminder,
        )

        lingerDetails = LingerDetails(title = title, body = body, timeLabel = timeLabel)

        val safeChannelId = channelId?.takeIf { it.isNotEmpty() }
            ?: EnsureNotificationChannelsUseCase.ADHAN_CHANNEL_ID
        startForeground(
            NOTIFICATION_ID,
            buildNotification(safeChannelId, alarm, body, fullScreen, languageTags),
        )
        if (channelId.isNullOrEmpty()) {
            cleanupAndStop()
            return START_NOT_STICKY
        }

        val uri = intent.getStringExtra(EXTRA_SOUND_URI)?.toUri()
        if (uri == null) {
            cleanupAndStop()
            return START_NOT_STICKY
        }
        volumePercent = intent.getIntExtra(EXTRA_VOLUME_PERCENT, -1)
        fadeInVolume = intent.getBooleanExtra(EXTRA_FADE_IN_VOLUME, false)
        shouldLoop = intent.getBooleanExtra(EXTRA_LOOP, false)
        val vibration = intent.getStringExtra(EXTRA_VIBRATION)?.let { runCatching { VibrationMode.valueOf(it) }.getOrNull() }
            ?: VibrationMode.Off
        continuousVibration = vibration == VibrationMode.Continuous

        if (isCallActive()) {

            lingerDetails = lingerDetails?.copy(
                body = withAppLocale(languageTags).getString(R.string.missed_during_call_body, timeLabel),
            )
            cleanupAndStop(leaveLingering = true)
            return START_NOT_STICKY
        }

        requestAudioFocus(useMediaUsage)
        registerCallStateListener()

        if (volumeButtonStops) {

            val sessionSubtitle = body?.takeIf { it.isNotEmpty() } ?: timeLabel
            volumeKeyMonitor = VolumeKeyDismissMonitor(
                context = this,
                nowPlaying = VolumeKeyDismissMonitor.NowPlaying(title, sessionSubtitle),
            ) { cleanupAndStop() }.also { it.start() }
        } else {
            registerVolumeMirrorReceiver()
        }
        VibrationController.vibrate(this, vibration)

        val notificationsEnabled = NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()
        if (forceLaunchActivity || !notificationsEnabled) {
            launchAlarmActivity(AlarmActivity.intent(this), forced = forceLaunchActivity)
        }
        applyAbsoluteStreamVolume()
        startPlayer(uri, useMediaUsage)
        return START_NOT_STICKY
    }

    private fun applyAbsoluteStreamVolume() {
        if (volumePercent !in 0..100) return
        val am = audioManager ?: return
        if (savedStreamVolume == -1) {
            savedStreamVolume = runCatching { am.getStreamVolume(playbackStream) }.getOrDefault(-1)
        }
        val max = runCatching { am.getStreamMaxVolume(playbackStream) }.getOrDefault(0)
        if (max <= 0) return
        runCatching { am.setStreamVolume(playbackStream, (volumePercent * max + 50) / 100, 0) }
    }

    private fun restoreStreamVolume() {
        if (savedStreamVolume < 0) return
        runCatching { audioManager?.setStreamVolume(playbackStream, savedStreamVolume, 0) }
        savedStreamVolume = -1
    }

    private fun startPlayer(
        uri: Uri,
        useMediaUsage: Boolean,
    ) {
        player?.release()

        val mp = MediaPlayer().apply {
            setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(if (useMediaUsage) AudioAttributes.USAGE_MEDIA else AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build(),
            )
            setOnPreparedListener(this@PlaybackService)
            setOnCompletionListener(this@PlaybackService)
            setOnErrorListener(this@PlaybackService)
        }
        player = mp
        val ok = runCatching {
            mp.setDataSource(applicationContext, uri)
            mp.prepareAsync()
        }.isSuccess

        if (!ok) cleanupAndStop(leaveLingering = true)
    }

    override fun onPrepared(mp: MediaPlayer) {
        if (shouldLoop) {
            mp.isLooping = true
            mainHandler.postDelayed(loopCapRunnable, LOOP_CAP_MS)
        }
        if (fadeInVolume && shouldFadeIn(mp)) {
            runCatching { mp.setVolume(0f, 0f) }
            fadeInStartedAt = SystemClock.elapsedRealtime()
            mainHandler.postDelayed(fadeInRunnable, FADE_IN_STEP_MS)
        }
        runCatching { mp.start() }
    }

    private fun shouldFadeIn(mp: MediaPlayer): Boolean {
        if (shouldLoop) return true
        val durationMs = runCatching { mp.duration }.getOrDefault(-1)
        return durationMs < 0 || durationMs >= FADE_IN_MIN_DURATION_MS
    }

    override fun onCompletion(mp: MediaPlayer) {

        if (continuousVibration) {
            runCatching { mp.reset() }
            runCatching { mp.release() }
            player = null
            mainHandler.removeCallbacks(loopCapRunnable)
            mainHandler.postDelayed(loopCapRunnable, LOOP_CAP_MS)
        } else {
            cleanupAndStop(leaveLingering = true)
        }
    }

    override fun onError(
        mp: MediaPlayer,
        what: Int,
        extra: Int,
    ): Boolean {
        cleanupAndStop(leaveLingering = true)
        return true
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when {
            focusChange == AudioManager.AUDIOFOCUS_GAIN -> if (wasPlayingBeforeCall) resume()
            focusChange == AudioManager.AUDIOFOCUS_LOSS -> if (!isCallActive()) cleanupAndStop(leaveLingering = true)
            focusChange < 0 -> pauseForInterruption()
        }
    }

    private fun pauseForInterruption() {
        runCatching {
            if (player?.isPlaying == true) {
                player?.pause()
                wasPlayingBeforeCall = true
                mainHandler.removeCallbacks(interruptionCapRunnable)
                mainHandler.postDelayed(interruptionCapRunnable, INTERRUPTION_CAP_MS)
            }
        }
    }

    private fun resume() {
        wasPlayingBeforeCall = false
        mainHandler.removeCallbacks(interruptionCapRunnable)
        runCatching { player?.start() }
    }

    private fun requestAudioFocus(useMediaUsage: Boolean): Boolean {
        val am = audioManager ?: return false
        val attrs = AudioAttributes.Builder()
            .setUsage(if (useMediaUsage) AudioAttributes.USAGE_MEDIA else AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(attrs)
            .setOnAudioFocusChangeListener(this)
            .build()
        focusRequest = request
        return am.requestAudioFocus(request) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    private fun isCallActive(): Boolean = CallStateInspector.isCallActive(this)

    private fun registerCallStateListener() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE)
            != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val tm = telephonyManager ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val callback = object : TelephonyCallback(), TelephonyCallback.CallStateListener {
                override fun onCallStateChanged(state: Int) = onCallState(state)
            }
            telephonyCallback = callback
            tm.registerTelephonyCallback(mainExecutor, callback)
        } else {
            @Suppress("DEPRECATION")
            val listener = object : PhoneStateListener() {
                @Deprecated("Deprecated in Java")
                override fun onCallStateChanged(
                    state: Int,
                    phoneNumber: String?,
                ) = onCallState(state)
            }
            phoneStateListener = listener
            @Suppress("DEPRECATION")
            tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE)
        }
    }

    private fun onCallState(state: Int) {
        if (state == TelephonyManager.CALL_STATE_IDLE) {
            if (wasPlayingBeforeCall) resume()
        } else {
            pauseForInterruption()
        }
    }

    private fun registerVolumeMirrorReceiver() {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(
                c: Context?,
                i: Intent?,
            ) {
                if (i != null) mirrorVolumeToPlayer(i)
            }
        }
        volumeReceiver = receiver
        ContextCompat.registerReceiver(
            this,
            receiver,
            IntentFilter(VolumeKeyDismissMonitor.ACTION_VOLUME_CHANGED),

            ContextCompat.RECEIVER_EXPORTED,
        )
    }

    private fun unregisterVolumeReceiver() {
        volumeReceiver?.let { runCatching { unregisterReceiver(it) } }
        volumeReceiver = null
    }

    private fun mirrorVolumeToPlayer(intent: Intent) {

        if (fadeInStartedAt != 0L) return
        val stream = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_TYPE", -1)
        if (stream == playbackStream || stream !in VOLUME_KEY_STREAMS) return
        val value = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_VALUE", Int.MIN_VALUE)
        if (value == Int.MIN_VALUE) return
        val max = runCatching { audioManager?.getStreamMaxVolume(stream) ?: 0 }.getOrDefault(0)
        if (max <= 0) return
        val ratio = (value.toFloat() / max).coerceIn(0f, 1f)
        runCatching { player?.setVolume(ratio, ratio) }
    }

    @SuppressLint("FullScreenIntentPolicy")
    private fun buildNotification(
        channelId: String,
        alarm: ActiveAlarm,
        body: String?,
        fullScreen: Boolean,
        languageTags: String,
    ): android.app.Notification {
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            AlarmActivity.intent(this),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val stopIntent = PendingIntent.getService(
            this,
            0,
            Intent(this, PlaybackService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.monochrome_notif)
            .setContentTitle(alarm.title)
            .setSubText(alarm.timeLabel)
            .setContentText(body)
            .setShowWhen(false)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setAutoCancel(false)
            .setContentIntent(contentIntent)

            .setDeleteIntent(stopIntent)

            .addAction(R.drawable.outline_stop_24, withAppLocale(languageTags).getString(R.string.dismiss), stopIntent)
            .setOnlyAlertOnce(true)

        if (fullScreen && NotificationManagerCompat.from(applicationContext).canUseFullScreenIntent()) {
            builder.setFullScreenIntent(contentIntent, true)
        }
        return builder.build()
    }

    private fun launchAlarmActivity(
        intent: Intent,
        forced: Boolean,
    ) {
        val canDrawOverlays = Settings.canDrawOverlays(this)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                sendAlarmActivityPendingIntent(intent)
            } else {
                startActivity(intent)
            }
            Log.i(TAG, "Alarm activity start requested (forced=$forced, canDrawOverlays=$canDrawOverlays)")
        } catch (e: Exception) {
            Log.e(TAG, "Alarm activity start threw (forced=$forced, canDrawOverlays=$canDrawOverlays)", e)
            return
        }
        if (!canDrawOverlays) {
            Log.w(
                TAG,
                "No 'display over other apps' permission — the system may have blocked this start. " +
                    "Check logcat for a background activity launch denial.",
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun sendAlarmActivityPendingIntent(intent: Intent) {
        val options = ActivityOptions.makeBasic()
            .setPendingIntentBackgroundActivityStartMode(backgroundActivityStartMode())
        PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        ).send(this, 0, null, null, null, null, options.toBundle())
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    @Suppress("DEPRECATION")
    private fun backgroundActivityStartMode(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
            ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOW_ALWAYS
        } else {
            ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
        }

    private fun teardownPlayback() {
        mainHandler.removeCallbacks(loopCapRunnable)
        mainHandler.removeCallbacks(interruptionCapRunnable)
        mainHandler.removeCallbacks(fadeInRunnable)
        fadeInStartedAt = 0L
        player?.let { mp ->
            runCatching { mp.reset() }
            mp.release()
        }
        player = null
        wasPlayingBeforeCall = false

        restoreStreamVolume()
        abandonAudioFocus()
        unregisterCallStateListener()
        unregisterVolumeReceiver()
        volumeKeyMonitor?.stop()
        volumeKeyMonitor = null
        VibrationController.stop(this)
    }

    private fun cleanupAndStop(leaveLingering: Boolean = false) {
        if (stopped) return
        stopped = true
        teardownPlayback()
        _activeAlarm.value = null

        stopForeground(STOP_FOREGROUND_REMOVE)
        if (leaveLingering) postLingeringTrace()
        stopSelf()
    }

    @SuppressLint("MissingPermission")
    private fun postLingeringTrace() {

        if (!NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()) return
        val lingering = buildLingeringNotification() ?: return
        runCatching {
            NotificationManagerCompat.from(this).notify(nextTraceNotificationId(), lingering)
        }.onFailure { Log.w(TAG, "Could not post the lingering notification", it) }
    }

    private fun buildLingeringNotification(): android.app.Notification? {
        val details = lingerDetails ?: return null
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        return NotificationCompat.Builder(this, EnsureNotificationChannelsUseCase.MISSED_CHANNEL_ID)
            .setSmallIcon(R.drawable.monochrome_notif)
            .setContentTitle(details.title)
            .setSubText(details.timeLabel)
            .setContentText(details.body)
            .setShowWhen(false)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(false)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(contentIntent)
            .build()
    }

    data class ActiveAlarm(
        val id: Long,
        val prayer: Prayer?,
        val timeLabel: String,
        val title: String,
        val header: String,
        val isReminder: Boolean,
    )

    private data class LingerDetails(
        val title: String,
        val body: String?,
        val timeLabel: String,
    )

    private fun abandonAudioFocus() {
        focusRequest?.let { audioManager?.abandonAudioFocusRequest(it) }
        focusRequest = null
    }

    private fun unregisterCallStateListener() {
        val tm = telephonyManager ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            telephonyCallback?.let { tm.unregisterTelephonyCallback(it) }
            telephonyCallback = null
        } else {
            @Suppress("DEPRECATION")
            phoneStateListener?.let { tm.listen(it, PhoneStateListener.LISTEN_NONE) }
            phoneStateListener = null
        }
    }

    override fun onDestroy() {
        cleanupAndStop()
        super.onDestroy()
    }
}
