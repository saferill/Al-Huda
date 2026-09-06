package com.alhuda.app.main.quran.model

import kotlinx.serialization.Serializable

@Serializable
data class Surah(
    val number: Int,
    val nameArabic: String,
    val nameLatin: String,
    val translationEn: String,
    val translationId: String,
    val versesCount: Int,
    val isMeccan: Boolean,
    val juz: Int,
)

@Serializable
data class Ayah(
    val numberInSurah: Int,
    val numberInQuran: Int,
    val textArabic: String,
    val textLatin: String = "",
    val translationEn: String,
    val translationId: String,
    val tafsirId: String = "",
    val juz: Int,
)

@Serializable
data class JuzInfo(
    val number: Int,
    val nameArabic: String,
    val startSurahNumber: Int,
    val startSurahName: String,
    val startAyahNumber: Int,
)

@Serializable
data class QuranBookmark(
    val surahNumber: Int,
    val surahName: String,
    val ayahNumber: Int,
    val pageNumber: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),
)
