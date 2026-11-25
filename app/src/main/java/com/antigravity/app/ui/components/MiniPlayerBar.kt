package com.antigravity.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.OfflinePin
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.antigravity.app.data.model.AudioMode
import com.antigravity.app.player.PlaybackState
import com.antigravity.app.ui.theme.CyanAccent
import com.antigravity.app.ui.theme.CyanMuted
import com.antigravity.app.ui.theme.EmeraldAccent
import com.antigravity.app.ui.theme.EmeraldMuted
import com.antigravity.app.ui.theme.GlassBorderCyan
import com.antigravity.app.ui.theme.GlassBorderEmerald
import com.antigravity.app.ui.theme.Navy950
import com.antigravity.app.ui.theme.TextPrimary
import com.antigravity.app.ui.theme.TextSecondary

@Composable
fun MiniPlayerBar(
    playbackState: PlaybackState,
    visible: Boolean,
    onExpandClick: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val track = playbackState.currentTrack
    AnimatedVisibility(
        visible = visible && track != null,
        enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut(),
        modifier = modifier
    ) {
        if (track != null) {
            val isAyat = track.mode == AudioMode.AYAT
            val accent = if (isAyat) EmeraldAccent else CyanAccent
            val badgeBg = if (isAyat) EmeraldMuted else CyanMuted
            val border = if (isAyat) GlassBorderEmerald else GlassBorderCyan

            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                cornerRadius = 18.dp,
                borderColor = border,
                elevated = true,
                onClick = onExpandClick
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(badgeBg),
                            contentAlignment = Alignment.Center
                        ) {
                            if (playbackState.isPlaying) {
                                AudioEqualizerIndicator(isPlaying = true, color = accent, maxHeight = 16.dp)
                            } else {
                                Icon(
                                    imageVector = Icons.Rounded.GraphicEq,
                                    contentDescription = null,
                                    tint = accent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = if (isAyat) "AYAT" else "SONG",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = accent,
                                    fontWeight = FontWeight.Bold
                                )
                                if (track.isAvailableOffline) {
                                    Icon(
                                        imageVector = Icons.Rounded.OfflinePin,
                                        contentDescription = "Available offline",
                                        tint = EmeraldAccent,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                            Text(
                                text = track.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = track.displayReference,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        IconButton(
                            onClick = onTogglePlayPause,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(accent)
                        ) {
                            Icon(
                                imageVector = if (playbackState.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                contentDescription = if (playbackState.isPlaying) "Pause" else "Play",
                                tint = Navy950,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        IconButton(
                            onClick = onNextClick,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SkipNext,
                                contentDescription = "Next track",
                                tint = TextPrimary
                            )
                        }
                    }

                    LinearProgressIndicator(
                        progress = { playbackState.progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.5.dp),
                        color = accent,
                        trackColor = accent.copy(alpha = 0.16f)
                    )
                }
            }
        }
    }
}
