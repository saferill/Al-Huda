package com.alhuda.app.core.domain.repository

import com.alhuda.app.core.domain.model.notification.NotificationChannelConfig

interface NotificationChannelManager {
    fun ensureChannelsExist(configs: List<NotificationChannelConfig>)
    fun deleteChannel(channelId: String)
}
