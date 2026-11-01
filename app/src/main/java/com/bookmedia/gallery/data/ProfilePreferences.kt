package com.bookmedia.gallery.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class ServerProfile(
    val id: String,
    val name: String,
    val url: String
)

class ProfilePreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("bookmedia_profiles_pref", Context.MODE_PRIVATE)

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    companion object {
        private const val KEY_PROFILES = "saved_server_profiles"
        private const val KEY_ACTIVE_ID = "active_profile_id"
        private const val DEFAULT_URL = "http://192.168.18.11:8080"
    }

    fun getProfiles(): List<ServerProfile> {
        val serialized = prefs.getString(KEY_PROFILES, null)
        if (serialized.isNullOrBlank()) {
            val defaultList = listOf(
                ServerProfile(id = "default", name = "Home WiFi", url = DEFAULT_URL)
            )
            saveProfiles(defaultList)
            return defaultList
        }
        return try {
            json.decodeFromString<List<ServerProfile>>(serialized)
        } catch (e: Exception) {
            listOf(ServerProfile(id = "default", name = "Home WiFi", url = DEFAULT_URL))
        }
    }

    fun saveProfiles(profiles: List<ServerProfile>) {
        val serialized = json.encodeToString(profiles)
        prefs.edit().putString(KEY_PROFILES, serialized).apply()
    }

    fun getActiveProfileId(): String {
        return prefs.getString(KEY_ACTIVE_ID, "default") ?: "default"
    }

    fun setActiveProfileId(id: String) {
        prefs.edit().putString(KEY_ACTIVE_ID, id).apply()
    }

    fun getActiveProfile(): ServerProfile {
        val list = getProfiles()
        val activeId = getActiveProfileId()
        return list.firstOrNull { it.id == activeId } ?: list.firstOrNull() ?: ServerProfile(
            id = "default",
            name = "Home WiFi",
            url = DEFAULT_URL
        )
    }
}
