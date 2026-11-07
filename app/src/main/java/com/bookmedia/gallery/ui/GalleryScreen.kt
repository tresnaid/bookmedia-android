package com.bookmedia.gallery.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.SeekParameters
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.bookmedia.gallery.BuildConfig
import com.bookmedia.gallery.data.MediaCacheManager
import com.bookmedia.gallery.model.AppVersionInfo
import com.bookmedia.gallery.model.ArchiveItem
import com.bookmedia.gallery.model.CarouselMediaEntry
import com.bookmedia.gallery.model.MediaFilter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun UpdateBanner(
    updateState: UpdateState,
    onUpdate: () -> Unit,
    onDismiss: () -> Unit
) {
    when (updateState) {
        is UpdateState.Available -> {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(4.dp),
                color = SurfaceElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = "Update Available",
                            tint = Accent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "New Version: v${updateState.version.versionName}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = TextPrimary
                            )
                            if (updateState.version.changelog.isNotBlank()) {
                                Text(
                                    text = updateState.version.changelog,
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(2.dp),
                            color = Accent,
                            modifier = Modifier.clickable(onClick = onUpdate)
                        ) {
                            Text(
                                text = "Update Now",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
        is UpdateState.Downloading -> {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(4.dp),
                color = SurfaceElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Downloading Update...",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "${(updateState.progress * 100).toInt()}%",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Accent
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { updateState.progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = Accent,
                        trackColor = Color(0xFFDFD7CA)
                    )
                }
            }
        }
        is UpdateState.ReadyToInstall -> {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(4.dp),
                color = SurfaceElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E7D32))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Update downloaded. Ready to install!",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2E7D32)
                    )
                    Surface(
                        shape = RoundedCornerShape(2.dp),
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.clickable(onClick = onUpdate)
                    ) {
                        Text(
                            text = "Install",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
        else -> {}
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(viewModel: GalleryViewModel) {
    val state by viewModel.uiState.collectAsState()
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showBrowseOptions by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()

    val context = LocalContext.current

    BackHandler(enabled = state.activeViewerIndex != null || showSettingsDialog || showBrowseOptions || showSearch) {
        if (state.activeViewerIndex != null) {
            viewModel.closeViewer()
        } else if (showSettingsDialog) {
            showSettingsDialog = false
        } else if (showBrowseOptions) {
            showBrowseOptions = false
        } else if (showSearch) {
            showSearch = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(top = statusBarPadding.calculateTopPadding())
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            GalleryTopBar(
                isLocalOnly = state.isLocalOnly,
                onToggleLocal = viewModel::toggleLocalOnly,
                isFavoritesOnly = state.selectedMediaFilter == MediaFilter.FAVORITES,
                onToggleFavoritesOnly = viewModel::toggleFavoritesOnly,
                sortOrder = state.sortOrder,
                onToggleSort = viewModel::toggleSortOrder,
                onOpenSearch = { showSearch = !showSearch },
                onOpenBrowseOptions = { showBrowseOptions = true }
            )

            UpdateBanner(
                updateState = state.updateState,
                onUpdate = { viewModel.downloadAndInstallUpdate(context) },
                onDismiss = viewModel::dismissUpdateBanner
            )

            if (showSearch) {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = viewModel::setSearchQuery,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    placeholder = { Text("Search author or URL", fontSize = 13.sp, color = TextSecondary) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                    trailingIcon = {
                        IconButton(onClick = {
                            viewModel.setSearchQuery("")
                            showSearch = false
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Close search", tint = TextPrimary)
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceLight,
                        unfocusedContainerColor = SurfaceLight,
                        focusedBorderColor = BorderSubtle,
                        unfocusedBorderColor = BorderSubtle,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            }

            // High Performance Pull-to-Refresh Grid
            val pullRefreshState = rememberPullToRefreshState()
            PullToRefreshBox(
                isRefreshing = state.isLoading && state.items.isNotEmpty(),
                onRefresh = viewModel::loadArchives,
                state = pullRefreshState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    when {
                        state.isLoading && state.items.isEmpty() -> {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center),
                                color = Accent
                            )
                        }
                        state.errorMessage != null && state.items.isEmpty() -> {
                            Column(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Unable to connect",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = state.errorMessage ?: "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                TextButton(onClick = viewModel::loadArchives) {
                                    Text("Retry Connection", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                        state.filteredItems.isEmpty() -> {
                            Text(
                                text = if (state.isLocalOnly) "No locally stored media found" else "No matching media found",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                        else -> {
                            MediaGrid(
                                items = state.filteredItems,
                                baseUrl = state.serverUrl,
                                gridAspect = state.gridAspect,
                                cacheManager = viewModel.mediaCacheManager,
                                isFavorite = viewModel::isFavorite,
                                onItemClick = { index -> viewModel.openViewer(index) }
                            )
                        }
                    }

                    if (state.isLoading && state.items.isNotEmpty()) {
                        Surface(
                            modifier = Modifier.align(Alignment.Center),
                            shape = CircleShape,
                            color = SurfaceLight.copy(alpha = 0.95f),
                            shadowElevation = 8.dp
                        ) {
                            Box(modifier = Modifier.padding(14.dp)) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(28.dp),
                                    strokeWidth = 2.5.dp,
                                    color = Accent
                                )
                            }
                        }
                    }
                }
            }
        }

        // Fullscreen Swipe Viewer Overlay
        state.activeViewerIndex?.let { initialIndex ->
            MediaViewerOverlay(
                items = state.filteredItems,
                initialIndex = initialIndex,
                baseUrl = state.serverUrl,
                cacheManager = viewModel.mediaCacheManager,
                isFavorite = viewModel::isFavorite,
                onToggleFavorite = viewModel::toggleFavorite,
                onRequestLocalDownload = viewModel::requestLocalDownload,
                onDeleteLocalMedia = viewModel::deleteLocalMedia,
                onClose = viewModel::closeViewer
            )
        }

        if (showSettingsDialog) {
            ServerProfilesScreen(
                profiles = state.profiles,
                activeProfile = state.activeProfile,
                onSelectProfile = {
                    viewModel.switchProfile(it.id)
                    showSettingsDialog = false
                },
                onAddOrUpdateProfile = { name, url, id ->
                    viewModel.addOrUpdateProfile(name, url, id)
                },
                onDeleteProfile = {
                    viewModel.deleteProfile(it.id)
                },
                onDismiss = { showSettingsDialog = false }
            )
        }

        if (showBrowseOptions) {
            BrowseOptionsSheet(
                selectedMediaFilter = state.selectedMediaFilter,
                availablePlatforms = state.availablePlatforms,
                selectedPlatform = state.selectedPlatform,
                isLocalOnly = state.isLocalOnly,
                sortOrder = state.sortOrder,
                gridAspect = state.gridAspect,
                autoCacheFavorites = state.autoCacheFavorites,
                cacheSizeMb = state.cacheSizeMb,
                updateState = state.updateState,
                onToggleAutoCache = viewModel::toggleAutoCacheFavorites,
                onClearCache = viewModel::clearMediaCache,
                onCheckForUpdates = { viewModel.checkForUpdates(silent = false) },
                onApply = { filter, platform, localOnly, order, aspect ->
                    viewModel.setMediaFilter(filter)
                    viewModel.setPlatform(platform)
                    viewModel.setLocalOnly(localOnly)
                    viewModel.setSortOrder(order)
                    viewModel.setGridAspect(aspect)
                },
                onOpenServerProfiles = {
                    showBrowseOptions = false
                    showSettingsDialog = true
                },
                onDismiss = { showBrowseOptions = false }
            )
        }
    }
}

@Composable
fun GalleryTopBar(
    isLocalOnly: Boolean,
    onToggleLocal: () -> Unit,
    isFavoritesOnly: Boolean,
    onToggleFavoritesOnly: () -> Unit,
    sortOrder: SortOrder,
    onToggleSort: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenBrowseOptions: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(BackgroundLight)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "archie",
            fontSize = 22.sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            IconButton(onClick = onOpenSearch, modifier = Modifier.size(44.dp)) {
                Icon(Icons.Default.Search, contentDescription = "Search media", tint = TextPrimary)
            }
            IconButton(onClick = onOpenBrowseOptions, modifier = Modifier.size(44.dp)) {
                Icon(Icons.Default.Tune, contentDescription = "Browse options", tint = TextPrimary)
            }
            IconButton(onClick = onToggleSort, modifier = Modifier.size(44.dp)) {
                Icon(
                    Icons.Default.SwapVert,
                    contentDescription = "Sort: ${sortOrder.label}",
                    tint = if (sortOrder == SortOrder.OLDEST) Accent else TextPrimary
                )
            }
            IconButton(
                onClick = onToggleFavoritesOnly,
                modifier = Modifier
                    .size(44.dp)
                    .background(if (isFavoritesOnly) Color(0xFFF3E2E2) else Color.Transparent, RoundedCornerShape(10.dp))
            ) {
                Icon(
                    imageVector = if (isFavoritesOnly) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorites",
                    tint = if (isFavoritesOnly) Accent else TextPrimary
                )
            }
            IconButton(
                onClick = onToggleLocal,
                modifier = Modifier
                    .size(44.dp)
                    .background(if (isLocalOnly) Color(0xFFD8EADF) else Color.Transparent, RoundedCornerShape(10.dp))
            ) {
                Icon(
                    Icons.Default.DownloadDone,
                    contentDescription = if (isLocalOnly) "Showing media saved to the server" else "Show media saved to the server",
                    tint = if (isLocalOnly) Color(0xFF275A3C) else TextPrimary
                )
            }
        }
    }
}

@Composable
fun BrowseOptionsSheet(
    selectedMediaFilter: MediaFilter,
    availablePlatforms: List<String>,
    selectedPlatform: String?,
    isLocalOnly: Boolean,
    sortOrder: SortOrder,
    gridAspect: GridAspect,
    autoCacheFavorites: Boolean,
    cacheSizeMb: Float,
    updateState: UpdateState,
    onToggleAutoCache: () -> Unit,
    onClearCache: () -> Unit,
    onCheckForUpdates: () -> Unit,
    onApply: (MediaFilter, String?, Boolean, SortOrder, GridAspect) -> Unit,
    onOpenServerProfiles: () -> Unit,
    onDismiss: () -> Unit
) {
    var draftMediaFilter by remember(selectedMediaFilter) { mutableStateOf(selectedMediaFilter) }
    var draftPlatform by remember(selectedPlatform) { mutableStateOf(selectedPlatform) }
    var draftLocalOnly by remember(isLocalOnly) { mutableStateOf(isLocalOnly) }
    var draftSortOrder by remember(sortOrder) { mutableStateOf(sortOrder) }
    var draftGridAspect by remember(gridAspect) { mutableStateOf(gridAspect) }
    val navigationBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2A201C).copy(alpha = 0.55f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(BackgroundLight, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp + navigationBarPadding)
                .clickable(enabled = false) {},
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Browse options",
                    fontSize = 20.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(44.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close options", tint = TextPrimary)
                }
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                MediaFilter.entries.forEach { filter ->
                    val selected = filter == draftMediaFilter
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .background(if (selected) Accent else Color.Transparent)
                            .then(
                                if (selected) Modifier else Modifier.background(SurfaceLight)
                            )
                            .clickable { draftMediaFilter = filter },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            filter.label,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (selected) Color.White else TextPrimary
                        )
                    }
                }
            }

            Text("Platform", fontSize = 12.sp, color = TextSecondary)
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = draftPlatform == null,
                        onClick = { draftPlatform = null },
                        label = { Text("All") },
                        colors = chipColors()
                    )
                }
                items(availablePlatforms) { platform ->
                    FilterChip(
                        selected = draftPlatform.equals(platform, ignoreCase = true),
                        onClick = { draftPlatform = platform },
                        label = { Text(platform.uppercase()) },
                        colors = chipColors()
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clickable { draftLocalOnly = !draftLocalOnly },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Only media saved to the server", fontSize = 13.sp, color = TextPrimary)
                Box(
                    modifier = Modifier
                        .size(width = 44.dp, height = 24.dp)
                        .background(if (draftLocalOnly) Color(0xFFD8EADF) else BorderSubtle, CircleShape)
                        .padding(start = if (draftLocalOnly) 20.dp else 0.dp),
                    contentAlignment = if (draftLocalOnly) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(if (draftLocalOnly) Color(0xFF2F6B45) else SurfaceLight, CircleShape)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .background(SurfaceElevated)
                    .clickable {
                        draftSortOrder = if (draftSortOrder == SortOrder.NEWEST) SortOrder.OLDEST else SortOrder.NEWEST
                    }
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Sort", fontSize = 12.sp, color = TextSecondary)
                Text(draftSortOrder.label, fontSize = 13.sp, color = TextPrimary)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                GridAspect.entries.forEach { aspect ->
                    val selected = aspect == draftGridAspect
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .background(if (selected) Accent else SurfaceElevated)
                            .clickable { draftGridAspect = aspect },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Grid ${aspect.label}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (selected) Color.White else TextPrimary
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .background(SurfaceElevated)
                    .clickable { onToggleAutoCache() }
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Auto-cache favorites", fontSize = 13.sp, color = TextPrimary)
                    Text("Save starred media to phone", fontSize = 10.sp, color = TextSecondary)
                }
                Box(
                    modifier = Modifier
                        .size(width = 44.dp, height = 24.dp)
                        .background(if (autoCacheFavorites) Color(0xFFD8EADF) else BorderSubtle, CircleShape)
                        .padding(start = if (autoCacheFavorites) 20.dp else 0.dp),
                    contentAlignment = if (autoCacheFavorites) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(if (autoCacheFavorites) Color(0xFF2F6B45) else SurfaceLight, CircleShape)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(SurfaceElevated)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Offline Cache: ${"%.1f".format(cacheSizeMb)} MB",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                TextButton(onClick = onClearCache) {
                    Text("Clear Cache", color = Accent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(SurfaceElevated)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "archie v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                TextButton(onClick = onCheckForUpdates) {
                    Text(
                        text = when (updateState) {
                            is UpdateState.Checking -> "Checking..."
                            is UpdateState.Available -> "Update Ready!"
                            is UpdateState.UpToDate -> "Up to date"
                            else -> "Check Update"
                        },
                        color = Accent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = onOpenServerProfiles,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Text("Server profiles", color = Accent, fontSize = 13.sp)
                }
                TextButton(
                    onClick = {
                        onApply(draftMediaFilter, draftPlatform, draftLocalOnly, draftSortOrder, draftGridAspect)
                        onDismiss()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .background(Accent)
                ) {
                    Text("Apply", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun chipColors() = FilterChipDefaults.filterChipColors(
    containerColor = SurfaceElevated,
    labelColor = TextSecondary,
    selectedContainerColor = Accent,
    selectedLabelColor = Color.White
)

@Composable
fun MediaGrid(
    items: List<ArchiveItem>,
    baseUrl: String,
    gridAspect: GridAspect,
    cacheManager: MediaCacheManager,
    isFavorite: (Long) -> Boolean,
    onItemClick: (Int) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(0.dp),
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        itemsIndexed(items, key = { _, item -> item.id }) { index, item ->
            MediaGridThumbnail(
                item = item,
                baseUrl = baseUrl,
                aspectRatio = gridAspect.ratio,
                cacheManager = cacheManager,
                isFavorite = isFavorite(item.id),
                onClick = { onItemClick(index) }
            )
        }
    }
}

@Composable
fun MediaGridThumbnail(
    item: ArchiveItem,
    baseUrl: String,
    aspectRatio: Float,
    cacheManager: MediaCacheManager,
    isFavorite: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val imageSource = remember(item.id, item.thumbnailUrl, item.localUrl, baseUrl) {
        item.resolveThumbnailUrl(baseUrl, cacheManager)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(aspectRatio)
            .background(SurfaceElevated)
            .clickable(onClick = onClick)
    ) {
        var imageFailed by remember(imageSource) { mutableStateOf(imageSource.isBlank()) }

        if (imageSource.isNotBlank() && !imageFailed) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageSource)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                onError = { imageFailed = true }
            )
        }

        if (imageFailed) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SurfaceElevated),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (item.isVideo) Icons.Default.Videocam else Icons.Default.BrokenImage,
                    contentDescription = "No Media",
                    tint = TextSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        if (isFavorite) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(4.dp)
                    .size(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Favorite",
                    tint = Accent,
                    modifier = Modifier.size(13.dp)
                )
            }
        }

        if (item.downloadingLocal) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(13.dp),
                    strokeWidth = 1.5.dp,
                    color = Accent
                )
            }
        } else if (item.localError.isNotBlank()) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = "Save failed",
                    tint = Color(0xFFB3263E),
                    modifier = Modifier.size(15.dp)
                )
            }
        } else if (item.hasLocalMedia || cacheManager.isItemCached(item.id)) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DownloadDone,
                    contentDescription = "Saved",
                    tint = Color(0xFF275A3C),
                    modifier = Modifier.size(15.dp)
                )
            }
        }

        if (item.isCarousel) {
            Box(
                modifier = Modifier
                    .align(if (isFavorite) Alignment.BottomStart else Alignment.TopStart)
                    .padding(4.dp)
                    .background(Color(0xFF2A201C).copy(alpha = 0.78f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Collections,
                        contentDescription = "Carousel",
                        tint = Color.White,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${item.totalMediaCount}",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaViewerOverlay(
    items: List<ArchiveItem>,
    initialIndex: Int,
    baseUrl: String,
    cacheManager: MediaCacheManager,
    isFavorite: (Long) -> Boolean,
    onToggleFavorite: (Long) -> Unit,
    onRequestLocalDownload: (Long) -> Unit,
    onDeleteLocalMedia: (Long) -> Unit,
    onClose: () -> Unit
) {
    val postPagerState = rememberPagerState(initialPage = initialIndex, pageCount = { items.size })
    var showControls by remember { mutableStateOf(false) }
    var activePlayer by remember { mutableStateOf<ExoPlayer?>(null) }
    val currentPost = items.getOrNull(postPagerState.currentPage)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val offsetY = remember { Animatable(0f) }
    val dismissThreshold = 180f

    val draggableState = rememberDraggableState { delta ->
        scope.launch {
            offsetY.snapTo(offsetY.value + delta)
        }
    }

    val systemBarsPadding = WindowInsets.systemBars.asPaddingValues()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                val progress = (abs(offsetY.value) / 600f).coerceIn(0f, 1f)
                alpha = 1f - (progress * 0.5f)
                translationY = offsetY.value
            }
            .background(Color(0xFF2D2420))
            .draggable(
                state = draggableState,
                orientation = Orientation.Vertical,
                onDragStopped = { velocity ->
                    if (abs(offsetY.value) > dismissThreshold || abs(velocity) > 1200f) {
                        onClose()
                    } else {
                        scope.launch {
                            offsetY.animateTo(0f)
                        }
                    }
                }
            )
            .pointerInput(Unit) {
                detectTapGestures(onTap = { showControls = !showControls })
            }
    ) {
        // Post-level HorizontalPager with smooth settle detection
        val isSettled = !postPagerState.isScrollInProgress
        HorizontalPager(
            state = postPagerState,
            beyondViewportPageCount = 0,
            key = { items[it].id },
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val post = items[page]
            val isCurrentPost = postPagerState.currentPage == page
            val canPlay = isCurrentPost && isSettled

            PostMediaViewer(
                post = post,
                baseUrl = baseUrl,
                cacheManager = cacheManager,
                isActivePost = canPlay,
                onActivePlayerChanged = { player ->
                    if (canPlay) {
                        activePlayer = player
                    }
                }
            )
        }

        // Overlay Chrome
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = systemBarsPadding.calculateTopPadding(),
                        bottom = systemBarsPadding.calculateBottomPadding()
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .background(Color(0xFF2D2420).copy(alpha = 0.92f))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onClose, modifier = Modifier.size(44.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close viewer", tint = Color.White)
                    }
                    Text(
                        text = "${postPagerState.currentPage + 1} of ${items.size}",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    currentPost?.let { post ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { onToggleFavorite(post.id) },
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(
                                    imageVector = if (isFavorite(post.id)) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Favorite",
                                    tint = if (isFavorite(post.id)) Color(0xFFE25C6A) else Color.White
                                )
                            }
                            IconButton(
                                onClick = {
                                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(post.displayUrl))
                                    context.startActivity(browserIntent)
                                },
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.OpenInNew,
                                    contentDescription = "Open source",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }

                // Bottom Controls: Video Seeker (on top) + Detail Metadata Bar
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                ) {
                    currentPost?.let { item ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF201816).copy(alpha = 0.96f))
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            activePlayer?.let { player ->
                                VideoSeekerBar(
                                    exoPlayer = player,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.effectiveTitle,
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (item.text.isNotBlank() && item.text != item.effectiveTitle) {
                                        Text(
                                            text = item.text,
                                            color = Color(0xFFD8CEC7),
                                            fontSize = 12.sp,
                                            maxLines = 3,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    } else if (item.localError.isNotBlank()) {
                                        Text(
                                            text = "Save failed: ${item.localError}",
                                            color = Color(0xFFFFB4AB),
                                            fontSize = 11.sp,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                if (item.hasLocalMedia) {
                                    Box(
                                        modifier = Modifier.size(44.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.DownloadDone,
                                            contentDescription = "Saved to server",
                                            tint = Color(0xFF6FAA7F),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                } else {
                                    IconButton(
                                        onClick = { onRequestLocalDownload(item.id) },
                                        enabled = !item.downloadingLocal,
                                        modifier = Modifier.size(44.dp)
                                    ) {
                                        if (item.downloadingLocal) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(18.dp),
                                                strokeWidth = 2.dp,
                                                color = TextPrimary
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.CloudDownload,
                                                contentDescription = "Save to server",
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Handles single or multiple media items within a single post
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PostMediaViewer(
    post: ArchiveItem,
    baseUrl: String,
    cacheManager: MediaCacheManager,
    isActivePost: Boolean,
    onActivePlayerChanged: (ExoPlayer?) -> Unit = {}
) {
    val mediaEntries = remember(post, baseUrl, cacheManager) { post.resolveMediaList(baseUrl, cacheManager) }

    if (mediaEntries.size <= 1) {
        val entry = mediaEntries.firstOrNull() ?: CarouselMediaEntry(1, "", false, "")
        if (entry.isVideo) {
            VideoPlayerPage(
                videoUrl = entry.url,
                thumbnailUrl = entry.thumbnailUrl.ifBlank { post.resolveThumbnailUrl(baseUrl, cacheManager) },
                isActive = isActivePost,
                onPlayerReady = onActivePlayerChanged
            )
        } else {
            DisposableEffect(Unit) {
                onActivePlayerChanged(null)
                onDispose { }
            }
            PhotoViewerPage(
                imageUrl = entry.url,
                thumbnailUrl = entry.thumbnailUrl.ifBlank { post.resolveThumbnailUrl(baseUrl, cacheManager) }
            )
        }
    } else {
        // In-post Carousel Pager
        val innerPagerState = rememberPagerState(initialPage = 0, pageCount = { mediaEntries.size })
        val isInnerSettled = !innerPagerState.isScrollInProgress

        Box(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(
                state = innerPagerState,
                beyondViewportPageCount = 0,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val entry = mediaEntries[page]
                val isEntryActive = isActivePost && innerPagerState.currentPage == page && isInnerSettled

                if (entry.isVideo) {
                    VideoPlayerPage(
                        videoUrl = entry.url,
                        thumbnailUrl = entry.thumbnailUrl.ifBlank { post.resolveThumbnailUrl(baseUrl, cacheManager) },
                        isActive = isEntryActive,
                        onPlayerReady = { player ->
                            if (isEntryActive) {
                                onActivePlayerChanged(player)
                            }
                        }
                    )
                } else {
                    if (isEntryActive) {
                        DisposableEffect(Unit) {
                            onActivePlayerChanged(null)
                            onDispose { }
                        }
                    }
                    PhotoViewerPage(
                        imageUrl = entry.url,
                        thumbnailUrl = entry.thumbnailUrl.ifBlank { post.resolveThumbnailUrl(baseUrl, cacheManager) }
                    )
                }
            }

            // Carousel Dot Indicator
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 85.dp)
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(mediaEntries.size) { index ->
                    val isCurrent = innerPagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .size(if (isCurrent) 6.dp else 5.dp)
                            .background(
                                color = if (isCurrent) Color.White else Color.White.copy(alpha = 0.4f),
                                shape = CircleShape
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun PhotoViewerPage(
    imageUrl: String,
    thumbnailUrl: String = ""
) {
    val context = LocalContext.current
    var isFullLoaded by remember(imageUrl) { mutableStateOf(false) }
    var isFullFailed by remember(imageUrl) { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // 1. Instant background thumbnail layer (displays on frame 1 from memory/disk cache)
        if (thumbnailUrl.isNotBlank() && !isFullLoaded) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(thumbnailUrl)
                    .crossfade(false)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }

        // 2. High-resolution / Full HD foreground layer
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(imageUrl)
                .crossfade(250)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(),
            onSuccess = {
                isFullLoaded = true
                isFullFailed = false
            },
            onError = {
                isFullFailed = true
            }
        )

        // 3. Subtle non-blocking spinner if high-res is still streaming
        if (!isFullLoaded && !isFullFailed) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 80.dp, end = 20.dp)
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CircularProgressIndicator(
                        color = Color.White.copy(alpha = 0.85f),
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(13.dp)
                    )
                    Text("HD", color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // 4. If high-res failed completely
        if (isFullFailed) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.BrokenImage, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(36.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Failed to load full photo", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun VideoPlayerPage(
    videoUrl: String,
    thumbnailUrl: String = "",
    isActive: Boolean,
    onPlayerReady: (ExoPlayer?) -> Unit = {}
) {
    val context = LocalContext.current

    // If video is not downloaded to local yet or source is unavailable, ALWAYS render the thumbnail cover
    if (videoUrl.isBlank()) {
        DisposableEffect(Unit) {
            onPlayerReady(null)
            onDispose { }
        }
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (thumbnailUrl.isNotBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(thumbnailUrl)
                        .crossfade(false)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    text = "Media not downloaded yet",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 13.sp
                )
            }
        }
        return
    }

    // When swiping and page is not yet active, show thumbnail preview instead of black screen
    if (!isActive) {
        DisposableEffect(Unit) {
            onPlayerReady(null)
            onDispose { }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            if (thumbnailUrl.isNotBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(thumbnailUrl)
                        .crossfade(false)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                CircularProgressIndicator(color = Color.White.copy(alpha = 0.4f), modifier = Modifier.size(24.dp))
            }
        }
        return
    }

    val exoPlayer = remember(videoUrl) {
        ExoPlayer.Builder(context).build().apply {
            setSeekParameters(SeekParameters.CLOSEST_SYNC)
            setMediaItem(MediaItem.fromUri(Uri.parse(videoUrl)))
            prepare()
            playWhenReady = true
        }
    }

    var isFirstFrameRendered by remember(videoUrl) { mutableStateOf(false) }

    DisposableEffect(exoPlayer) {
        val listener = object : androidx.media3.common.Player.Listener {
            override fun onRenderedFirstFrame() {
                isFirstFrameRendered = true
            }
        }
        exoPlayer.addListener(listener)
        onPlayerReady(exoPlayer)
        onDispose {
            exoPlayer.removeListener(listener)
            onPlayerReady(null)
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
            exoPlayer.release()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Seamless cover: Keep thumbnail displayed on top until the exact first video frame renders
        if (!isFirstFrameRendered && thumbnailUrl.isNotBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(thumbnailUrl)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun VideoSeekerBar(
    exoPlayer: ExoPlayer,
    modifier: Modifier = Modifier
) {
    var isPlaying by remember { mutableStateOf(exoPlayer.isPlaying) }
    var currentPositionMs by remember { mutableStateOf(0L) }
    var durationMs by remember { mutableStateOf(0L) }
    var isDraggingSlider by remember { mutableStateOf(false) }
    var sliderPosition by remember { mutableStateOf(0f) }

    DisposableEffect(exoPlayer) {
        val listener = object : androidx.media3.common.Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == androidx.media3.common.Player.STATE_READY) {
                    durationMs = exoPlayer.duration.coerceAtLeast(0L)
                }
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
        }
    }

    LaunchedEffect(exoPlayer, isDraggingSlider) {
        while (true) {
            if (!isDraggingSlider && exoPlayer.playbackState == androidx.media3.common.Player.STATE_READY) {
                currentPositionMs = exoPlayer.currentPosition.coerceAtLeast(0L)
                durationMs = exoPlayer.duration.coerceAtLeast(0L)
            }
            delay(250)
        }
    }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
                IconButton(
                    onClick = {
                        if (exoPlayer.isPlaying) {
                            exoPlayer.pause()
                        } else {
                            exoPlayer.play()
                        }
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Text(
                    text = formatDuration(currentPositionMs),
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )

                Slider(
                    value = if (isDraggingSlider) sliderPosition else {
                        if (durationMs > 0) (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
                    },
                    onValueChange = { newPos ->
                        isDraggingSlider = true
                        sliderPosition = newPos
                    },
                    onValueChangeFinished = {
                        isDraggingSlider = false
                        val targetMs = (sliderPosition * durationMs).toLong()
                        val resumeAfterSeek = exoPlayer.playWhenReady
                        currentPositionMs = targetMs
                        exoPlayer.seekTo(targetMs)
                        exoPlayer.playWhenReady = resumeAfterSeek
                        if (resumeAfterSeek) exoPlayer.play()
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.White,
                        inactiveTrackColor = Color.White.copy(alpha = 0.25f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp)
                        .padding(horizontal = 6.dp)
                )

                Text(
                    text = formatDuration(durationMs),
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
        }
    }
}

fun formatDuration(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}

@Composable
fun ServerProfilesScreen(
    profiles: List<com.bookmedia.gallery.data.ServerProfile>,
    activeProfile: com.bookmedia.gallery.data.ServerProfile?,
    onSelectProfile: (com.bookmedia.gallery.data.ServerProfile) -> Unit,
    onAddOrUpdateProfile: (name: String, url: String, id: String?) -> Unit,
    onDeleteProfile: (com.bookmedia.gallery.data.ServerProfile) -> Unit,
    onDismiss: () -> Unit
) {
    var isAdding by remember { mutableStateOf(false) }
    var profileName by remember { mutableStateOf("") }
    var profileUrl by remember { mutableStateOf("http://192.168.18.11:8080") }

    Surface(modifier = Modifier.fillMaxSize(), color = BackgroundLight) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .padding(start = 12.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onDismiss, modifier = Modifier.height(44.dp)) {
                        Text("Back", color = Accent, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Server profiles",
                        fontSize = 22.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }
                TextButton(
                    onClick = { isAdding = !isAdding },
                    modifier = Modifier
                        .height(44.dp)
                        .background(Accent)
                ) {
                    Text(if (isAdding) "Cancel" else "Add", color = Color.White, fontSize = 13.sp)
                }
            }

            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Text(
                        "Switch between local, private-network, or public Archie hosts.",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                }

                items(profiles, key = { it.id }) { profile ->
                    val isActive = profile.id == activeProfile?.id
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectProfile(profile) },
                        color = if (isActive) SurfaceElevated else BackgroundLight,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isActive) Accent else BorderSubtle
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(profile.name, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                                    Text(
                                        if (isActive) "Active" else "Use profile",
                                        fontSize = 12.sp,
                                        color = if (isActive) Color(0xFF2F6B45) else Accent
                                    )
                                }
                                Text(
                                    profile.url,
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (profiles.size > 1 && !isActive) {
                                IconButton(onClick = { onDeleteProfile(profile) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete ${profile.name}", tint = TextSecondary)
                                }
                            }
                        }
                    }
                }

                if (isAdding) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SurfaceElevated)
                                .padding(horizontal = 16.dp, vertical = 18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "New profile",
                                fontSize = 18.sp,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            OutlinedTextField(
                                value = profileName,
                                onValueChange = { profileName = it },
                                label = { Text("Profile name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedBorderColor = BorderSubtle,
                                    unfocusedBorderColor = BorderSubtle
                                )
                            )
                            OutlinedTextField(
                                value = profileUrl,
                                onValueChange = { profileUrl = it },
                                label = { Text("Server URL") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedBorderColor = BorderSubtle,
                                    unfocusedBorderColor = BorderSubtle
                                )
                            )
                            TextButton(
                                onClick = {
                                    if (profileUrl.isNotBlank()) {
                                        onAddOrUpdateProfile(profileName, profileUrl, null)
                                        profileName = ""
                                        isAdding = false
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .background(Accent)
                            ) {
                                Text("Save profile", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ServerProfilesDialog(
    profiles: List<com.bookmedia.gallery.data.ServerProfile>,
    activeProfile: com.bookmedia.gallery.data.ServerProfile?,
    onSelectProfile: (com.bookmedia.gallery.data.ServerProfile) -> Unit,
    onAddOrUpdateProfile: (name: String, url: String, id: String?) -> Unit,
    onDeleteProfile: (com.bookmedia.gallery.data.ServerProfile) -> Unit,
    onDismiss: () -> Unit
) {
    var isAdding by remember { mutableStateOf(false) }
    var profileName by remember { mutableStateOf("") }
    var profileUrl by remember { mutableStateOf("http://192.168.18.11:8080") }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(onClick = onDismiss),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceLight)
                    .padding(22.dp)
                    .clickable(enabled = false) {}
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isAdding) "New Server Profile" else "Server Profiles",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    if (!isAdding) {
                        IconButton(onClick = { isAdding = true }) {
                            Icon(androidx.compose.material.icons.Icons.Default.Add, contentDescription = "Add Profile", tint = TextPrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (isAdding) {
                    OutlinedTextField(
                        value = profileName,
                        onValueChange = { profileName = it },
                        singleLine = true,
                        placeholder = { Text("e.g. Home Wi-Fi, Office, VPS", color = TextSecondary, fontSize = 13.sp) },
                        label = { Text("Profile Name") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = profileUrl,
                        onValueChange = { profileUrl = it },
                        singleLine = true,
                        placeholder = { Text("http://192.168.18.11:8080", color = TextSecondary, fontSize = 13.sp) },
                        label = { Text("Server URL") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { isAdding = false }) {
                            Text("Cancel", color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(
                            onClick = {
                                if (profileUrl.isNotBlank()) {
                                    onAddOrUpdateProfile(profileName, profileUrl, null)
                                    isAdding = false
                                }
                            }
                        ) {
                            Text("Save Profile", color = TextPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    androidx.compose.foundation.lazy.LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(profiles) { profile ->
                            val isActive = profile.id == activeProfile?.id
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectProfile(profile) },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isActive) SurfaceElevated else Color.Transparent,
                                border = androidx.compose.foundation.BorderStroke(
                                    width = 1.dp,
                                    color = if (isActive) Accent else BorderSubtle
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = profile.name,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 14.sp,
                                                color = TextPrimary
                                            )
                                            if (isActive) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Icon(
                                                    imageVector = androidx.compose.material.icons.Icons.Default.Check,
                                                    contentDescription = "Active",
                                                    tint = TextPrimary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = profile.url,
                                            fontSize = 12.sp,
                                            color = TextSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    if (profiles.size > 1) {
                                        IconButton(onClick = { onDeleteProfile(profile) }) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = TextSecondary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Close", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}
