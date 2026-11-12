package com.bookmedia.gallery.model

enum class CreatorSortOption(val label: String) {
    ALBUMS_DESC("Most Albums"),
    MEDIA_DESC("Most Media"),
    NAME_ASC("Name (A–Z)"),
    RECENT("Recent Archive")
}

data class CreatorGroup(
    val name: String,
    val albums: List<ArchiveItem>,
    val totalMediaCount: Int,
    val latestThumbnailUrl: String
)
