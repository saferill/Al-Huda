package com.alhuda.app.core.presentation.navigation.deeplink

import android.util.Log
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.KSerializer

internal class DeepLinkMatcher<T : NavKey>(
    val request: DeepLinkRequest,
    val deepLinkPattern: DeepLinkPattern<T>,
) {

    fun match(): DeepLinkMatchResult<T>? {
        if (request.uri.scheme != deepLinkPattern.uriPattern.scheme) return null
        if (!request.uri.authority.equals(deepLinkPattern.uriPattern.authority, ignoreCase = true)) return null
        if (request.pathSegments.size != deepLinkPattern.pathSegments.size) return null

        if (request.uri == deepLinkPattern.uriPattern) {
            return DeepLinkMatchResult(deepLinkPattern.serializer, mapOf())
        }

        val args = mutableMapOf<String, Any>()

        request.pathSegments
            .asSequence()

            .zip(deepLinkPattern.pathSegments.asSequence())
            .forEach { it ->

                val requestedSegment = it.first
                val candidateSegment = it.second

                if (candidateSegment.isParamArg) {
                    val parsedValue = try {
                        candidateSegment.typeParser.invoke(requestedSegment)
                    } catch (e: IllegalArgumentException) {
                        Log.e(TAG_LOG_ERROR, "Failed to parse path value:[$requestedSegment].", e)
                        return null
                    }
                    args[candidateSegment.stringValue] = parsedValue
                } else if (requestedSegment != candidateSegment.stringValue) {

                    return null
                }
            }

        request.queries.forEach { query ->
            val name = query.key

            val queryStringParser = deepLinkPattern.queryValueParsers[name] ?: return@forEach

            val queryParsedValue = try {
                queryStringParser.invoke(query.value)
            } catch (e: IllegalArgumentException) {
                Log.e(TAG_LOG_ERROR, "Failed to parse query name:[$name] value:[${query.value}].", e)
                return null
            }
            args[name] = queryParsedValue
        }

        return DeepLinkMatchResult(deepLinkPattern.serializer, args)
    }
}

internal data class DeepLinkMatchResult<T : NavKey>(
    val serializer: KSerializer<T>,
    val args: Map<String, Any>,
)

const val TAG_LOG_ERROR = "DeepLinkMatcher"
