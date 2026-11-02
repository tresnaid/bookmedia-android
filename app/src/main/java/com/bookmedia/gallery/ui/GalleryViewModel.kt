package com.bookmedia.gallery.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bookmedia.gallery.data.BookmediaRepository
import com.bookmedia.gallery.data.ProfilePreferences
import com.bookmedia.gallery.data.ServerProfile
import com.bookmedia.gallery.model.ArchiveItem
import com.bookmedia.gallery.model.MediaFilter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class SortOrder(val label: String) {
    NEWEST("Newest"),
    OLDEST("Oldest")
}

data class GalleryUiState(
    val items: List<ArchiveItem> = emptyList(),
    val filteredItems: List<ArchiveItem> = emptyList(),
    val availablePlatforms: List<String> = emptyList(),
    val selectedMediaFilter: MediaFilter = MediaFilter.ALL,
    val sortOrder: SortOrder = SortOrder.NEWEST,
    val isLocalOnly: Boolean = false,
    val selectedPlatform: String? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val activeViewerIndex: Int? = null,
    val serverUrl: String = "http://192.168.18.11:8080",
    val profiles: List<ServerProfile> = emptyList(),
    val activeProfile: ServerProfile? = null
)

class GalleryViewModel(application: Application) : AndroidViewModel(application) {

    private val profilePrefs = ProfilePreferences(application)
    private val repository = BookmediaRepository(profilePrefs.getActiveProfile().url)

    private val _rawItems = MutableStateFlow<List<ArchiveItem>>(emptyList())
    private val _selectedMediaFilter = MutableStateFlow(MediaFilter.ALL)
    private val _sortOrder = MutableStateFlow(SortOrder.NEWEST)
    private val _isLocalOnly = MutableStateFlow(false)
    private val _selectedPlatform = MutableStateFlow<String?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _isLoading = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _activeViewerIndex = MutableStateFlow<Int?>(null)

    private val _profiles = MutableStateFlow(profilePrefs.getProfiles())
    private val _activeProfile = MutableStateFlow(profilePrefs.getActiveProfile())
    private val _isFilterTransitioning = MutableStateFlow(false)
    private var pollJob: Job? = null

    private val filterBundleFlow = combine(
        _rawItems,
        _selectedMediaFilter,
        _sortOrder,
        _isLocalOnly,
        _selectedPlatform
    ) { raw, filter, sort, localOnly, platform ->
        FilterBundle(raw, filter, sort, localOnly, platform)
    }

    private val queryAndBundleFlow = combine(
        filterBundleFlow,
        _searchQuery
    ) { bundle, query ->
        QueryBundle(bundle, query)
    }

    private val loadingFlow = combine(
        _isLoading,
        _isFilterTransitioning
    ) { loading, transitioning ->
        loading || transitioning
    }

    private val appStateFlow = combine(
        loadingFlow,
        _errorMessage,
        _activeViewerIndex,
        _activeProfile,
        _profiles
    ) { loading, error, viewerIdx, activeProf, profileList ->
        StateBundle(loading, error, viewerIdx, activeProf, profileList)
    }

