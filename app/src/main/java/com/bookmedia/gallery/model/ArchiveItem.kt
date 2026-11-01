package com.bookmedia.gallery.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
    @SerialName("downloading_local") val downloadingLocal: Boolean = false,
    @SerialName("local_error") val localError: String = "",
    val status: String = "archived",
    val type: String = "unsorted",
    val error: String = "",
    @SerialName("updated_at") val updatedAt: String = "",
    @SerialName("created_at") val createdAt: String = "",
    val transcoded: Boolean = false
) {
    val hasLocalMedia: Boolean
        get() = localUrl.isNotBlank()

    val isDirectVideo: Boolean
        get() = hasLocalMedia && (localUrl.endsWith(".mp4", ignoreCase = true) || localUrl.endsWith(".mov", ignoreCase = true) || localUrl.endsWith(".webm", ignoreCase = true)) ||
                submittedUrl.contains(".mp4", ignoreCase = true) ||
                submittedUrl.contains(".mov", ignoreCase = true) ||
                canonicalUrl.contains(".mp4", ignoreCase = true) ||
                canonicalUrl.contains(".mov", ignoreCase = true)

    val isVideo: Boolean
        get() = platform.lowercase() in listOf("tiktok", "douyin") ||
                isDirectVideo ||
                (thumbnailUrl.isNotEmpty() && !submittedUrl.matches(Regex(".*\\.(jpg|jpeg|png|webp|gif)(\\?.*)?$", RegexOption.IGNORE_CASE)))

    val effectiveTitle: String
        get() = if (username.isNotBlank()) "@$username" else platform.uppercase()

    val displayUrl: String
        get() = canonicalUrl.ifBlank { submittedUrl }

    fun resolvePlayableMediaUrl(baseUrl: String): String {
        return if (hasLocalMedia) {
            val trimmedBase = baseUrl.trimEnd('/')
            val trimmedPath = localUrl.trimStart('/')
            "$trimmedBase/$trimmedPath"
        } else {
            submittedUrl.ifBlank { canonicalUrl }
        }
    }
}

enum class MediaFilter(val label: String) {
    ALL("All"),
    LOCAL("Local"),
    PHOTOS("Photos"),
    VIDEOS("Videos")
}
