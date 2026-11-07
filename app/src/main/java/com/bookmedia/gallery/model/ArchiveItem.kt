package com.bookmedia.gallery.model

import com.bookmedia.gallery.data.MediaCacheManager
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LocalMediaFile(
    val filename: String = "",
    val url: String = "",
    val type: String = "photo",
    val size: Long = 0L
)

@Serializable
data class MediaItemInfo(
    val index: Int = 1,
    val filename: String = "",
    val type: String = "photo",
    val url: String = "",
    @SerialName("thumbnail_url") val thumbnailUrl: String = ""
)

@Serializable
data class ArchiveItem(
    val id: Long,
    val platform: String = "",
    @SerialName("post_id") val postId: String = "",
    @SerialName("submitted_url") val submittedUrl: String = "",
    @SerialName("canonical_url") val canonicalUrl: String = "",
    val username: String = "",
    @SerialName("thumbnail_url") val thumbnailUrl: String = "",
    @SerialName("local_url") val localUrl: String = "",
    @SerialName("local_files") val localFiles: List<LocalMediaFile> = emptyList(),
    @SerialName("stored_locally") val storedLocally: Boolean = false,
    @SerialName("media_count") val mediaCount: Int = 1,
    @SerialName("media_items") val mediaItems: List<MediaItemInfo> = emptyList(),
    @SerialName("downloading_local") val downloadingLocal: Boolean = false,
    @SerialName("local_error") val localError: String = "",
    val status: String = "archived",
    val type: String = "unsorted",
    val text: String = "",
    val error: String = "",
    @SerialName("updated_at") val updatedAt: String = "",
    @SerialName("created_at") val createdAt: String = "",
    val transcoded: Boolean = false
) {
    val validLocalFiles: List<LocalMediaFile>
        get() = localFiles.filter { it.filename != "thumb.jpg" && !it.filename.startsWith("thumb_") && it.size > 0L }

    val hasLocalMedia: Boolean
        get() = storedLocally || validLocalFiles.isNotEmpty() || (localUrl.isNotBlank() && !localUrl.endsWith("thumb.jpg") && localFiles.none { it.size == 0L })

    val totalMediaCount: Int
        get() = when {
            validLocalFiles.isNotEmpty() -> validLocalFiles.size
            mediaItems.isNotEmpty() -> mediaItems.size
            mediaCount > 1 -> mediaCount
            else -> 1
        }

    val isCarousel: Boolean
        get() = totalMediaCount > 1

    val isDirectVideo: Boolean
        get() = (hasLocalMedia && (localUrl.endsWith(".mp4", ignoreCase = true) || localUrl.endsWith(".mov", ignoreCase = true) || localUrl.endsWith(".webm", ignoreCase = true) || validLocalFiles.any { it.type == "video" })) ||
                submittedUrl.contains(".mp4", ignoreCase = true) ||
                submittedUrl.contains(".mov", ignoreCase = true) ||
                canonicalUrl.contains(".mp4", ignoreCase = true) ||
                canonicalUrl.contains(".mov", ignoreCase = true)

    val isVideo: Boolean
        get() = platform.lowercase() in listOf("tiktok", "douyin") ||
                isDirectVideo ||
                mediaItems.any {
                    it.type.equals("video", ignoreCase = true) ||
                    it.filename.endsWith(".mp4", ignoreCase = true) ||
                    it.filename.endsWith(".mov", ignoreCase = true) ||
                    it.filename.endsWith(".webm", ignoreCase = true) ||
                    it.filename.endsWith(".mkv", ignoreCase = true)
                } ||
                validLocalFiles.any {
                    it.type.equals("video", ignoreCase = true) ||
                    it.filename.endsWith(".mp4", ignoreCase = true) ||
                    it.filename.endsWith(".mov", ignoreCase = true) ||
                    it.filename.endsWith(".webm", ignoreCase = true)
                } ||
                (thumbnailUrl.isNotEmpty() &&
                 !submittedUrl.matches(Regex(".*\\.(jpg|jpeg|png|webp|gif)(\\?.*)?$", RegexOption.IGNORE_CASE)))

    val effectiveTitle: String
        get() = when {
            text.isNotBlank() -> text
            username.isNotBlank() && username != "pc_upload" -> "@$username"
            else -> platform.uppercase()
        }

    val displayUrl: String
        get() = canonicalUrl.ifBlank { submittedUrl }

    fun resolveThumbnailCandidates(baseUrl: String): List<String> {
        val candidates = buildList {
            val localPhotos = validLocalFiles.filter {
                it.type.equals("photo", ignoreCase = true) || isImageUrl(it.url) || isImageUrl(it.filename)
            }

            if (thumbnailUrl.isNotBlank()) {
                add(thumbnailUrl)
            }
            add("/media/$id/thumb.jpg")

            localPhotos.forEach { add(it.url) }
            mediaItems.forEach { mi ->
                if (mi.thumbnailUrl.isNotBlank()) add(mi.thumbnailUrl)
                if (mi.type.equals("photo", ignoreCase = true) || isImageUrl(mi.filename)) {
                    add("/media/$id/${mi.filename}")
                }
            }

            if (hasLocalMedia) add(localUrl)
            add(submittedUrl)
            add(canonicalUrl)
        }

        return candidates
            .asSequence()
            .filter { it.isNotBlank() && !it.startsWith("file://", ignoreCase = true) }
            .map { resolveUrl(baseUrl, it) }
            .distinct()
            .toList()
    }

    fun resolveThumbnailUrl(baseUrl: String, cacheManager: MediaCacheManager? = null): String {
        if (cacheManager != null) {
            val cachedThumb = cacheManager.getCachedThumbnailFile(id)
            if (cachedThumb != null) {
                return "file://${cachedThumb.absolutePath}"
            }
        }
        return resolveThumbnailCandidates(baseUrl).firstOrNull().orEmpty()
    }

    companion object {
        fun encodeUrlPath(raw: String): String {
            if (raw.isBlank() || raw.startsWith("file://", ignoreCase = true)) return raw
            val schemeEnd = raw.indexOf("://")
            val (base, pathAndQuery) = if (schemeEnd != -1) {
                val pathStart = raw.indexOf('/', schemeEnd + 3)
                if (pathStart == -1) return raw
                raw.substring(0, pathStart) to raw.substring(pathStart)
            } else {
                "" to raw
            }

            val queryIdx = pathAndQuery.indexOf('?')
            val (pathOnly, query) = if (queryIdx != -1) {
                pathAndQuery.substring(0, queryIdx) to pathAndQuery.substring(queryIdx)
            } else {
                pathAndQuery to ""
            }

            val encodedPath = pathOnly.split('/').joinToString("/") { segment ->
                if (segment.isEmpty()) ""
                else {
                    try {
                        val decoded = java.net.URLDecoder.decode(segment, "UTF-8")
                        java.net.URLEncoder.encode(decoded, "UTF-8")
                            .replace("+", "%20")
                            .replace("%21", "!")
                            .replace("%27", "'")
                            .replace("%28", "(")
                            .replace("%29", ")")
                            .replace("%7E", "~")
                    } catch (_: Exception) {
                        segment
                    }
                }
            }
            return "$base$encodedPath$query"
        }
    }

    fun resolveUrl(baseUrl: String, raw: String): String {
        val url = if (raw.startsWith("http://", ignoreCase = true) || raw.startsWith("https://", ignoreCase = true)) {
            raw
        } else {
            val trimmedBase = baseUrl.trimEnd('/')
            val trimmedPath = raw.trimStart('/')
            "$trimmedBase/$trimmedPath"
        }
        return encodeUrlPath(url)
    }

    private fun isImageUrl(value: String): Boolean {
        return value.substringBefore('?').substringBefore('#').lowercase().let { path ->
            listOf(".jpg", ".jpeg", ".png", ".webp", ".gif", ".avif", ".heic", ".heif")
                .any(path::endsWith)
        }
    }

    fun resolvePlayableMediaUrl(baseUrl: String, cacheManager: MediaCacheManager? = null): String {
        if (cacheManager != null) {
            val itemDir = cacheManager.getItemDir(id)
            if (itemDir.exists()) {
                val cachedVid = itemDir.listFiles { _, name ->
                    val n = name.lowercase()
                    n.endsWith(".mp4") || n.endsWith(".mov") || n.endsWith(".webm") || n.endsWith(".mkv")
                }?.firstOrNull()
                if (cachedVid != null) return "file://${cachedVid.absolutePath}"
            }
        }
        if (hasLocalMedia) {
            val fileUrl = if (localUrl.isNotBlank() && !localUrl.endsWith("thumb.jpg")) localUrl else (validLocalFiles.firstOrNull()?.url ?: "")
            val trimmedBase = baseUrl.trimEnd('/')
            val trimmedPath = fileUrl.trimStart('/')
            return resolveUrl(baseUrl, "$trimmedBase/$trimmedPath")
        }
        val directUrl = submittedUrl.ifBlank { canonicalUrl }
        // Only return if it's an actual direct playable media stream (not a webpage like tiktok.com/@user/video/...)
        val playableExts = listOf(".mp4", ".mov", ".webm", ".mkv", ".m4v", ".m3u8")
        if (playableExts.any { directUrl.contains(it, ignoreCase = true) }) {
            return directUrl
        }
        return ""
    }

    /**
     * Resolve all carousel media items for fullscreen viewing
     */
    fun resolveMediaList(baseUrl: String, cacheManager: MediaCacheManager? = null): List<CarouselMediaEntry> {
        val trimmedBase = baseUrl.trimEnd('/')
        val defaultThumb = resolveThumbnailUrl(baseUrl, cacheManager)
        val actualFiles = validLocalFiles
        if (actualFiles.isNotEmpty()) {
            return actualFiles.mapIndexed { idx, lf ->
                val cached = cacheManager?.getCachedFile(id, lf.filename)
                val fullUrl = if (cached != null) "file://${cached.absolutePath}" else resolveUrl(baseUrl, lf.url)
                val itemThumb = if (idx == 0) {
                    defaultThumb
                } else {
                    "$trimmedBase/media/$id/thumb_${idx + 1}.jpg"
                }
                CarouselMediaEntry(
                    index = idx + 1,
                    url = fullUrl,
                    isVideo = lf.type == "video" || lf.filename.endsWith(".mp4", ignoreCase = true) || lf.filename.endsWith(".mov", ignoreCase = true),
                    filename = lf.filename,
                    thumbnailUrl = itemThumb
                )
            }
        }

        if (mediaItems.isNotEmpty()) {
            return mediaItems.map { mi ->
                val isVid = mi.type.equals("video", ignoreCase = true) ||
                    mi.filename.endsWith(".mp4", ignoreCase = true) ||
                    mi.filename.endsWith(".mov", ignoreCase = true) ||
                    mi.filename.endsWith(".webm", ignoreCase = true) ||
                    mi.filename.endsWith(".mkv", ignoreCase = true)
                val cached = cacheManager?.getCachedFile(id, mi.filename)
                val rawMediaUrl = mi.url.ifBlank { "/media/$id/${mi.filename}" }
                val fullUrl = if (cached != null) "file://${cached.absolutePath}" else resolveUrl(baseUrl, rawMediaUrl)
                val itemThumb = when {
                    mi.index == 1 -> defaultThumb
                    mi.thumbnailUrl.isNotBlank() -> resolveUrl(baseUrl, mi.thumbnailUrl)
                    else -> "$trimmedBase/media/$id/thumb_${mi.index}.jpg"
                }
                CarouselMediaEntry(
                    index = mi.index,
                    url = fullUrl,
                    isVideo = isVid,
                    filename = mi.filename,
                    thumbnailUrl = itemThumb
                )
            }
        }

        // Single fallback item
        val singleUrl = resolvePlayableMediaUrl(baseUrl, cacheManager)
        val finalUrl = if (isVideo && singleUrl.isBlank()) {
            resolveThumbnailUrl(baseUrl, cacheManager).ifBlank { submittedUrl.ifBlank { canonicalUrl } }
        } else {
            singleUrl.ifBlank { submittedUrl.ifBlank { canonicalUrl } }
        }
        return listOf(
            CarouselMediaEntry(
                index = 1,
                url = finalUrl,
                isVideo = isVideo && singleUrl.isNotBlank(),
                filename = "",
                thumbnailUrl = resolveThumbnailUrl(baseUrl, cacheManager)
            )
        )
    }
}

data class CarouselMediaEntry(
    val index: Int,
    val url: String,
    val isVideo: Boolean,
    val filename: String,
    val thumbnailUrl: String = ""
)

enum class MediaFilter(val label: String) {
    ALL("All"),
    PHOTOS("Photos"),
    VIDEOS("Videos"),
    FAVORITES("Favorites")
}
