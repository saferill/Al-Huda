package com.alhuda.app.main.quran.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
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
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val memoryCache = object : LruCache<Int, Bitmap>(30) {
        override fun sizeOf(key: Int, value: Bitmap): Int {
            return value.byteCount / 1024
        }
    }

    private val storageDir = File(context.filesDir, "quran_mushaf_pages").apply {
        if (!exists()) mkdirs()
    }

    suspend fun getPageBitmap(page: Int): Bitmap? = withContext(Dispatchers.IO) {
        if (page !in 1..604) return@withContext null

        synchronized(memoryCache) {
            val cached = memoryCache.get(page)
            if (cached != null && !cached.isRecycled) {
                prefetchAdjacentPages(page)
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
                prefetchAdjacentPages(page)
                return@withContext bitmap
            }
        } catch (_: Exception) {}

        val file = File(storageDir, String.format(java.util.Locale.US, "page_%03d.png", page))
        if (file.exists() && file.length() > 0) {
            try {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                if (bitmap != null) {
                    synchronized(memoryCache) {
                        memoryCache.put(page, bitmap)
                    }
                    prefetchAdjacentPages(page)
                    return@withContext bitmap
                }
            } catch (_: Exception) {}
        }

        val downloaded = downloadPage(page, file)
        if (downloaded != null) {
            synchronized(memoryCache) {
                memoryCache.put(page, downloaded)
            }
            prefetchAdjacentPages(page)
        }
        downloaded
    }

    private fun prefetchAdjacentPages(currentPage: Int) {
        scope.launch {
            val nextPage = currentPage + 1
            val prevPage = currentPage - 1
            if (nextPage in 1..604) {
                prefetchSinglePage(nextPage)
            }
            if (prevPage in 1..604) {
                prefetchSinglePage(prevPage)
            }
        }
    }

    private fun prefetchSinglePage(page: Int) {
        synchronized(memoryCache) {
            if (memoryCache.get(page) != null) return
        }
        val file = File(storageDir, String.format(java.util.Locale.US, "page_%03d.png", page))
        if (file.exists() && file.length() > 0) {
            try {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                if (bitmap != null) {
                    synchronized(memoryCache) {
                        memoryCache.put(page, bitmap)
                    }
                }
            } catch (_: Exception) {}
            return
        }
        downloadPage(page, file)?.let { downloaded ->
            synchronized(memoryCache) {
                memoryCache.put(page, downloaded)
            }
        }
    }

    private fun downloadPage(page: Int, targetFile: File): Bitmap? {
        val urlString = String.format(java.util.Locale.US, "https://android.quran.com/data/width_1024/page%03d.png", page)
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
