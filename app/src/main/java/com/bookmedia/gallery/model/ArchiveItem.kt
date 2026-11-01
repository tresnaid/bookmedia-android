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
    val status: String = "archived",
    val type: String = "unsorted",
    val error: String = "",
    @SerialName("updated_at") val updatedAt: String = "",
    @SerialName("created_at") val createdAt: String = "",
    val transcoded: Boolean = false
) {
    val isDirectVideo: Boolean
        get() = submittedUrl.contains(".mp4", ignoreCase = true) ||
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
}

enum class MediaFilter(val label: String) {
    ALL("All"),
    PHOTOS("Photos"),
    VIDEOS("Videos")
}
