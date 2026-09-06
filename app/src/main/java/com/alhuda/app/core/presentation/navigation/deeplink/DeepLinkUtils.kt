package com.alhuda.app.core.presentation.navigation.deeplink

import android.net.Uri
import com.alhuda.app.core.domain.model.navigation.DeepLinkableRoute
import com.alhuda.app.core.presentation.navigation.Route

private const val PATH_BASE = "al-huda://"

fun getDeepLinkUriString(route: DeepLinkableRoute): String = PATH_BASE + route::class.simpleName

internal fun parseUriToRoute(
    uri: Uri?,
    deepLinkPatterns: List<DeepLinkPattern<out Route>>,
): Route? =
    uri?.let {
        val request = DeepLinkRequest(uri)
        val match = deepLinkPatterns.firstNotNullOfOrNull { pattern ->
            DeepLinkMatcher(request, pattern).match()
        }
        match?.let {
            KeyDecoder(match.args)
                .decodeSerializableValue(match.serializer)
        }
    }
