package com.antigravity.app.ui.screens

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
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.antigravity.app.data.model.AudioMode
import com.antigravity.app.ui.components.GlassCard
import com.antigravity.app.ui.components.TaskRowCard
import com.antigravity.app.ui.theme.AmberAccent
import com.antigravity.app.ui.theme.CyanAccent
import com.antigravity.app.ui.theme.CyanMuted
import com.antigravity.app.ui.theme.EmeraldAccent
import com.antigravity.app.ui.theme.EmeraldMuted
import com.antigravity.app.ui.theme.GlassBorderCyan
import com.antigravity.app.ui.theme.GlassBorderEmerald
import com.antigravity.app.ui.theme.Navy950
import com.antigravity.app.ui.theme.TextMuted
import com.antigravity.app.ui.theme.TextPrimary
import com.antigravity.app.ui.theme.TextSecondary
import java.util.Calendar

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToTodo: () -> Unit,
    onNavigateToPlayer: () -> Unit,
    bottomContentPadding: DpPadding = DpPadding(140.dp)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> "Good Morning ☀️"
            in 12..16 -> "Good Afternoon"
            in 17..20 -> "Good Evening 🌙"
            else -> "Peaceful Night ✨"
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp, end = 16.dp,
            top = 20.dp, bottom = bottomContentPadding.value
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ───── Greeting ─────
        item {
            Text(
                text = greeting,
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 32.sp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "What would you like to do?",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary
            )
        }

        // ───── Quick Action Cards ─────
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Songs Card
                QuickActionCard(
                    title = "My Songs",
                    subtitle = "${state.songCount} tracks",
                    icon = Icons.Rounded.MusicNote,
                    accentColor = CyanAccent,
                    bgColor = CyanMuted,
                    onClick = {
                        viewModel.switchToSongs()
                        onNavigateToPlayer()
                    },
                    modifier = Modifier.weight(1f)
                )
                // Quran Card
                QuickActionCard(
                    title = "Quran",
                    subtitle = "${state.ayatCount} ayats",
                    icon = Icons.Rounded.MenuBook,
                    accentColor = EmeraldAccent,
                    bgColor = EmeraldMuted,
                    onClick = {
                        viewModel.switchToQuran()
                        onNavigateToPlayer()
                    },
                    modifier = Modifier.weight(1f)
                )
                // To-Do Card
                QuickActionCard(
                    title = "To-Do",
                    subtitle = "${state.pendingCount} pending",
                    icon = Icons.Rounded.CheckCircle,
                    accentColor = AmberAccent,
                    bgColor = AmberAccent.copy(alpha = 0.15f),
                    onClick = onNavigateToTodo,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ───── Now Playing (if something is active) ─────
        state.playbackState.currentTrack?.let { track ->
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = if (track.mode == AudioMode.AYAT) GlassBorderEmerald else GlassBorderCyan,
                    elevated = true,
                    onClick = onNavigateToPlayer
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "NOW PLAYING",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (track.mode == AudioMode.AYAT) EmeraldAccent else CyanAccent,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = track.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = track.subtitle,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary,
                                    maxLines = 1
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            val accent = if (track.mode == AudioMode.AYAT) EmeraldAccent else CyanAccent
                            IconButton(
                                onClick = { viewModel.togglePlayPause() },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(accent)
                            ) {
                                Icon(
                                    imageVector = if (state.playbackState.isPlaying)
                                        Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                    contentDescription = "Play/Pause",
                                    tint = Navy950,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = { viewModel.next() },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.SkipNext,
                                    contentDescription = "Next",
                                    tint = TextPrimary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { state.playbackState.progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = if (track.mode == AudioMode.AYAT) EmeraldAccent else CyanAccent,
                            trackColor = if (track.mode == AudioMode.AYAT) EmeraldMuted else CyanMuted
                        )
                    }
                }
            }
        }

        // ───── Daily Ayat ─────
        item {
            val ayat = state.dailyAyat
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = GlassBorderEmerald,
                elevated = true,
                onClick = {
                    viewModel.playDailyAyat(ayat.trackId)
                    onNavigateToPlayer()
                }
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "📖 Daily Ayat — ${ayat.surahName}",
                        style = MaterialTheme.typography.labelMedium,
                        color = EmeraldAccent
                    )
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        Text(
                            text = ayat.arabicText,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontSize = 22.sp, lineHeight = 34.sp
                            ),
                            color = TextPrimary,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Text(
                        text = "\"${ayat.translationText}\"",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary.copy(alpha = 0.9f)
                    )
                    Text(
                        text = "Tap to listen  ▶",
                        style = MaterialTheme.typography.labelMedium,
                        color = EmeraldAccent,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // ───── Pending Tasks ─────
        if (state.pendingTodayTasks.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Pending Tasks", style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = "See all →",
                        style = MaterialTheme.typography.labelMedium,
                        color = CyanAccent,
                        modifier = Modifier.clickable(onClick = onNavigateToTodo)
                    )
                }
            }
            items(state.pendingTodayTasks.take(3), key = { it.id }) { task ->
                TaskRowCard(
                    task = task,
                    onToggleCompleted = { viewModel.toggleTaskCompleted(task) },
                    onClick = onNavigateToTodo
                )
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: androidx.compose.ui.graphics.Color,
    bgColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier,
        cornerRadius = 20.dp,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(26.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
        }
    }
}

@JvmInline
value class DpPadding(val value: androidx.compose.ui.unit.Dp)
