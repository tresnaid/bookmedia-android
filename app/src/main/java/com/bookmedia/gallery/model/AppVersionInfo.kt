package com.bookmedia.gallery.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppVersionInfo(
    @SerialName("version_code") val versionCode: Int = 0,
    @SerialName("version_name") val versionName: String = "",
    @SerialName("apk_url") val apkUrl: String = "",
    @SerialName("changelog") val changelog: String = "",
    @SerialName("file_size") val fileSize: Long = 0,
    @SerialName("updated_at") val updatedAt: String = ""
)
