package com.antigravity.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.OfflinePin
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.antigravity.app.data.model.AudioMode
import com.antigravity.app.data.model.AudioTrack
import com.antigravity.app.data.model.RepeatMode
import com.antigravity.app.data.offline.DeviceAudioCandidate
import com.antigravity.app.ui.components.AudioEqualizerIndicator
import com.antigravity.app.ui.components.GlassCard
import com.antigravity.app.ui.components.formatDurationMs
import com.antigravity.app.ui.theme.CyanAccent
import com.antigravity.app.ui.theme.CyanMuted
import com.antigravity.app.ui.theme.EmeraldAccent
import com.antigravity.app.ui.theme.EmeraldMuted
import com.antigravity.app.ui.theme.GlassBorder
import com.antigravity.app.ui.theme.GlassBorderCyan
import com.antigravity.app.ui.theme.GlassBorderEmerald
import com.antigravity.app.ui.theme.Navy900
import com.antigravity.app.ui.theme.Navy950
import com.antigravity.app.ui.theme.RoseAccent
import com.antigravity.app.ui.theme.RoseMuted
import com.antigravity.app.ui.theme.Slate850
import com.antigravity.app.ui.theme.TextMuted
import com.antigravity.app.ui.theme.TextPrimary
import com.antigravity.app.ui.theme.TextSecondary

