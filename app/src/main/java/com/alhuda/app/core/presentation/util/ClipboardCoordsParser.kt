package com.alhuda.app.core.presentation.util

import kotlin.math.abs

fun parseClipboardToCoords(text: String?): Pair<Double, Double>? {
    if (text.isNullOrBlank()) return null

    val cleaned =
        text
            .toEnglishDigits()
            .replace(Regex("(?i)(and|latitude|longitude)"), " ")
            .trim()

    val norm =
        cleaned
            .replace(Regex("[‘’′]"), "'")
            .replace(Regex("[“”″]"), "\"")
            .replace("''", "\"")

    val dmsRegex =
        Regex(
            "([+-]?\\d+(?:\\.\\d+)?)\\s*(?:°|deg)?\\s*(\\d+(?:\\.\\d+)?)?\\s*(?:'|m)?\\s*(\\d+(?:\\.\\d+)?)?\\s*(?:\\\"|s)?\\s*([NSEW])?",
            RegexOption.IGNORE_CASE,
        )

    data class DmsPart(val num: Double, val dir: Char?, val origDeg: Double)

    val dmsParts = ArrayList<DmsPart>(2)
    for (m in dmsRegex.findAll(norm)) {
        val deg = m.groupValues.getOrNull(1)?.toDoubleOrNull() ?: continue
        val min = m.groupValues.getOrNull(2)?.toDoubleOrNull() ?: 0.0
        val sec = m.groupValues.getOrNull(3)?.toDoubleOrNull() ?: 0.0
        val dir = m.groupValues.getOrNull(4)?.trim()?.uppercase()?.firstOrNull()

        val decimal = abs(deg) + (min / 60.0) + (sec / 3600.0)
        dmsParts.add(DmsPart(num = decimal, dir = dir, origDeg = deg))
        if (dmsParts.size >= 2) break
    }

    if (dmsParts.size >= 2) {
        var lat = dmsParts[0].num * if (dmsParts[0].origDeg < 0) -1 else 1
        var lon = dmsParts[1].num * if (dmsParts[1].origDeg < 0) -1 else 1

        when (dmsParts[0].dir) {
            'S' -> lat = -abs(lat)
            'N' -> lat = abs(lat)
        }
        when (dmsParts[1].dir) {
            'W' -> lon = -abs(lon)
            'E' -> lon = abs(lon)
        }
        return lat to lon
    }

    val decRegex =
        Regex(
            "([+-]?\\d+(?:\\.\\d+)?)(?:\\s*°)?\\s*([NSEW])?",
            RegexOption.IGNORE_CASE,
        )

    data class Part(val num: Double, val dir: Char?)

    val parts = ArrayList<Part>(2)
    for (m in decRegex.findAll(norm)) {
        val num = m.groupValues.getOrNull(1)?.toDoubleOrNull() ?: continue
        val dir = m.groupValues.getOrNull(2)?.trim()?.uppercase()?.firstOrNull()
        parts.add(Part(num = num, dir = dir))
        if (parts.size >= 2) break
    }

    if (parts.size >= 2) {
        var lat = parts[0].num
        var lon = parts[1].num

        when (parts[0].dir) {
            'S' -> lat = -abs(lat)
            'N' -> lat = abs(lat)
        }
        when (parts[1].dir) {
            'W' -> lon = -abs(lon)
            'E' -> lon = abs(lon)
        }
        return lat to lon
    }

    val nums = Regex("-?\\d+(?:\\.\\d+)?").findAll(norm).mapNotNull { it.value.toDoubleOrNull() }.toList()
    if (nums.size >= 2) {
        var lat = nums[0]
        var lon = nums[1]

        if (Regex("[sS]").containsMatchIn(norm) && !Regex("[nN]").containsMatchIn(norm)) {
            lat = -abs(lat)
        }
        if (Regex("[wW]").containsMatchIn(norm) && !Regex("[eE]").containsMatchIn(norm)) {
            lon = -abs(lon)
        }

        return lat to lon
    }

    return null
}
