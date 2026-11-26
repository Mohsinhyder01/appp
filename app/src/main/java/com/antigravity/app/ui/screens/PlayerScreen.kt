package com.antigravity.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.antigravity.app.data.model.AudioMode
import com.antigravity.app.data.model.AudioTrack
import com.antigravity.app.data.model.RepeatMode
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
    val accent = if (isAyatMode) EmeraldAccent else CyanAccent

    // File picker for importing audio
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.importLocalFiles(uris, activeMode)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp, end = 16.dp,
            top = 16.dp, bottom = bottomInsetPadding
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ───── Mode Switcher (Songs / Quran) ─────
        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 18.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    AudioMode.entries.forEach { mode ->
                        val selected = mode == activeMode
                        val modeAccent = if (mode == AudioMode.AYAT) EmeraldAccent else CyanAccent
                        val icon = if (mode == AudioMode.AYAT) Icons.Rounded.MenuBook else Icons.Rounded.MusicNote

                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (selected) modeAccent.copy(alpha = 0.22f) else Color.Transparent)
                                .clickable { viewModel.switchMode(mode) }
                                .padding(vertical = 14.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(icon, null, tint = if (selected) modeAccent else TextMuted, modifier = Modifier.size(22.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (mode == AudioMode.SONG) "Songs" else "Quran",
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                                color = if (selected) modeAccent else TextMuted,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // ───── Now Playing Card ─────
        item {
            val playback = state.playbackState
            val track = playback.currentTrack
            val isAyat = track?.mode == AudioMode.AYAT
            val trackAccent = if (isAyat) EmeraldAccent else CyanAccent

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 24.dp,
                borderColor = if (isAyat) GlassBorderEmerald else GlassBorderCyan,
                elevated = true
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Album art / visualizer
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(trackAccent.copy(alpha = 0.3f), Navy900)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (playback.isBuffering) {
                            CircularProgressIndicator(color = trackAccent, modifier = Modifier.size(32.dp))
                        } else if (playback.isPlaying) {
                            AudioEqualizerIndicator(isPlaying = true, color = trackAccent, maxHeight = 32.dp)
                        } else {
                            Icon(
                                if (isAyat) Icons.Rounded.MenuBook else Icons.Rounded.MusicNote,
                                null, tint = trackAccent, modifier = Modifier.size(40.dp)
                            )
                        }
                    }

                    // Track info
                    if (track != null) {
                        Text(
                            text = track.title,
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = track.subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )

                        // Arabic text for Ayat
                        if (isAyat && !track.arabicText.isNullOrBlank()) {
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                                Text(
                                    text = track.arabicText,
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontSize = 20.sp, lineHeight = 32.sp
                                    ),
                                    color = TextPrimary,
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                                )
                            }
                        }
                        if (isAyat && !track.translationText.isNullOrBlank()) {
                            Text(
                                text = track.translationText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        Text("Tap a track below to play", style = MaterialTheme.typography.bodyLarge, color = TextMuted)
                    }

                    // Seek bar
                    val safeDuration = playback.durationMs.coerceAtLeast(1L)
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Slider(
                            value = playback.positionMs.coerceIn(0L, safeDuration).toFloat(),
                            onValueChange = { viewModel.seekTo(it.toLong()) },
                            valueRange = 0f..safeDuration.toFloat(),
                            colors = SliderDefaults.colors(
                                thumbColor = trackAccent,
                                activeTrackColor = trackAccent,
                                inactiveTrackColor = trackAccent.copy(alpha = 0.2f)
                            )
                        )
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(formatDurationMs(playback.positionMs), style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Text(formatDurationMs(playback.durationMs), style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        }
                    }

                    // Transport controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = viewModel::toggleShuffle) {
                            Icon(Icons.Rounded.Shuffle, "Shuffle",
                                tint = if (playback.shuffleEnabled) trackAccent else TextMuted)
                        }
                        IconButton(onClick = viewModel::previous, Modifier.size(48.dp)) {
                            Icon(Icons.Rounded.SkipPrevious, "Previous", tint = TextPrimary, modifier = Modifier.size(32.dp))
                        }
                        IconButton(
                            onClick = viewModel::togglePlayPause,
                            modifier = Modifier.size(64.dp).clip(CircleShape).background(trackAccent)
                        ) {
                            Icon(
                                if (playback.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                "Play/Pause", tint = Navy950, modifier = Modifier.size(34.dp)
                            )
                        }
                        IconButton(onClick = viewModel::next, Modifier.size(48.dp)) {
                            Icon(Icons.Rounded.SkipNext, "Next", tint = TextPrimary, modifier = Modifier.size(32.dp))
                        }
                        IconButton(onClick = viewModel::cycleRepeatMode) {
                            Icon(
                                if (playback.repeatMode == RepeatMode.ONE) Icons.Rounded.RepeatOne else Icons.Rounded.Repeat,
                                "Repeat",
                                tint = if (playback.repeatMode != RepeatMode.OFF) trackAccent else TextMuted
                            )
                        }
                    }
                }
            }
        }

        // ───── Add Music Button ─────
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isAyatMode) "Quran Library" else "My Songs",
                    style = MaterialTheme.typography.titleLarge
                )
                Button(
                    onClick = { filePickerLauncher.launch(arrayOf("audio/*")) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = Navy950),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Rounded.Add, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add", fontWeight = FontWeight.Bold)
                }
            }
        }

        // ───── Track List ─────
        if (state.visibleTracks.isEmpty()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (isAyatMode) "No Quran tracks yet" else "No songs found",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = if (isAyatMode) "Tap + Add to import Quran recitations"
                            else "Grant audio permission to auto-discover your music, or tap + Add",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(state.visibleTracks, key = { it.id }) { track ->
                val isCurrent = state.playbackState.currentTrack?.id == track.id
                val isPlaying = isCurrent && state.playbackState.isPlaying
                SimpleTrackRow(
                    track = track,
                    isCurrent = isCurrent,
                    isPlaying = isPlaying,
                    onClick = { viewModel.playTrack(track) }
                )
            }
        }
    }
}

@Composable
private fun SimpleTrackRow(
    track: AudioTrack,
    isCurrent: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    val isAyat = track.mode == AudioMode.AYAT
    val accent = if (isAyat) EmeraldAccent else CyanAccent
    val bgTint = if (isAyat) EmeraldMuted else CyanMuted

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 16.dp,
        borderColor = if (isCurrent) accent.copy(alpha = 0.6f) else GlassBorder,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Track icon
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isCurrent) accent.copy(alpha = 0.25f) else bgTint),
                contentAlignment = Alignment.Center
            ) {
                if (isPlaying) {
                    AudioEqualizerIndicator(isPlaying = true, color = accent, maxHeight = 18.dp)
                } else {
                    Icon(
                        if (isAyat) Icons.Rounded.MenuBook else Icons.Rounded.MusicNote,
                        null, tint = accent, modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            // Track info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isCurrent) accent else TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${track.subtitle} • ${formatDurationMs(track.durationMs)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Offline indicator
            if (track.isAvailableOffline) {
                Icon(
                    Icons.Rounded.OfflinePin, "Offline",
                    tint = EmeraldAccent, modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
