package com.antigravity.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.OfflinePin
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.antigravity.app.ui.components.AudioEqualizerIndicator
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
            in 5..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..20 -> "Good Evening"
            else -> "Peaceful Night"
        }
    }

    val totalTasks = state.pendingCount + state.completedCount
    val completionFraction = if (totalTasks > 0) {
        state.completedCount.toFloat() / totalTasks.toFloat()
    } else 0f

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = bottomContentPadding.value
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "ANTIGRAVITY",
                    style = MaterialTheme.typography.labelMedium,
                    color = CyanAccent,
                    letterSpacing = 2.sp
                )
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.headlineLarge
                )
                Text(
                    text = "Stay focused, light, and grounded today.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Daily Progress Summary
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = GlassBorderCyan,
                elevated = true
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Today's Momentum",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "${state.pendingCount} pending • ${state.completedCount} completed",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Text(
                            text = "${(completionFraction * 100).toInt()}%",
                            style = MaterialTheme.typography.headlineMedium,
                            color = CyanAccent
                        )
                    }
                    LinearProgressIndicator(
                        progress = { completionFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = CyanAccent,
                        trackColor = CyanMuted
                    )
                }
            }
        }

        // Daily Ayat Card
        item {
            val ayat = state.dailyAyat
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = GlassBorderEmerald,
                elevated = true
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = AmberAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Daily Ayat",
                                style = MaterialTheme.typography.labelMedium,
                                color = EmeraldAccent
                            )
                        }
                        Text(
                            text = "${ayat.surahName} • ${ayat.surahNumber}:${ayat.ayatNumber}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }

                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        Text(
                            text = ayat.arabicText,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontSize = 23.sp,
                                lineHeight = 36.sp
                            ),
                            color = TextPrimary,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Text(
                        text = "\"${ayat.translationText}\"",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary.copy(alpha = 0.92f)
                    )

                    Text(
                        text = ayat.reflectionNote,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = {
                                viewModel.playDailyAyat(ayat.trackId)
                                onNavigateToPlayer()
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldAccent,
                                contentColor = Navy950
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Listen to Ayat", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Quick Resume Last Played Audio
        state.playbackState.currentTrack?.let { lastTrack ->
            item {
                val isAyat = lastTrack.mode == AudioMode.AYAT
                val accent = if (isAyat) EmeraldAccent else CyanAccent
                val badgeBg = if (isAyat) EmeraldMuted else CyanMuted

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = if (isAyat) GlassBorderEmerald else GlassBorderCyan,
                    onClick = onNavigateToPlayer
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(badgeBg),
                            contentAlignment = Alignment.Center
                        ) {
                            if (state.playbackState.isPlaying) {
                                AudioEqualizerIndicator(isPlaying = true, color = accent)
                            } else {
                                Icon(
                                    imageVector = Icons.Rounded.GraphicEq,
                                    contentDescription = null,
                                    tint = accent
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "QUICK RESUME • ${lastTrack.mode.label.uppercase()}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = accent,
                                    fontWeight = FontWeight.Bold
                                )
                                if (lastTrack.isAvailableOffline) {
                                    Icon(
                                        imageVector = Icons.Rounded.OfflinePin,
                                        contentDescription = "Offline ready",
                                        tint = EmeraldAccent,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                            Text(
                                text = lastTrack.title,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = lastTrack.displayReference,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        IconButton(
                            onClick = { viewModel.togglePlayPause() },
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(accent)
                        ) {
                            Icon(
                                imageVector = if (state.playbackState.isPlaying) {
                                    Icons.Rounded.Pause
                                } else {
                                    Icons.Rounded.PlayArrow
                                },
                                contentDescription = "Resume",
                                tint = Navy950
                            )
                        }
                    }
                }
            }
        }

        // Pending Tasks Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Pending Tasks",
                    style = MaterialTheme.typography.titleLarge
                )
                TextButton(onClick = onNavigateToTodo) {
                    Text("Open To-Do", color = CyanAccent)
                }
            }
        }

        if (state.pendingTodayTasks.isEmpty()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "All caught up!",
                            style = MaterialTheme.typography.titleMedium,
                            color = EmeraldAccent
                        )
                        Text(
                            text = "Add a new task in To-Do or unwind with calm audio.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )
                    }
                }
            }
        } else {
            items(state.pendingTodayTasks, key = { it.id }) { task ->
                TaskRowCard(
                    task = task,
                    onToggleCompleted = { viewModel.toggleTaskCompleted(task) },
                    onClick = onNavigateToTodo
                )
            }
        }
    }
}

@JvmInline
value class DpPadding(val value: androidx.compose.ui.unit.Dp)
