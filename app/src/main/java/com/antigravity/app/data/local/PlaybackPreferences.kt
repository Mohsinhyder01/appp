package com.antigravity.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.antigravity.app.data.model.AudioMode
import com.antigravity.app.data.model.RepeatMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.playbackDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "antigravity_playback_prefs"
)

data class SavedPlaybackPrefs(
    val lastTrackId: String? = null,
    val lastPositionMs: Long = 0L,
    val activeMode: AudioMode = AudioMode.SONG,
    val shuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val offlineOnlyFilter: Boolean = false
)

class PlaybackPreferences(private val context: Context) {

    private object Keys {
        val LAST_TRACK_ID = stringPreferencesKey("last_track_id")
        val LAST_POSITION_MS = longPreferencesKey("last_position_ms")
        val ACTIVE_MODE = stringPreferencesKey("active_mode")
        val SHUFFLE_ENABLED = booleanPreferencesKey("shuffle_enabled")
        val REPEAT_MODE = stringPreferencesKey("repeat_mode")
        val OFFLINE_ONLY_FILTER = booleanPreferencesKey("offline_only_filter")
    }

    val preferencesFlow: Flow<SavedPlaybackPrefs> = context.playbackDataStore.data.map { prefs ->
        SavedPlaybackPrefs(
            lastTrackId = prefs[Keys.LAST_TRACK_ID],
            lastPositionMs = prefs[Keys.LAST_POSITION_MS] ?: 0L,
            activeMode = prefs[Keys.ACTIVE_MODE]
                ?.let { runCatching { AudioMode.valueOf(it) }.getOrNull() }
                ?: AudioMode.SONG,
            shuffleEnabled = prefs[Keys.SHUFFLE_ENABLED] ?: false,
            repeatMode = prefs[Keys.REPEAT_MODE]
                ?.let { runCatching { RepeatMode.valueOf(it) }.getOrNull() }
                ?: RepeatMode.OFF,
            offlineOnlyFilter = prefs[Keys.OFFLINE_ONLY_FILTER] ?: false
        )
    }

    suspend fun saveLastTrack(trackId: String, mode: AudioMode, positionMs: Long = 0L) {
        context.playbackDataStore.edit { prefs ->
            prefs[Keys.LAST_TRACK_ID] = trackId
            prefs[Keys.ACTIVE_MODE] = mode.name
            prefs[Keys.LAST_POSITION_MS] = positionMs
        }
    }

    suspend fun savePosition(positionMs: Long) {
        context.playbackDataStore.edit { prefs ->
            prefs[Keys.LAST_POSITION_MS] = positionMs
        }
    }

    suspend fun saveActiveMode(mode: AudioMode) {
        context.playbackDataStore.edit { prefs ->
            prefs[Keys.ACTIVE_MODE] = mode.name
        }
    }

    suspend fun saveShuffle(enabled: Boolean) {
        context.playbackDataStore.edit { prefs ->
            prefs[Keys.SHUFFLE_ENABLED] = enabled
        }
    }

    suspend fun saveRepeatMode(repeatMode: RepeatMode) {
        context.playbackDataStore.edit { prefs ->
            prefs[Keys.REPEAT_MODE] = repeatMode.name
        }
    }

    suspend fun saveOfflineOnlyFilter(enabled: Boolean) {
        context.playbackDataStore.edit { prefs ->
            prefs[Keys.OFFLINE_ONLY_FILTER] = enabled
        }
    }
}