@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel,
    bottomInsetPadding: Dp = 96.dp
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activeMode = state.playbackState.activeMode
    val isAyatMode = activeMode == AudioMode.AYAT
    val modeAccent = if (isAyatMode) EmeraldAccent else CyanAccent

    var pendingAyatImportUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            if (isAyatMode) {
                pendingAyatImportUris = uris
            } else {
                viewModel.importLocalFiles(uris, AudioMode.SONG)
            }
        }
    }

    val readAudioPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.scanDeviceAudio()
        } else {
            filePickerLauncher.launch(arrayOf("audio/*"))
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = bottomInsetPadding
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header + Dual-Mode Switcher
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Dual Audio Player", style = MaterialTheme.typography.headlineLarge)
                DualModeSwitcher(
                    activeMode = activeMode,
                    onModeSelected = viewModel::switchMode
                )
            }
        }

        // Error or Status Banner
        val bannerText = state.playbackState.errorMessage ?: state.statusBanner
        if (!bannerText.isNullOrBlank()) {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = if (state.playbackState.errorMessage != null) RoseAccent else CyanAccent
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = bannerText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            onClick = {
                                viewModel.clearPlayerError()
                                viewModel.dismissBanner()
                            }
                        ) {
                            Text("OK", color = CyanAccent)
                        }
                    }
                }
            }
        }

        // Now Playing Hero Card
        item {
            NowPlayingHeroCard(
                state = state,
                onTogglePlayPause = viewModel::togglePlayPause,
                onNext = viewModel::next,
                onPrevious = viewModel::previous,
                onSeekTo = viewModel::seekTo,
                onToggleShuffle = viewModel::toggleShuffle,
                onCycleRepeat = viewModel::cycleRepeatMode,
                onToggleArabic = viewModel::toggleArabicText,
                onToggleTranslation = viewModel::toggleTranslation
            )
        }

        // Offline Actions & Filters
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isAyatMode) "Quranic Ayats Library" else "Songs & Lo-Fi Library",
                        style = MaterialTheme.typography.titleLarge
                    )
                    FilterChip(
                        selected = state.offlineOnly,
                        onClick = viewModel::toggleOfflineOnly,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.OfflinePin,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = { Text("Offline Only") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldMuted,
                            selectedLabelColor = EmeraldAccent
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { filePickerLauncher.launch(arrayOf("audio/*")) },
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, modeAccent.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                            tint = modeAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Import Audio", color = TextPrimary, style = MaterialTheme.typography.labelMedium)
                    }

                    OutlinedButton(
                        onClick = {
                            val granted = ContextCompat.checkSelfPermission(
                                context,
                                readAudioPermission
                            ) == PackageManager.PERMISSION_GRANTED
                            if (granted) {
                                viewModel.scanDeviceAudio()
                            } else {
                                permissionLauncher.launch(readAudioPermission)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, GlassBorder)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.FolderOpen,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (state.isScanningDevice) "Scanning..." else "Scan Device",
                            color = TextPrimary,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                if (isAyatMode && state.visibleTracks.any { !it.isAvailableOffline && !it.streamUrl.isNullOrBlank() }) {
                    Button(
                        onClick = viewModel::downloadAllAyatsOffline,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldMuted,
                            contentColor = EmeraldAccent
                        )
                    ) {
                        Icon(Icons.Rounded.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Download All Ayats for Offline Playback", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        if (state.visibleTracks.isEmpty()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("No tracks in this view", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "Import local MP3/M4A files or disable the Offline Only filter.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(state.visibleTracks, key = { it.id }) { track ->
                val isCurrent = state.playbackState.currentTrack?.id == track.id
                val isDownloading = track.id in state.downloadingIds
                TrackQueueItem(
                    track = track,
                    isCurrent = isCurrent,
                    isPlaying = isCurrent && state.playbackState.isPlaying,
                    isDownloading = isDownloading,
                    onClick = { viewModel.playTrack(track) },
                    onDownloadClick = { viewModel.downloadTrackOffline(track) },
                    onDeleteClick = if (track.isUserImported) {
                        { viewModel.deleteTrack(track) }
                    } else null
                )
            }
        }
    }

    if (pendingAyatImportUris.isNotEmpty()) {
        AyatMetadataImportDialog(
            fileCount = pendingAyatImportUris.size,
            onDismiss = { pendingAyatImportUris = emptyList() },
            onConfirm = { surah, startAyat ->
                viewModel.importLocalFiles(
                    uris = pendingAyatImportUris,
                    mode = AudioMode.AYAT,
                    surahName = surah,
                    ayatNumber = startAyat
                )
                pendingAyatImportUris = emptyList()
            }
        )
    }

    if (state.scannedCandidates.isNotEmpty()) {
        DeviceAudioPickerDialog(
            candidates = state.scannedCandidates,
            targetMode = activeMode,
            onDismiss = viewModel::dismissScannedCandidates,
            onImportSelected = { selected ->
                viewModel.importScannedCandidates(selected, activeMode)
            }
        )
    }
}

@Composable
private fun DualModeSwitcher(
    activeMode: AudioMode,
    onModeSelected: (AudioMode) -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 18.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AudioMode.entries.forEach { mode ->
                val selected = mode == activeMode
                val accent = if (mode == AudioMode.AYAT) EmeraldAccent else CyanAccent
                val icon = if (mode == AudioMode.AYAT) Icons.Rounded.MenuBook else Icons.Rounded.MusicNote

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (selected) accent.copy(alpha = 0.22f) else Color.Transparent
                        )
                        .clickable { onModeSelected(mode) }
                        .padding(vertical = 11.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (selected) accent else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = mode.label,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (selected) accent else TextSecondary,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun NowPlayingHeroCard(
    state: PlayerUiState,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onToggleArabic: () -> Unit,
    onToggleTranslation: () -> Unit
) {
    val playback = state.playbackState
    val track = playback.currentTrack
    val isAyat = track?.mode == AudioMode.AYAT
    val accent = if (isAyat) EmeraldAccent else CyanAccent
    val border = if (isAyat) GlassBorderEmerald else GlassBorderCyan

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 26.dp,
        borderColor = border,
        elevated = true
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Artwork Orb + Status
            Box(
                modifier = Modifier
                    .size(116.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(accent.copy(alpha = 0.32f), Navy900)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (playback.isBuffering) {
                    CircularProgressIndicator(color = accent, modifier = Modifier.size(36.dp))
                } else if (playback.isPlaying) {
                    AudioEqualizerIndicator(isPlaying = true, color = accent, maxHeight = 36.dp)
                } else {
                    Icon(
                        imageVector = if (isAyat) Icons.Rounded.MenuBook else Icons.Rounded.GraphicEq,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            if (track != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isAyat) "QURANIC AYAT" else "SONG / FOCUS",
                        style = MaterialTheme.typography.labelSmall,
                        color = accent,
                        fontWeight = FontWeight.Bold
                    )
                    if (track.isAvailableOffline) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(EmeraldMuted)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.OfflinePin,
                                contentDescription = null,
                                tint = EmeraldAccent,
                                modifier = Modifier.size(12.dp)
                            )
                            Text("Offline Ready", style = MaterialTheme.typography.labelSmall, color = EmeraldAccent)
                        }
                    }
                }

                Text(
                    text = track.title,
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = if (isAyat && track.surahNameArabic != null) {
                        "${track.displayReference} • ${track.surahNameArabic}"
                    } else {
                        track.displayReference
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                // Ayat Arabic & Translation Panel
                if (isAyat && (!track.arabicText.isNullOrBlank() || !track.translationText.isNullOrBlank())) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = state.showArabicText,
                            onClick = onToggleArabic,
                            label = { Text("Arabic") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldMuted,
                                selectedLabelColor = EmeraldAccent
                            )
                        )
                        FilterChip(
                            selected = state.showTranslation,
                            onClick = onToggleTranslation,
                            label = { Text("Translation") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldMuted,
                                selectedLabelColor = EmeraldAccent
                            )
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.04f))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (state.showArabicText && !track.arabicText.isNullOrBlank()) {
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                                Text(
                                    text = track.arabicText,
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontSize = 22.sp,
                                        lineHeight = 35.sp
                                    ),
                                    color = TextPrimary,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        if (state.showTranslation && !track.translationText.isNullOrBlank()) {
                            Text(
                                text = track.translationText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "Select a track or Ayat below",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextSecondary
                )
            }

            // Seek bar & Timestamps
            val safeDuration = playback.durationMs.coerceAtLeast(1L)
            Column(modifier = Modifier.fillMaxWidth()) {
                Slider(
                    value = playback.positionMs.coerceIn(0L, safeDuration).toFloat(),
                    onValueChange = { onSeekTo(it.toLong()) },
                    valueRange = 0f..safeDuration.toFloat(),
                    colors = SliderDefaults.colors(
                        thumbColor = accent,
                        activeTrackColor = accent,
                        inactiveTrackColor = accent.copy(alpha = 0.22f)
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatDurationMs(playback.positionMs),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Text(
                        text = formatDurationMs(playback.durationMs),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }

            // Playback Transport Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onToggleShuffle) {
                    Icon(
                        imageVector = Icons.Rounded.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (playback.shuffleEnabled) accent else TextMuted
                    )
                }
                IconButton(onClick = onPrevious, modifier = Modifier.size(46.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.SkipPrevious,
                        contentDescription = "Previous",
                        tint = TextPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }
                IconButton(
                    onClick = onTogglePlayPause,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(accent)
                ) {
                    Icon(
                        imageVector = if (playback.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (playback.isPlaying) "Pause" else "Play",
                        tint = Navy950,
                        modifier = Modifier.size(34.dp)
                    )
                }
                IconButton(onClick = onNext, modifier = Modifier.size(46.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.SkipNext,
                        contentDescription = "Next",
                        tint = TextPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }
                IconButton(onClick = onCycleRepeat) {
                    Icon(
                        imageVector = if (playback.repeatMode == RepeatMode.ONE) {
                            Icons.Rounded.RepeatOne
                        } else {
                            Icons.Rounded.Repeat
                        },
                        contentDescription = "Repeat mode",
                        tint = if (playback.repeatMode != RepeatMode.OFF) accent else TextMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun TrackQueueItem(
    track: AudioTrack,
    isCurrent: Boolean,
    isPlaying: Boolean,
    isDownloading: Boolean,
    onClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onDeleteClick: (() -> Unit)?
) {
    val accent = if (track.mode == AudioMode.AYAT) EmeraldAccent else CyanAccent
    val bgTint = if (track.mode == AudioMode.AYAT) EmeraldMuted else CyanMuted

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 16.dp,
        borderColor = if (isCurrent) accent.copy(alpha = 0.65f) else GlassBorder,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isCurrent) accent.copy(alpha = 0.25f) else bgTint),
                contentAlignment = Alignment.Center
            ) {
                if (isPlaying) {
                    AudioEqualizerIndicator(isPlaying = true, color = accent, maxHeight = 16.dp)
                } else {
                    Icon(
                        imageVector = if (track.mode == AudioMode.AYAT) Icons.Rounded.MenuBook else Icons.Rounded.MusicNote,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isCurrent) accent else TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${track.displayReference} • ${formatDurationMs(track.durationMs)}",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (isDownloading) {
                CircularProgressIndicator(
                    color = accent,
                    strokeWidth = 2.dp,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(20.dp)
                )
            } else if (track.isAvailableOffline) {
                Icon(
                    imageVector = Icons.Rounded.OfflinePin,
                    contentDescription = "Available offline",
                    tint = EmeraldAccent,
                    modifier = Modifier
                        .padding(horizontal = 6.dp)
                        .size(20.dp)
                )
            } else if (!track.streamUrl.isNullOrBlank()) {
                IconButton(onClick = onDownloadClick, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.CloudDownload,
                        contentDescription = "Download offline",
                        tint = CyanAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (onDeleteClick != null) {
                IconButton(onClick = onDeleteClick, modifier = Modifier.size(34.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = "Remove imported track",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AyatMetadataImportDialog(
    fileCount: Int,
    onDismiss: () -> Unit,
    onConfirm: (surahName: String?, startAyat: Int?) -> Unit
) {
    var surahName by remember { mutableStateOf("") }
    var ayatNumberText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = Slate850,
            border = BorderStroke(1.dp, GlassBorderEmerald)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Tag Imported Ayat Audio ($fileCount file${if (fileCount > 1) "s" else ""})",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Optional: add Surah name & starting Ayat number for offline Quran display.",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = surahName,
                    onValueChange = { surahName = it },
                    label = { Text("Surah Name (optional)") },
                    placeholder = { Text("e.g., Surah Ar-Rahman") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = ayatNumberText,
                    onValueChange = { input -> ayatNumberText = input.filter { it.isDigit() } },
                    label = { Text("Starting Ayat # (optional)") },
                    placeholder = { Text("e.g., 1") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    TextButton(onClick = { onConfirm(null, null) }) { Text("Skip") }
                    Button(
                        onClick = {
                            onConfirm(
                                surahName.trim().takeIf { it.isNotEmpty() },
                                ayatNumberText.toIntOrNull()
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldAccent,
                            contentColor = Navy950
                        )
                    ) {
                        Text("Import", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceAudioPickerDialog(
    candidates: List<DeviceAudioCandidate>,
    targetMode: AudioMode,
    onDismiss: () -> Unit,
    onImportSelected: (List<DeviceAudioCandidate>) -> Unit
) {
    var selectedUris by remember(candidates) {
        mutableStateOf(candidates.take(5).map { it.contentUri }.toSet())
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = Slate850,
            border = BorderStroke(1.dp, GlassBorderCyan)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Add Device Audio to ${targetMode.label}",
                    style = MaterialTheme.typography.titleLarge
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(candidates, key = { it.contentUri.toString() }) { candidate ->
                        val checked = candidate.contentUri in selectedUris
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    selectedUris = if (checked) {
                                        selectedUris - candidate.contentUri
                                    } else {
                                        selectedUris + candidate.contentUri
                                    }
                                }
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = {
                                    selectedUris = if (checked) {
                                        selectedUris - candidate.contentUri
                                    } else {
                                        selectedUris + candidate.contentUri
                                    }
                                }
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = candidate.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${candidate.artist} • ${formatDurationMs(candidate.durationMs)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(
                        onClick = {
                            onImportSelected(candidates.filter { it.contentUri in selectedUris })
                        },
                        enabled = selectedUris.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanAccent,
                            contentColor = Navy950
                        )
                    ) {
                        Text("Add (${selectedUris.size})", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
