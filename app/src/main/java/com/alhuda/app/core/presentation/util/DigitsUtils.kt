package com.alhuda.app.core.presentation.util

fun String.toEnglishDigits(): String =
    replace(Regex("[\u0660-\u0669\u06F0-\u06F9]")) { matchResult ->
        val ch = matchResult.value[0]
        val digit = ch.code and 0xF
        digit.toString()
    }

fun String.filterToDigitsAndDot(
    maxDots: Int = 1,
    allowLeadingMinus: Boolean = false,
): String {
    if (isEmpty()) return this

    var dots = 0
    val out = StringBuilder(length)

    for (ch in this) {
        when {
            allowLeadingMinus && out.isEmpty() && (ch == '-' || ch == '−') -> out.append('-')
            ch in '0'..'9' -> out.append(ch)
            ch in '\u0660'..'\u0669' -> out.append(ch)
            ch in '\u06F0'..'\u06F9' -> out.append(ch)
            ch == '.' -> {
                if (dots < maxDots) {
                    dots++
                    out.append(ch)
                }
            }
        }
    }

    return out.toString()
}
