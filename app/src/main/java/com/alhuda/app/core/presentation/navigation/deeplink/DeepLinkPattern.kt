package com.alhuda.app.core.presentation.navigation.deeplink

import android.net.Uri
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.encoding.CompositeDecoder
import java.io.Serializable

internal class DeepLinkPattern<T : NavKey>(
    val serializer: KSerializer<T>,
    val uriPattern: Uri,
) {

    private val regexPatternFillIn by lazy { Regex("\\{(.+?)\\}") }

    val pathSegments: List<PathSegment> by lazy {
        buildList {
            uriPattern.pathSegments.forEach { segment ->

                var result = regexPatternFillIn.find(segment)
                if (result != null) {

                    val argName = result.groups[1]!!.value

                    val elementIndex = serializer.descriptor.getElementIndex(argName)
                    if (elementIndex == CompositeDecoder.UNKNOWN_NAME) {
                        throw IllegalArgumentException(
                            "Path parameter '{$argName}' defined in the DeepLink $uriPattern does not exist in the Serializable class '${serializer.descriptor.serialName}'.",
                        )
                    }

                    val elementDescriptor = serializer.descriptor.getElementDescriptor(elementIndex)

                    add(PathSegment(argName, true, getTypeParser(elementDescriptor.kind)))
                } else {

                    add(PathSegment(segment, false, getTypeParser(PrimitiveKind.STRING)))
                }
            }
        }
    }

    val queryValueParsers: Map<String, TypeParser> by lazy {
        buildMap {
            uriPattern.queryParameterNames.forEach { paramName ->
                val elementIndex = serializer.descriptor.getElementIndex(paramName)

                if (elementIndex != CompositeDecoder.UNKNOWN_NAME) {
                    val elementDescriptor = serializer.descriptor.getElementDescriptor(elementIndex)
                    this[paramName] = getTypeParser(elementDescriptor.kind)
                }
            }
        }
    }

    class PathSegment(
        val stringValue: String,
        val isParamArg: Boolean,
        val typeParser: TypeParser,
    )
}

private typealias TypeParser = (String) -> Serializable

private fun getTypeParser(kind: SerialKind): TypeParser =
    when (kind) {
        PrimitiveKind.STRING -> Any::toString

        PrimitiveKind.INT -> String::toInt

        PrimitiveKind.BOOLEAN -> String::toBoolean

        PrimitiveKind.BYTE -> String::toByte

        PrimitiveKind.CHAR -> String::toCharArray

        PrimitiveKind.DOUBLE -> String::toDouble

        PrimitiveKind.FLOAT -> String::toFloat

        PrimitiveKind.LONG -> String::toLong

        PrimitiveKind.SHORT -> String::toShort

        else -> throw IllegalArgumentException(
            "Unsupported argument type of SerialKind:$kind. The argument type must be a Primitive.",
        )
    }
