package com.alhuda.app.core.domain.model.notification

import android.widget.RemoteViews
import androidx.compose.runtime.Immutable
import com.alhuda.app.core.domain.model.TextResource

@Immutable
data class NotificationConfig(
    val id: String? = null,
    val title: TextResource? = null,
    val subtitle: TextResource? = null,
    val body: TextResource? = null,
    val badgeCount: Int? = null,
    val android: AndroidNotificationConfig? = null,
) {
    init {
        if (badgeCount != null && badgeCount < 0) {
            throw IllegalArgumentException("badgeCount cannot be less than 0")
        }
    }
}

@Immutable
data class AndroidNotificationConfig(

    var channelId: String,

    var ongoing: Boolean = false,

    var onlyAlertOnce: Boolean = false,

    var category: AndroidNotificationCategory? = null,

    var showTimestamp: Boolean = true,

    var timestamp: Long? = null,

    var group: String? = null,

    var visibility: AndroidNotificationVisibility = AndroidNotificationVisibility.VISIBILITY_PUBLIC,

    var autoCancel: Boolean = true,

    var sortKey: String? = null,

    var pressAction: NotificationPressAction? = NotificationPressAction.Default,

    var actions: List<NotificationButton>? = null,

    var dismissAction: NotificationPressAction? = null,

    var customContentView: RemoteViews? = null,

    var customBigContentView: RemoteViews? = null,
)
