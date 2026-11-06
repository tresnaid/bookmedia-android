package com.bookmedia.gallery.data

import android.content.Context
import com.bookmedia.gallery.model.ArchiveItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class MediaCacheManager(private val context: Context) {
    private val cacheBaseDir: File
        get() = File(context.filesDir, "media_cache").apply { if (!exists()) mkdirs() }

    fun getItemDir(itemId: Long): File {
        return File(cacheBaseDir, itemId.toString()).apply { if (!exists()) mkdirs() }
    }

    fun getCachedThumbnailFile(itemId: Long): File? {
        val dir = File(cacheBaseDir, itemId.toString())
        if (!dir.exists() || !dir.isDirectory) return null
        val candidates = listOf("thumb.jpg", "thumb.png", "thumbnail.jpg", "preview.jpg")
        for (name in candidates) {
            val f = File(dir, name)
            if (f.exists() && f.length() > 0) return f
        }
        val firstImage = dir.listFiles { _, name ->
            val n = name.lowercase()
            n.endsWith(".jpg") || n.endsWith(".jpeg") || n.endsWith(".png") || n.endsWith(".webp")
        }?.firstOrNull()
        return if (firstImage != null && firstImage.length() > 0) firstImage else null
    }

    fun getCachedFile(itemId: Long, filename: String): File? {
        val dir = File(cacheBaseDir, itemId.toString())
        if (!dir.exists()) return null
        val cleanName = File(filename).name
        val f = File(dir, cleanName)
        return if (f.exists() && f.length() > 0) f else null
    }

    fun isItemCached(itemId: Long): Boolean {
        val dir = File(cacheBaseDir, itemId.toString())
        if (!dir.exists() || !dir.isDirectory) return false
        val files = dir.listFiles() ?: return false
        return files.any { it.length() > 0 }
    }

    suspend fun cacheItemMedia(baseUrl: String, item: ArchiveItem): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val targetDir = getItemDir(item.id)
            val trimmedBase = baseUrl.trimEnd('/')

            // 1. Download thumb if missing
            val thumbTarget = File(targetDir, "thumb.jpg")
            if (!thumbTarget.exists() || thumbTarget.length() == 0L) {
                val candidateThumbUrls = buildList {
                    if (item.thumbnailUrl.isNotBlank()) add(item.thumbnailUrl)
                    add("/media/${item.id}/thumb.jpg")
                }
                for (u in candidateThumbUrls) {
                    val fullUrl = if (u.startsWith("http://") || u.startsWith("https://")) u else "$trimmedBase/${u.trimStart('/')}"
                    if (downloadToFile(fullUrl, thumbTarget)) break
                }
            }

            // 2. Download media files (images or video)
            val mediaUrls = buildList {
                item.validLocalFiles.forEach { lf ->
                    val name = lf.filename.ifBlank { File(lf.url).name }
                    add(Pair(lf.url, name))
                }
                item.mediaItems.forEach { mi ->
                    val name = mi.filename.trim()
                    if (name.isNotBlank()) {
                        add(Pair("/media/${item.id}/$name", name))
                    }
                }
                if (isEmpty() && item.hasLocalMedia && item.localUrl.isNotBlank()) {
                    add(Pair(item.localUrl, File(item.localUrl).name))
                }
            }.distinctBy { it.second }

            for ((urlPath, fname) in mediaUrls) {
                if (fname.isBlank()) continue
                val fileTarget = File(targetDir, fname)
                if (fileTarget.exists() && fileTarget.length() > 0L) continue
                val fullUrl = if (urlPath.startsWith("http://") || urlPath.startsWith("https://")) {
                    urlPath
                } else {
                    "$trimmedBase/${urlPath.trimStart('/')}"
                }
                downloadToFile(fullUrl, fileTarget)
            }
            true
        }.getOrDefault(false)
    }

    private fun downloadToFile(urlString: String, destinationFile: File): Boolean {
        return try {
            val tempFile = File(destinationFile.parentFile, "${destinationFile.name}.tmp")
            val url = URL(urlString)
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 10000
            conn.readTimeout = 25000
            conn.instanceFollowRedirects = true

            if (conn.responseCode in 200..299) {
                conn.inputStream.use { input ->
                    FileOutputStream(tempFile).use { output ->
                        input.copyTo(output, bufferSize = 32768)
                    }
                }
                if (tempFile.exists() && tempFile.length() > 0) {
                    if (destinationFile.exists()) destinationFile.delete()
                    tempFile.renameTo(destinationFile)
                    true
                } else {
                    tempFile.delete()
                    false
                }
            } else {
                conn.disconnect()
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    fun removeCachedItem(itemId: Long) {
        val dir = File(cacheBaseDir, itemId.toString())
        if (dir.exists()) {
            dir.deleteRecursively()
        }
    }

    fun getCacheSizeBytes(): Long {
        if (!cacheBaseDir.exists()) return 0L
        return cacheBaseDir.walkTopDown().filter { it.isFile }.map { it.length() }.sum()
    }

    fun clearCache() {
        if (cacheBaseDir.exists()) {
            cacheBaseDir.deleteRecursively()
            cacheBaseDir.mkdirs()
        }
    }
}
