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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Videocam
import coil.compose.SubcomposeAsyncImage
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.bookmedia.gallery.data.ServerProfile
import com.bookmedia.gallery.model.ArchiveItem
import com.bookmedia.gallery.model.MediaFilter
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(viewModel: GalleryViewModel) {
    val state by viewModel.uiState.collectAsState()
    var showSettingsDialog by remember { mutableStateOf(false) }

    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()

    BackHandler(enabled = state.activeViewerIndex != null || showSettingsDialog) {
        if (state.activeViewerIndex != null) {
            viewModel.closeViewer()
        } else if (showSettingsDialog) {
            showSettingsDialog = false
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
                searchQuery = state.searchQuery,
                onSearchChange = viewModel::setSearchQuery,
                onRefresh = viewModel::loadArchives,
                onOpenSettings = { showSettingsDialog = true },
                activeProfileName = state.activeProfile?.name
            )

            GalleryFilterSection(
                selectedFilter = state.selectedMediaFilter,
                onSelectFilter = viewModel::setMediaFilter,
                platforms = state.availablePlatforms,
                selectedPlatform = state.selectedPlatform,
                onSelectPlatform = viewModel::setPlatform
            )

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
                                text = "No matching media found",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                        else -> {
                            MediaGrid(
                                items = state.filteredItems,
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
                onRequestLocalDownload = viewModel::requestLocalDownload,
                onDeleteLocalMedia = viewModel::deleteLocalMedia,
                onClose = viewModel::closeViewer
            )
        }

        if (showSettingsDialog) {
            ServerProfilesDialog(
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
    }
}

@Composable
fun GalleryTopBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onRefresh: () -> Unit,
    onOpenSettings: () -> Unit,
    activeProfileName: String?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Search author, keyword, id...", fontSize = 13.sp, color = TextSecondary) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceLight,
                unfocusedContainerColor = SurfaceElevated,
                focusedBorderColor = BorderSubtle,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(onClick = onRefresh) {
            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = TextPrimary)
        }
        IconButton(onClick = onOpenSettings) {
            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = TextPrimary)
        }
    }
}

