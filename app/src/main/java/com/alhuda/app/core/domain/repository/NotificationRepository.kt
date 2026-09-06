package com.alhuda.app.core.domain.repository

import com.alhuda.app.core.domain.model.notification.NotificationConfig

interface NotificationRepository {

    suspend fun notify(payload: NotificationConfig)

    suspend fun cancelNotification(notificationId: String)

    suspend fun isNotificationsAllowed(): Boolean
}
