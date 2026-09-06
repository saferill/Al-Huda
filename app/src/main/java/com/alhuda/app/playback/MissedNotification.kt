package com.alhuda.app.playback

import com.alhuda.app.core.domain.model.TextResource
import com.alhuda.app.core.domain.model.notification.AndroidNotificationCategory
import com.alhuda.app.core.domain.model.notification.AndroidNotificationConfig
import com.alhuda.app.core.domain.model.notification.NotificationConfig
import com.alhuda.app.core.domain.usecase.EnsureNotificationChannelsUseCase

fun missedNotificationConfig(
    id: String,
    title: TextResource,
    body: TextResource,
): NotificationConfig =
    NotificationConfig(
        id = id,
        title = title,
        body = body,
        android = AndroidNotificationConfig(
            channelId = EnsureNotificationChannelsUseCase.MISSED_CHANNEL_ID,
            category = AndroidNotificationCategory.CATEGORY_REMINDER,
            autoCancel = true,
        ),
    )
