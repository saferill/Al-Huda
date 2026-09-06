package com.alhuda.app.core.presentation.navigation.deeplink

import android.net.Uri

internal class DeepLinkRequest(
    val uri: Uri,
) {

    val pathSegments: List<String> = uri.pathSegments

    val queries = buildMap {
        uri.queryParameterNames.forEach { argName ->
            this[argName] = uri.getQueryParameter(argName)!!
        }
    }

}
