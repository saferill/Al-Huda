package com.alhuda.app.core.domain.model.notification

import com.alhuda.app.core.domain.model.navigation.DeepLinkableRoute

sealed class NotificationPressAction {
    abstract val id: String

    data class Broadcast(
        override val id: String = "broadcast",
        val action: String,
        val requestCode: Int,

        val extras: Map<String, String> = emptyMap(),
    ) : NotificationPressAction()

    data class Route(
        val route: DeepLinkableRoute,
        override val id: String = "route",
    ) : NotificationPressAction()

    object Default : NotificationPressAction() {
        override val id: String = "default"
    }
}
