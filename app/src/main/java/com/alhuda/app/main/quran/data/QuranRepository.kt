package com.alhuda.app.main.quran.data

import android.content.Context
import com.alhuda.app.main.quran.model.Ayah
import com.alhuda.app.main.quran.model.QuranBookmark
import com.alhuda.app.main.quran.model.Surah
import com.tencent.mmkv.MMKV
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuranRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val mmkv: MMKV,
    private val pageLoader: QuranPageLoader,
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    private val _bookmark = MutableStateFlow(loadBookmark())
    val bookmark: Flow<QuranBookmark?> = _bookmark.asStateFlow()

    fun getAllSurahs(): List<Surah> = QuranDataSource.surahs
    fun getAllJuz(): List<com.alhuda.app.main.quran.model.JuzInfo> = QuranDataSource.juzList

    fun getSurah(number: Int): Surah? = QuranDataSource.surahs.firstOrNull { it.number == number }

    fun getStartPageForSurah(surahNumber: Int): Int = QuranDataSource.getStartPageForSurah(surahNumber)
    fun getStartPageForJuz(juzNumber: Int): Int = QuranDataSource.getStartPageForJuz(juzNumber)
    fun getSurahForPage(page: Int): Surah = QuranDataSource.getSurahForPage(page)
    fun getJuzForPage(page: Int): Int = QuranDataSource.getJuzForPage(page)

    suspend fun getPageBitmap(page: Int): android.graphics.Bitmap? = pageLoader.getPageBitmap(page)

    suspend fun getAyahs(surahNumber: Int): List<Ayah> = withContext(Dispatchers.IO) {
        val assetAyahs = loadAyahsFromAssets(surahNumber)
        if (assetAyahs.isNotEmpty()) return@withContext assetAyahs

        val cacheKey = "$KEY_AYAH_CACHE_PREFIX$surahNumber"
        val cached = mmkv.decodeString(cacheKey)
        if (!cached.isNullOrBlank()) {
            try {
                val list = json.decodeFromString(ListSerializer(Ayah.serializer()), cached)
                if (list.isNotEmpty()) return@withContext list
            } catch (_: Exception) {}
        }

        val fetched = tryFetchFromEquran(surahNumber) ?: tryFetchFromAlQuranCloud(surahNumber)
        if (fetched != null && fetched.isNotEmpty()) {
            try {
                val serialized = json.encodeToString(ListSerializer(Ayah.serializer()), fetched)
                mmkv.encode(cacheKey, serialized)
            } catch (_: Exception) {}
            return@withContext fetched
        }

        emptyList()
    }

    private fun loadAyahsFromAssets(surahNumber: Int): List<Ayah> {
        return try {
            val jsonText = context.assets.open("quran/surah/$surahNumber.json").bufferedReader().use { it.readText() }
            json.decodeFromString(ListSerializer(Ayah.serializer()), jsonText)
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun tryFetchFromEquran(surahNumber: Int): List<Ayah>? {
        return try {
            val url = URL("https://equran.id/api/v2/surat/$surahNumber")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 6000
                readTimeout = 6000
                requestMethod = "GET"
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", "Mozilla/5.0 AlHudaApp")
            }
            if (conn.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8"))
                val body = reader.readText()
                reader.close()
                conn.disconnect()

                val root = json.parseToJsonElement(body).jsonObject
                val data = root["data"]?.jsonObject ?: return null
                val ayatArray = data["ayat"]?.jsonArray ?: return null
                val surah = getSurah(surahNumber)
                val defaultJuz = surah?.juz ?: 1

                ayatArray.mapNotNull { element ->
                    val obj = element.jsonObject
                    val nomorAyat = obj["nomorAyat"]?.jsonPrimitive?.content?.toIntOrNull() ?: return@mapNotNull null
                    val teksArab = obj["teksArab"]?.jsonPrimitive?.content ?: ""
                    val teksLatin = obj["teksLatin"]?.jsonPrimitive?.content ?: ""
                    val teksIndonesia = obj["teksIndonesia"]?.jsonPrimitive?.content ?: ""

                    Ayah(
                        numberInSurah = nomorAyat,
                        numberInQuran = 0,
                        textArabic = teksArab,
                        textLatin = teksLatin,
                        translationEn = "",
                        translationId = teksIndonesia,
                        tafsirId = "",
                        juz = defaultJuz,
                    )
                }
            } else {
                conn.disconnect()
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun tryFetchFromAlQuranCloud(surahNumber: Int): List<Ayah>? {
        return try {
            val url = URL("https://api.alquran.cloud/v1/surah/$surahNumber/editions/quran-uthmani,id.indonesian")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 6000
                readTimeout = 6000
                requestMethod = "GET"
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", "Mozilla/5.0 AlHudaApp")
            }
            if (conn.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8"))
                val body = reader.readText()
                reader.close()
                conn.disconnect()

                val root = json.parseToJsonElement(body).jsonObject
                val dataArray = root["data"]?.jsonArray ?: return null
                if (dataArray.size < 2) return null

                val arabicAyahs = dataArray[0].jsonObject["ayahs"]?.jsonArray ?: return null
                val indoAyahs = dataArray[1].jsonObject["ayahs"]?.jsonArray ?: return null
                val surah = getSurah(surahNumber)

                arabicAyahs.mapIndexedNotNull { index, elem ->
                    val arObj = elem.jsonObject
                    val idObj = indoAyahs.getOrNull(index)?.jsonObject
                    val numInSurah = arObj["numberInSurah"]?.jsonPrimitive?.content?.toIntOrNull() ?: (index + 1)
                    val numInQuran = arObj["number"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0
                    val textAr = arObj["text"]?.jsonPrimitive?.content ?: ""
                    val textId = idObj?.get("text")?.jsonPrimitive?.content ?: ""
                    val juzNum = arObj["juz"]?.jsonPrimitive?.content?.toIntOrNull() ?: (surah?.juz ?: 1)

                    Ayah(
                        numberInSurah = numInSurah,
                        numberInQuran = numInQuran,
                        textArabic = textAr,
                        textLatin = "",
                        translationEn = "",
                        translationId = textId,
                        tafsirId = "",
                        juz = juzNum,
                    )
                }
            } else {
                conn.disconnect()
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    fun searchSurahs(query: String): List<Surah> {
        if (query.isBlank()) return QuranDataSource.surahs
        val q = query.trim().lowercase()
        return QuranDataSource.surahs.filter {
            it.number.toString() == q ||
                it.nameLatin.lowercase().contains(q) ||
                it.translationId.lowercase().contains(q) ||
                it.translationEn.lowercase().contains(q) ||
                it.nameArabic.contains(q)
        }
    }

    fun saveBookmark(surahNumber: Int, ayahNumber: Int) {
        val surah = getSurah(surahNumber) ?: return
        val startPage = getStartPageForSurah(surahNumber)
        val bookmark = QuranBookmark(surahNumber, surah.nameLatin, ayahNumber, startPage)
        try {
            val serialized = json.encodeToString(QuranBookmark.serializer(), bookmark)
            mmkv.encode(KEY_BOOKMARK, serialized)
            _bookmark.value = bookmark
        } catch (_: Exception) {}
    }

    fun saveBookmarkPage(page: Int) {
        val clamped = page.coerceIn(1, 604)
        val surah = getSurahForPage(clamped)
        val bookmark = QuranBookmark(surah.number, surah.nameLatin, 1, clamped)
        try {
            val serialized = json.encodeToString(QuranBookmark.serializer(), bookmark)
            mmkv.encode(KEY_BOOKMARK, serialized)
            _bookmark.value = bookmark
        } catch (_: Exception) {}
    }

    private fun loadBookmark(): QuranBookmark? {
        val raw = mmkv.decodeString(KEY_BOOKMARK) ?: return null
        return try {
            json.decodeFromString(QuranBookmark.serializer(), raw)
        } catch (_: Exception) {
            null
        }
    }

    companion object {
        private const val KEY_BOOKMARK = "quran_last_read_bookmark"
        private const val KEY_AYAH_CACHE_PREFIX = "quran_surah_ayahs_v2_"
    }
}
