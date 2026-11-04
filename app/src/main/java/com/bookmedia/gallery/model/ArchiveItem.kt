package com.bookmedia.gallery.model

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
    val type: String = "photo"
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
        get() = localFiles.filter { it.filename != "thumb.jpg" }

    val hasLocalMedia: Boolean
        get() = (localUrl.isNotBlank() && !localUrl.endsWith("thumb.jpg")) || validLocalFiles.isNotEmpty()

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
                (platform.lowercase() != "upload" &&
                 thumbnailUrl.isNotEmpty() &&
                 !submittedUrl.matches(Regex(".*\\.(jpg|jpeg|png|webp|gif)(\\?.*)?$", RegexOption.IGNORE_CASE)))

    val effectiveTitle: String
        get() = when {
            text.isNotBlank() -> text
            username.isNotBlank() && username != "pc_upload" -> "@$username"
            else -> platform.uppercase()
        }

    val displayUrl: String
        get() = canonicalUrl.ifBlank { submittedUrl }

    fun resolveThumbnailUrl(baseUrl: String): String {
        val raw = thumbnailUrl.ifBlank {
            if (!isVideo && submittedUrl.isNotBlank() && !submittedUrl.startsWith("file://")) {
                submittedUrl
            } else if (validLocalFiles.isNotEmpty()) {
                validLocalFiles.firstOrNull()?.url ?: ""
            } else if (mediaItems.isNotEmpty()) {
                "/media/$id/${mediaItems.first().filename}"
            } else {
                ""
            }
        }
        if (raw.isBlank()) return ""
        if (raw.startsWith("http://") || raw.startsWith("https://")) return raw
        val trimmedBase = baseUrl.trimEnd('/')
        val trimmedPath = raw.trimStart('/')
        return "$trimmedBase/$trimmedPath"
    }

    fun resolvePlayableMediaUrl(baseUrl: String): String {
        if (hasLocalMedia) {
            val fileUrl = if (localUrl.isNotBlank() && !localUrl.endsWith("thumb.jpg")) localUrl else (validLocalFiles.firstOrNull()?.url ?: "")
            val trimmedBase = baseUrl.trimEnd('/')
            val trimmedPath = fileUrl.trimStart('/')
            return "$trimmedBase/$trimmedPath"
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
    fun resolveMediaList(baseUrl: String): List<CarouselMediaEntry> {
        val trimmedBase = baseUrl.trimEnd('/')
        val actualFiles = validLocalFiles
        if (actualFiles.isNotEmpty()) {
            return actualFiles.mapIndexed { idx, lf ->
                val fullUrl = "$trimmedBase/${lf.url.trimStart('/')}"
                CarouselMediaEntry(
                    index = idx + 1,
                    url = fullUrl,
                    isVideo = lf.type == "video" || lf.filename.endsWith(".mp4", ignoreCase = true) || lf.filename.endsWith(".mov", ignoreCase = true),
                    filename = lf.filename
                )
            }
        }

        if (mediaItems.isNotEmpty()) {
            return mediaItems.map { mi ->
                val isVid = mi.type == "video" || mi.filename.endsWith(".mp4", ignoreCase = true)
                val fullUrl = "$trimmedBase/media/$id/${mi.filename}"
                CarouselMediaEntry(
                    index = mi.index,
                    url = fullUrl,
                    isVideo = isVid,
                    filename = mi.filename
                )
            }
        }

        // Single fallback item
        val singleUrl = resolvePlayableMediaUrl(baseUrl)
        val finalUrl = if (isVideo && singleUrl.isBlank()) {
            resolveThumbnailUrl(baseUrl).ifBlank { submittedUrl.ifBlank { canonicalUrl } }
        } else {
            singleUrl.ifBlank { submittedUrl.ifBlank { canonicalUrl } }
        }
        return listOf(
            CarouselMediaEntry(
                index = 1,
                url = finalUrl,
                isVideo = isVideo && singleUrl.isNotBlank(),
                filename = ""
            )
        )
    }
}

data class CarouselMediaEntry(
    val index: Int,
    val url: String,
    val isVideo: Boolean,
    val filename: String
)

enum class MediaFilter(val label: String) {
    ALL("All"),
    PHOTOS("Photos"),
    VIDEOS("Videos")
}
