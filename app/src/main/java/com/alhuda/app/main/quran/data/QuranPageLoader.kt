package com.alhuda.app.main.quran.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuranPageLoader @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val memoryCache = object : LruCache<Int, Bitmap>(30) {
        override fun sizeOf(key: Int, value: Bitmap): Int {
            return value.byteCount / 1024
        }
    }

    private val cacheDir = File(context.cacheDir, "quran_mushaf_pages").apply {
        if (!exists()) mkdirs()
    }

    suspend fun getPageBitmap(page: Int): Bitmap? = withContext(Dispatchers.IO) {
        if (page !in 1..604) return@withContext null

        synchronized(memoryCache) {
            val cached = memoryCache.get(page)
            if (cached != null && !cached.isRecycled) {
                return@withContext cached
            }
        }

        val assetName = String.format(java.util.Locale.US, "quran/pages/page_%03d.png", page)
        try {
            val stream = context.assets.open(assetName)
            val bitmap = BitmapFactory.decodeStream(stream)
            stream.close()
            if (bitmap != null) {
                synchronized(memoryCache) {
                    memoryCache.put(page, bitmap)
                }
                return@withContext bitmap
            }
        } catch (_: Exception) {}

        val file = File(cacheDir, String.format(java.util.Locale.US, "page_%03d.png", page))
        if (file.exists() && file.length() > 0) {
            try {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                if (bitmap != null) {
                    synchronized(memoryCache) {
                        memoryCache.put(page, bitmap)
                    }
                    return@withContext bitmap
                }
            } catch (_: Exception) {}
        }

        val downloaded = downloadPage(page, file)
        if (downloaded != null) {
            synchronized(memoryCache) {
                memoryCache.put(page, downloaded)
            }
        }
        downloaded
    }

    private fun downloadPage(page: Int, targetFile: File): Bitmap? {
        val urlString = String.format("https://android.quran.com/data/width_1024/page%03d.png", page)
        var connection: HttpURLConnection? = null
        return try {
            val url = URL(urlString)
            connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 10000
                readTimeout = 10000
                requestMethod = "GET"
                setRequestProperty("User-Agent", "Mozilla/5.0 AlHudaApp")
            }
            if (connection.responseCode == 200) {
                val bytes = connection.inputStream.use { it.readBytes() }
                if (bytes.isNotEmpty()) {
                    try {
                        FileOutputStream(targetFile).use { it.write(bytes) }
                    } catch (_: Exception) {}
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                } else null
            } else null
        } catch (_: Exception) {
            null
        } finally {
            connection?.disconnect()
        }
    }
}
