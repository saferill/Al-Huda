package com.alhuda.app.core.domain.model.notification

import com.alhuda.app.core.domain.model.TextResource

data class NotificationButton(
    val title: TextResource,
    val pressAction: NotificationPressAction,
)
