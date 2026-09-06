package com.alhuda.app.core.domain.model.notification

import androidx.compose.runtime.Immutable
import com.alhuda.app.core.domain.model.TextResource

@Immutable
data class NotificationChannelConfig(
    val id: String,

    val name: TextResource,

    val description: TextResource,
    val importanceLevel: AndroidNotificationImportance = AndroidNotificationImportance.IMPORTANCE_DEFAULT,
    val showBadge: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val vibrationPattern: List<Long>? = null,

    val soundEnabled: Boolean = true,

    val soundUri: String? = null,

    val canBypassDnd: Boolean = false,

    val soundHandledExternally: Boolean = false,
)
