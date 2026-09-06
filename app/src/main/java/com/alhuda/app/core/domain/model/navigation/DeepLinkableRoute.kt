package com.alhuda.app.core.domain.model.navigation

import com.alhuda.app.core.presentation.navigation.deeplink.getDeepLinkUriString

interface DeepLinkableRoute {
    fun toUriString(): String = getDeepLinkUriString(this)
}
