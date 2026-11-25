package com.antigravity.app.player

import com.antigravity.app.data.model.AudioMode
import com.antigravity.app.data.model.AudioTrack
import com.antigravity.app.data.model.RepeatMode

data class PlaybackState(
    val currentTrack: AudioTrack? = null,
    val queue: List<AudioTrack> = emptyList(),
    val activeMode: AudioMode = AudioMode.SONG,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val shuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val errorMessage: String? = null
) {
    val progressFraction: Float
        get() = if (durationMs > 0L) (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f

    val hasActiveTrack: Boolean
        get() = currentTrack != null
}
