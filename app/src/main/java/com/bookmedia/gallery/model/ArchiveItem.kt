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
    val validLocalFiles: List<LocalMediaFile> by lazy(LazyThreadSafetyMode.PUBLICATION) {
        if (localFiles.isEmpty()) emptyList()
        else localFiles.filter { it.filename != "thumb.jpg" && !it.filename.startsWith("thumb_") && it.size > 0L }
    }

    val hasLocalMedia: Boolean by lazy(LazyThreadSafetyMode.PUBLICATION) {
        storedLocally || validLocalFiles.isNotEmpty() || (localUrl.isNotBlank() && !localUrl.endsWith("thumb.jpg") && localFiles.none { it.size == 0L })
    }

    val totalMediaCount: Int by lazy(LazyThreadSafetyMode.PUBLICATION) {
        when {
            mediaItems.isNotEmpty() -> mediaItems.size
            validLocalFiles.isNotEmpty() -> validLocalFiles.size
            mediaCount > 1 -> mediaCount
            else -> 1
        }
    }

    val isDirectVideo: Boolean by lazy(LazyThreadSafetyMode.PUBLICATION) {
        (hasLocalMedia && (isVideoFilename(localUrl) || validLocalFiles.any { it.type == "video" })) ||
        isVideoFilename(submittedUrl) ||
        isVideoFilename(canonicalUrl)
    }

    val isVideo: Boolean by lazy(LazyThreadSafetyMode.PUBLICATION) {
        platform.lowercase() in VIDEO_PLATFORMS ||
        isDirectVideo ||
        mediaItems.any {
            it.type.equals("video", ignoreCase = true) || isVideoFilename(it.filename)
        } ||
        validLocalFiles.any {
            it.type.equals("video", ignoreCase = true) || isVideoFilename(it.filename)
        } ||
        (thumbnailUrl.isNotEmpty() && !IMAGE_EXT_REGEX.matches(submittedUrl))
    }

    val videoCount: Int by lazy(LazyThreadSafetyMode.PUBLICATION) {
        if (mediaItems.isNotEmpty()) {
            mediaItems.count { mi ->
                mi.type.equals("video", ignoreCase = true) || isVideoFilename(mi.filename)
            }
        } else if (validLocalFiles.isNotEmpty()) {
            validLocalFiles.count { lf ->
                lf.type.equals("video", ignoreCase = true) || isVideoFilename(lf.filename)
            }
        } else {
            if (isVideo) 1 else 0
        }
    }

    val photoCount: Int by lazy(LazyThreadSafetyMode.PUBLICATION) {
        (totalMediaCount - videoCount).coerceAtLeast(0)
    }

    val isCarousel: Boolean by lazy(LazyThreadSafetyMode.PUBLICATION) {
        totalMediaCount > 1
    }

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

            // Prefer local server endpoint /media/$id/thumb.jpg first, as it's permanently reliable
            if (thumbnailUrl.isNotBlank() && thumbnailUrl.startsWith("/media/")) {
                add(thumbnailUrl)
            }
            add("/media/$id/thumb.jpg")

            localPhotos.forEach { add(it.url) }
            mediaItems.forEach { mi ->
                if (mi.thumbnailUrl.isNotBlank() && mi.thumbnailUrl.startsWith("/media/")) add(mi.thumbnailUrl)
                if (mi.type.equals("photo", ignoreCase = true) || isImageUrl(mi.filename)) {
                    add("/media/$id/${mi.filename}")
                }
            }

            if (thumbnailUrl.isNotBlank()) {
                add(thumbnailUrl)
            }
            mediaItems.forEach { mi ->
                if (mi.thumbnailUrl.isNotBlank()) add(mi.thumbnailUrl)
            }

            if (hasLocalMedia) add(localUrl)
            if (isImageUrl(submittedUrl)) add(submittedUrl)
            if (isImageUrl(canonicalUrl)) add(canonicalUrl)
        }

        return candidates
            .asSequence()
            .filter { it.isNotBlank() && !it.startsWith("file://", ignoreCase = true) }
            .map { resolveUrl(baseUrl, it) }
            .distinct()
            .toList()
    }

    fun resolveThumbnailUrl(baseUrl: String, cacheManager: MediaCacheManager? = null): String {
        if (cacheManager != null && cacheManager.isItemCached(id)) {
            val cachedThumb = cacheManager.getCachedThumbnailFile(id)
            if (cachedThumb != null) {
                return "file://${cachedThumb.absolutePath}"
            }
        }
        return resolveThumbnailCandidates(baseUrl).firstOrNull().orEmpty()
    }

    companion object {
        private val VIDEO_PLATFORMS = setOf("tiktok", "douyin")
        private val IMAGE_EXT_REGEX = Regex(".*\\.(jpg|jpeg|png|webp|gif)(\\?.*)?$", RegexOption.IGNORE_CASE)

        fun isVideoFilename(value: String): Boolean {
            if (value.isBlank()) return false
            val lower = value.substringBefore('?').substringBefore('#').lowercase()
            return lower.endsWith(".mp4") || lower.endsWith(".mov") || lower.endsWith(".webm") ||
                   lower.endsWith(".mkv") || lower.endsWith(".m4v")
        }

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
        if (value.isBlank()) return false
        val path = value.substringBefore('?').substringBefore('#').lowercase()
        return path.endsWith(".jpg") || path.endsWith(".jpeg") || path.endsWith(".png") ||
               path.endsWith(".webp") || path.endsWith(".gif") || path.endsWith(".avif") ||
               path.endsWith(".heic") || path.endsWith(".heif")
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

        if (mediaItems.isNotEmpty()) {
            val localMap = validLocalFiles.associateBy { it.filename }
            return mediaItems.mapIndexed { idx, mi ->
                val isVid = mi.type.equals("video", ignoreCase = true) ||
                    mi.filename.endsWith(".mp4", ignoreCase = true) ||
                    mi.filename.endsWith(".mov", ignoreCase = true) ||
                    mi.filename.endsWith(".webm", ignoreCase = true) ||
                    mi.filename.endsWith(".mkv", ignoreCase = true)
                val cached = cacheManager?.getCachedFile(id, mi.filename)
                val lf = localMap[mi.filename]
                val rawMediaUrl = when {
                    lf != null && lf.url.isNotBlank() -> lf.url
                    mi.url.isNotBlank() -> mi.url
                    mi.filename.isNotBlank() -> "/media/$id/${mi.filename}"
                    else -> ""
                }
                val fullUrl = when {
                    cached != null -> "file://${cached.absolutePath}"
                    rawMediaUrl.isNotBlank() -> resolveUrl(baseUrl, rawMediaUrl)
                    else -> resolvePlayableMediaUrl(baseUrl, cacheManager).ifBlank { defaultThumb }
                }
                val itemThumb = when {
                    mi.thumbnailUrl.isNotBlank() && mi.thumbnailUrl != "/media/$id/thumb.jpg" -> resolveUrl(baseUrl, mi.thumbnailUrl)
                    idx == 0 -> defaultThumb
                    else -> "$trimmedBase/media/$id/thumb_${idx + 1}.jpg"
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

        // Single fallback item
        val singleUrl = resolvePlayableMediaUrl(baseUrl, cacheManager)
        val defaultImgThumb = resolveThumbnailUrl(baseUrl, cacheManager).ifBlank { "$trimmedBase/media/$id/thumb.jpg" }
        val finalUrl = when {
            singleUrl.isNotBlank() -> singleUrl
            isImageUrl(submittedUrl) -> resolveUrl(baseUrl, submittedUrl)
            isImageUrl(canonicalUrl) -> resolveUrl(baseUrl, canonicalUrl)
            else -> defaultImgThumb
        }
        return listOf(
            CarouselMediaEntry(
                index = 1,
                url = finalUrl,
                isVideo = isVideo && singleUrl.isNotBlank(),
                filename = "",
                thumbnailUrl = defaultImgThumb
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
