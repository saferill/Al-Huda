package com.alhuda.app.core.domain.model.widget

import androidx.compose.runtime.Immutable
import com.alhuda.app.core.domain.model.adhan.Prayer
import com.alhuda.app.core.domain.model.widget.CustomWidgetConfig.Companion.FONT_SCALE_RANGE
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Immutable
data class CustomWidgetConfig(
    val bgColor: Int? = null,
    val textColor: Int? = null,
    val highlightColor: Int? = null,
    val topStart: HeaderBlock? = null,
    val topEnd: HeaderBlock? = null,

    val rows: List<List<Prayer>> = listOf(emptyList()),
    val showCountdown: Boolean = false,

    val countdownColor: Int? = null,
    val locationIds: List<String> = emptyList(),

    val headerFontScale: Float = 1f,
    val prayerFontScale: Float = 1f,
    val countdownFontScale: Float = 1f,
) {
    companion object {
        const val MAX_PRAYER_ROWS = 2

        val FONT_SCALE_RANGE = 0.5f..2f
    }
}

@Serializable
sealed interface HeaderBlock {
    @Serializable
    @SerialName("date")
    data class Date(
        val calendar: DateCalendar,
        val withDayName: Boolean = false,
    ) : HeaderBlock

    @Serializable
    @SerialName("location")
    data object LocationName : HeaderBlock
}

@Serializable
enum class DateCalendar {
    @SerialName("hijri")
    Hijri,

    @SerialName("gregorian")
    Gregorian,

    @SerialName("persian")
    Persian,

    @SerialName("ethiopic")
    Ethiopic,

    @SerialName("buddhist")
    Buddhist,
}

val DateCalendar.icuCalendar: String?
    get() = if (this == DateCalendar.Hijri) {
        null
    } else {
        DateCalendar.serializer().descriptor.getElementName(ordinal)
    }

fun List<List<Prayer>>.withRowCount(count: Int): List<List<Prayer>> {
    val clamped = count.coerceIn(1, CustomWidgetConfig.MAX_PRAYER_ROWS)
    val rows = toMutableList()
    while (rows.size < clamped) rows.add(emptyList())
    return rows.take(clamped)
}

fun List<List<Prayer>>.withPrayerPlaced(
    prayer: Prayer,
    rowIndex: Int,
    before: Prayer? = null,
): List<List<Prayer>> {
    val cleaned = map { row -> row.filterNot { it == prayer } }.toMutableList()
    if (rowIndex !in cleaned.indices) return this
    val row = cleaned[rowIndex].toMutableList()
    val insertAt = if (before != null && before != prayer) {
        row.indexOf(before).let { if (it < 0) row.size else it }
    } else {
        row.size
    }
    row.add(insertAt.coerceIn(0, row.size), prayer)
    cleaned[rowIndex] = row
    return cleaned
}

fun List<List<Prayer>>.withPrayerRemoved(prayer: Prayer): List<List<Prayer>> = map { row -> row.filterNot { it == prayer } }