@Composable
fun GalleryFilterSection(
    selectedFilter: MediaFilter,
    onSelectFilter: (MediaFilter) -> Unit,
    platforms: List<String>,
    selectedPlatform: String?,
    onSelectPlatform: (String?) -> Unit
) {
    // Single clean filter row (All / Photos / Videos + Platforms)
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedFilter == MediaFilter.ALL,
                onClick = { onSelectFilter(MediaFilter.ALL) },
                label = { Text("All", fontSize = 12.sp) },
                colors = chipColors()
            )
        }
        item {
            FilterChip(
                selected = selectedFilter == MediaFilter.PHOTOS,
                onClick = { onSelectFilter(MediaFilter.PHOTOS) },
                label = { Text("Photos", fontSize = 12.sp) },
                colors = chipColors()
            )
        }
        item {
            FilterChip(
                selected = selectedFilter == MediaFilter.VIDEOS,
                onClick = { onSelectFilter(MediaFilter.VIDEOS) },
                label = { Text("Videos", fontSize = 12.sp) },
                colors = chipColors()
            )
        }

        // Platform Filters
        items(platforms) { platform ->
            val isSelected = selectedPlatform.equals(platform, ignoreCase = true)
            FilterChip(
                selected = isSelected,
                onClick = { onSelectPlatform(platform) },
                label = { Text(platform.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                colors = chipColors()
            )
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
    onItemClick: (Int) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        itemsIndexed(items, key = { _, item -> item.id }) { index, item ->
            MediaGridThumbnail(
                item = item,
                onClick = { onItemClick(index) }
            )
        }
    }
}

@Composable
fun MediaGridThumbnail(
    item: ArchiveItem,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val imageSource = item.thumbnailUrl.ifBlank {
        if (!item.isVideo && item.submittedUrl.isNotBlank()) item.submittedUrl else ""
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .background(SurfaceElevated)
            .clickable(onClick = onClick)
    ) {
        if (imageSource.isBlank()) {
            // Distinct "No Thumbnail / No Media" state
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFE2E8F0)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (item.isVideo) Icons.Default.Videocam else Icons.Default.BrokenImage,
                    contentDescription = "No Media",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "NO PREVIEW",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B)
                )
            }
        } else {
            // Media with loading placeholder vs error fallback
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageSource)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(SurfaceElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                },
                error = {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFE2E8F0)),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (item.isVideo) Icons.Default.Videocam else Icons.Default.Image,
                            contentDescription = "No Preview",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "NO THUMB",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (item.isVideo) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .background(Color.Black.copy(alpha = 0.65f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
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
    onRequestLocalDownload: (Long) -> Unit,
    onDeleteLocalMedia: (Long) -> Unit,
    onClose: () -> Unit
) {
    val pagerState = rememberPagerState(initialPage = initialIndex, pageCount = { items.size })
    var showControls by remember { mutableStateOf(true) }
    val currentItem = items.getOrNull(pagerState.currentPage)
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
            }
            .offset { IntOffset(0, offsetY.value.roundToInt()) }
            .background(Color(0xFF0F172A)) // Neutral clean slate dark for immersive viewing
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
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val item = items[page]
            if (item.isVideo) {
                VideoPlayerPage(item = item, baseUrl = baseUrl)
            } else {
                PhotoViewerPage(item = item, baseUrl = baseUrl)
            }
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
                // Top Minimal Counter
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .background(Color(0xFF0F172A).copy(alpha = 0.6f))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${pagerState.currentPage + 1} / ${items.size}",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )

                    currentItem?.let {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (it.hasLocalMedia) {
                                Surface(
                                    color = Color(0xFF10B981).copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Stored Locally",
                                            tint = Color(0xFF34D399),
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "LOCAL",
                                            color = Color(0xFF34D399),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "${it.platform.uppercase()} #${it.id}",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Bottom Action & Metadata Bar (Easily Reachable with One Hand)
                currentItem?.let { item ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .background(Color(0xFF0F172A).copy(alpha = 0.85f))
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
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
                                    fontSize = 15.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (item.hasLocalMedia) {
                                    Text(
                                        text = item.localUrl,
                                        color = Color(0xFF34D399),
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                } else if (item.displayUrl.isNotBlank()) {
                                    Text(
                                        text = item.displayUrl,
                                        color = Color(0xFF94A3B8),
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Download to Local Storage / Delete Local Storage Button
                                if (item.hasLocalMedia) {
                                    IconButton(
                                        onClick = { onDeleteLocalMedia(item.id) }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Local Media",
                                            tint = Color(0xFFF87171),
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                } else {
                                    IconButton(
                                        onClick = { onRequestLocalDownload(item.id) },
                                        enabled = !item.downloadingLocal
                                    ) {
                                        if (item.downloadingLocal) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(18.dp),
                                                strokeWidth = 2.dp,
                                                color = Color.White
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.CloudDownload,
                                                contentDescription = "Save to Server Storage",
                                                tint = Color.White,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(item.displayUrl))
                                        context.startActivity(browserIntent)
                                    }
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.OpenInNew,
                                        contentDescription = "Open Source",
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                IconButton(onClick = onClose) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Close",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
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

@Composable
fun PhotoViewerPage(item: ArchiveItem, baseUrl: String) {
    val context = LocalContext.current
    val imageSource = if (item.hasLocalMedia) {
        val trimmedBase = baseUrl.trimEnd('/')
        val trimmedPath = item.localUrl.trimStart('/')
        "$trimmedBase/$trimmedPath"
    } else {
        item.submittedUrl.ifBlank { item.thumbnailUrl }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(imageSource)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun VideoPlayerPage(item: ArchiveItem, baseUrl: String) {
    val context = LocalContext.current

    // If local media file exists, stream it directly from the backend server!
    val videoSource = if (item.hasLocalMedia) {
        item.resolvePlayableMediaUrl(baseUrl)
    } else if (item.isDirectVideo) {
        item.submittedUrl.ifBlank { item.canonicalUrl }
    } else {
        ""
    }

    if (videoSource.isNotBlank()) {
        val exoPlayer = remember(videoSource) {
            ExoPlayer.Builder(context).build().apply {
                setMediaItem(MediaItem.fromUri(Uri.parse(videoSource)))
                prepare()
                playWhenReady = true
            }
        }

        DisposableEffect(Unit) {
            onDispose {
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
        }
    } else {
        val posterImage = item.thumbnailUrl.ifBlank { item.submittedUrl }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(posterImage)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )

            Surface(
                onClick = {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(item.displayUrl))
                    context.startActivity(browserIntent)
                },
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.65f),
                shadowElevation = 8.dp,
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Watch on ${item.platform.uppercase()}",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ServerProfilesDialog(
    profiles: List<ServerProfile>,
    activeProfile: ServerProfile?,
    onSelectProfile: (ServerProfile) -> Unit,
    onAddOrUpdateProfile: (name: String, url: String, id: String?) -> Unit,
    onDeleteProfile: (ServerProfile) -> Unit,
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
                            Icon(Icons.Default.Add, contentDescription = "Add Profile", tint = TextPrimary)
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
                    LazyColumn(
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
                                                    imageVector = Icons.Default.Check,
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