    val uiState: StateFlow<GalleryUiState> = combine(
        queryAndBundleFlow,
        appStateFlow
    ) { queryBundle, stateBundle ->
        val bundle = queryBundle.bundle
        val query = queryBundle.query.trim().lowercase()

        // Only include items that are successfully archived (strictly exclude failed items)
        val raw = bundle.raw.filter { it.status.equals("archived", ignoreCase = true) }
        val mediaFilter = bundle.filter
        val sortOrder = bundle.sortOrder
        val localOnly = bundle.localOnly
        val platform = bundle.platform

        val platforms = raw.map { it.platform.lowercase() }.filter { it.isNotBlank() }.distinct().sorted()

        val filtered = raw.filter { item ->
            // Local storage filter
            val matchesLocal = !localOnly || item.hasLocalMedia

            // Format type filter (All / Photos / Videos)
            val matchesKind = when (mediaFilter) {
                MediaFilter.ALL -> true
                MediaFilter.PHOTOS -> !item.isVideo
                MediaFilter.VIDEOS -> item.isVideo
            }

            // Platform source filter
            val matchesPlatform = platform == null || item.platform.equals(platform, ignoreCase = true)

            // Search query filter
            val matchesQuery = query.isBlank() ||
                    item.id.toString() == query ||
                    item.username.lowercase().contains(query) ||
                    item.platform.lowercase().contains(query) ||
                    item.displayUrl.lowercase().contains(query)

            matchesLocal && matchesKind && matchesPlatform && matchesQuery
        }.let { list ->
            when (sortOrder) {
                SortOrder.NEWEST -> list.sortedByDescending { it.id }
                SortOrder.OLDEST -> list.sortedBy { it.id }
            }
        }

        GalleryUiState(
            items = raw,
            filteredItems = filtered,
            availablePlatforms = platforms,
            selectedMediaFilter = mediaFilter,
            sortOrder = sortOrder,
            isLocalOnly = localOnly,
            selectedPlatform = platform,
            searchQuery = queryBundle.query,
            isLoading = stateBundle.loading,
            errorMessage = stateBundle.error,
            activeViewerIndex = stateBundle.viewerIdx,
            serverUrl = stateBundle.activeProf.url,
            profiles = stateBundle.profileList,
            activeProfile = stateBundle.activeProf
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GalleryUiState(
            isLoading = true,
            serverUrl = profilePrefs.getActiveProfile().url,
            profiles = profilePrefs.getProfiles(),
            activeProfile = profilePrefs.getActiveProfile()
        )
    )

    init {
        loadArchives()
    }

    private fun checkAndStartPolling() {
        val anyDownloading = _rawItems.value.any { it.downloadingLocal }
        if (anyDownloading && (pollJob == null || pollJob?.isActive == false)) {
            pollJob = viewModelScope.launch {
                while (true) {
                    delay(2000)
                    repository.fetchArchives().onSuccess { list ->
                        _rawItems.value = list.sortedByDescending { it.id }
                        if (!list.any { it.downloadingLocal }) {
                            return@launch
                        }
                    }
                }
            }
        }
    }

    fun loadArchives() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            repository.fetchArchives()
                .onSuccess { list ->
                    _rawItems.value = list.sortedByDescending { it.id }
                    _isLoading.value = false
                    checkAndStartPolling()
                }
                .onFailure { error ->
                    _errorMessage.value = error.localizedMessage ?: "Failed to connect to Bookmedia server"
                    _isLoading.value = false
                }
        }
    }

    fun switchProfile(profileId: String) {
        val found = _profiles.value.firstOrNull { it.id == profileId } ?: return
        profilePrefs.setActiveProfileId(found.id)
        _activeProfile.value = found
        repository.updateBaseUrl(found.url)
        loadArchives()
    }

    fun addOrUpdateProfile(name: String, url: String, profileId: String? = null) {
        val sanitizedUrl = url.trim().trimEnd('/')
        val sanitizedName = name.trim().ifBlank { "Profile" }
        if (sanitizedUrl.isBlank()) return

        val currentList = _profiles.value.toMutableList()
        val targetId = profileId ?: UUID.randomUUID().toString()

        val existingIndex = currentList.indexOfFirst { it.id == targetId }
        val updatedProfile = ServerProfile(id = targetId, name = sanitizedName, url = sanitizedUrl)

        if (existingIndex >= 0) {
            currentList[existingIndex] = updatedProfile
        } else {
            currentList.add(updatedProfile)
        }

        profilePrefs.saveProfiles(currentList)
        _profiles.value = currentList

        switchProfile(targetId)
    }

    fun deleteProfile(profileId: String) {
        if (_profiles.value.size <= 1) return
        val updated = _profiles.value.filterNot { it.id == profileId }
        profilePrefs.saveProfiles(updated)
        _profiles.value = updated

        if (_activeProfile.value.id == profileId) {
            switchProfile(updated.first().id)
        }
    }

    private fun triggerFilterLoading() {
        viewModelScope.launch {
            _isFilterTransitioning.value = true
            delay(120)
            _isFilterTransitioning.value = false
        }
    }

    fun setMediaFilter(filter: MediaFilter) {
        if (_selectedMediaFilter.value != filter) {
            triggerFilterLoading()
            _selectedMediaFilter.value = filter
        }
    }

    fun toggleSortOrder() {
        triggerFilterLoading()
        _sortOrder.value = if (_sortOrder.value == SortOrder.NEWEST) SortOrder.OLDEST else SortOrder.NEWEST
    }

    fun setSortOrder(order: SortOrder) {
        if (_sortOrder.value != order) {
            triggerFilterLoading()
            _sortOrder.value = order
        }
    }

    fun toggleLocalOnly() {
        triggerFilterLoading()
        _isLocalOnly.value = !_isLocalOnly.value
    }

    fun setPlatform(platform: String?) {
        val newPlatform = if (_selectedPlatform.value == platform) null else platform
        if (_selectedPlatform.value != newPlatform) {
            triggerFilterLoading()
            _selectedPlatform.value = newPlatform
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun openViewer(index: Int) {
        _activeViewerIndex.value = index
    }

    fun closeViewer() {
        _activeViewerIndex.value = null
    }

    fun requestLocalDownload(rowId: Long) {
        viewModelScope.launch {
            _rawItems.value = _rawItems.value.map {
                if (it.id == rowId) it.copy(downloadingLocal = true) else it
            }
            repository.triggerLocalDownload(rowId)
            delay(1000)
            repository.fetchArchives().onSuccess {
                _rawItems.value = it
                checkAndStartPolling()
            }
        }
    }

    fun handleSharedText(sharedText: String, onResult: (String) -> Unit) {
        val urlRegex = Regex("https?://[\\w\\d:#@%/;$()~_?\\+-=\\\\.&]+")
        val match = urlRegex.find(sharedText)
        val extractedUrl = match?.value ?: sharedText.trim()
        if (extractedUrl.isBlank()) return

        viewModelScope.launch {
            repository.submitUrl(extractedUrl)
                .onSuccess {
                    onResult("Queued for download & archive")
                    delay(1500)
                    loadArchives()
                }
                .onFailure {
                    onResult("Submit failed: ${it.localizedMessage ?: "Network error"}")
                }
        }
    }

    fun deleteLocalMedia(rowId: Long) {
        viewModelScope.launch {
            repository.deleteLocalMedia(rowId)
            delay(500)
            loadArchives()
        }
    }

    private data class QueryBundle(
        val bundle: FilterBundle,
        val query: String
    )

    private data class FilterBundle(
        val raw: List<ArchiveItem>,
        val filter: MediaFilter,
        val sortOrder: SortOrder,
        val localOnly: Boolean,
        val platform: String?
    )

    private data class StateBundle(
        val loading: Boolean,
        val error: String?,
        val viewerIdx: Int?,
        val activeProf: ServerProfile,
        val profileList: List<ServerProfile>
    )
}
