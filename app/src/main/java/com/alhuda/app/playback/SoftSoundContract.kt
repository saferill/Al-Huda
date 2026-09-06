package com.alhuda.app.playback

import com.alhuda.app.core.domain.model.notification.NotificationPressAction

object SoftSoundContract {

    const val ACTION_STOP_SOFT_SOUND = "com.alhuda.app.action.STOP_SOFT_SOUND"

    fun stopAction(notificationId: String) =
        NotificationPressAction.Broadcast(
            action = ACTION_STOP_SOFT_SOUND,
            requestCode = ACTION_STOP_SOFT_SOUND.hashCode() xor notificationId.hashCode(),
        )
}
