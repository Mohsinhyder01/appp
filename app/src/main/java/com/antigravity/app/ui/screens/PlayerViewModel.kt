package com.antigravity.app.ui.screens

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.antigravity.app.data.local.PlaybackPreferences
import com.antigravity.app.data.model.AudioMode
import com.antigravity.app.data.model.AudioTrack
import com.antigravity.app.data.offline.DeviceAudioCandidate
import com.antigravity.app.data.repository.AudioRepository
import com.antigravity.app.player.AudioController
import com.antigravity.app.player.PlaybackState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PlayerUiState(
    val playbackState: PlaybackState = PlaybackState(),
    val visibleTracks: List<AudioTrack> = emptyList(),
    val downloadingIds: Set<String> = emptySet(),
    val offlineOnly: Boolean = false,
    val showArabicText: Boolean = true,
    val showTranslation: Boolean = true,
    val scannedCandidates: List<DeviceAudioCandidate> = emptyList(),
    val isScanningDevice: Boolean = false,
    val statusBanner: String? = null
)

class PlayerViewModel(
    private val audioRepository: AudioRepository,
    private val audioController: AudioController,
    private val preferences: PlaybackPreferences
) : ViewModel() {

    private data class LocalUiFlags(
        val showArabicText: Boolean = true,
        val showTranslation: Boolean = true,
        val scannedCandidates: List<DeviceAudioCandidate> = emptyList(),
        val isScanningDevice: Boolean = false,
        val statusBanner: String? = null
    )

    private val localFlags = MutableStateFlow(LocalUiFlags())

    val uiState: StateFlow<PlayerUiState> = combine(
        audioController.playbackState,
        audioRepository.allTracks,
        audioRepository.downloadingTrackIds,
        preferences.preferencesFlow,
        localFlags
    ) { playback, allTracks, downloadingIds, prefs, flags ->
        val modeTracks = allTracks.filter { it.mode == playback.activeMode }
        val filtered = if (prefs.offlineOnlyFilter) {
            modeTracks.filter { it.isAvailableOffline }
        } else {
            modeTracks
        }
        PlayerUiState(
            playbackState = playback,
            visibleTracks = filtered,
            downloadingIds = downloadingIds,
            offlineOnly = prefs.offlineOnlyFilter,
            showArabicText = flags.showArabicText,
            showTranslation = flags.showTranslation,
            scannedCandidates = flags.scannedCandidates,
            isScanningDevice = flags.isScanningDevice,
            statusBanner = flags.statusBanner
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = PlayerUiState()
    )

    fun switchMode(mode: AudioMode) {
        audioController.switchMode(mode)
    }

    fun playTrack(track: AudioTrack) {
        val queue = uiState.value.visibleTracks.ifEmpty { listOf(track) }
        audioController.playTrack(track, queue)
    }

    fun togglePlayPause() = audioController.togglePlayPause()
    fun next() = audioController.next()
    fun previous() = audioController.previous()
    fun seekTo(positionMs: Long) = audioController.seekTo(positionMs)
    fun toggleShuffle() = audioController.toggleShuffle()
    fun cycleRepeatMode() = audioController.cycleRepeatMode()
    fun clearPlayerError() = audioController.clearError()

    fun toggleOfflineOnly() {
        val next = !uiState.value.offlineOnly
        viewModelScope.launch {
            preferences.saveOfflineOnlyFilter(next)
        }
    }

    fun toggleArabicText() {
        localFlags.update { it.copy(showArabicText = !it.showArabicText) }
    }

    fun toggleTranslation() {
        localFlags.update { it.copy(showTranslation = !it.showTranslation) }
    }

    fun downloadTrackOffline(track: AudioTrack) {
        viewModelScope.launch {
            val result = audioRepository.downloadTrackOffline(track)
            localFlags.update {
                it.copy(
                    statusBanner = if (result.isSuccess) {
                        "Saved \"${track.title}\" for offline playback."
                    } else {
                        "Download failed (${result.exceptionOrNull()?.message ?: "offline"}). Check connection or import a local file."
                    }
                )
            }
        }
    }

    fun downloadAllAyatsOffline() {
        viewModelScope.launch {
            localFlags.update { it.copy(statusBanner = "Downloading Quranic Ayats for offline playback...") }
            val count = audioRepository.downloadAllPendingAyats()
            localFlags.update {
                it.copy(
                    statusBanner = if (count > 0) {
                        "Saved $count Ayat recitation(s) offline."
                    } else {
                        "No new Ayats downloaded (already offline or no network)."
                    }
                )
            }
        }
    }

    fun importLocalFiles(
        uris: List<Uri>,
        mode: AudioMode,
        surahName: String? = null,
        ayatNumber: Int? = null
    ) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            val imported = audioRepository.importLocalAudioUris(uris, mode, surahName, ayatNumber)
            localFlags.update {
                it.copy(
                    statusBanner = if (imported.isNotEmpty()) {
                        "Imported ${imported.size} offline ${mode.label.lowercase()} file(s)."
                    } else {
                        "Could not import selected audio file(s)."
                    }
                )
            }
            imported.firstOrNull()?.let { first ->
                val queue = audioRepository.getTracksByMode(mode)
                audioController.playTrack(first, queue)
            }
        }
    }

    fun scanDeviceAudio() {
        viewModelScope.launch {
            localFlags.update { it.copy(isScanningDevice = true) }
            val found = audioRepository.scanDeviceAudio()
            localFlags.update {
                it.copy(
                    isScanningDevice = false,
                    scannedCandidates = found,
                    statusBanner = if (found.isEmpty()) {
                        "No audio files found via MediaStore. Try Import File to pick MP3/M4A directly."
                    } else null
                )
            }
        }
    }

    fun importScannedCandidates(candidates: List<DeviceAudioCandidate>, mode: AudioMode) {
        viewModelScope.launch {
            val added = audioRepository.addDeviceCandidates(candidates, mode)
            localFlags.update {
                it.copy(
                    scannedCandidates = emptyList(),
                    statusBanner = "Added $added local track(s) to ${mode.label} for offline playback."
                )
            }
        }
    }

    fun dismissScannedCandidates() {
        localFlags.update { it.copy(scannedCandidates = emptyList()) }
    }

    fun deleteTrack(track: AudioTrack) {
        viewModelScope.launch {
            audioRepository.deleteTrack(track)
        }
    }

    fun dismissBanner() {
        localFlags.update { it.copy(statusBanner = null) }
    }

    companion object {
        fun factory(
            audioRepository: AudioRepository,
            audioController: AudioController,
            preferences: PlaybackPreferences
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PlayerViewModel(audioRepository, audioController, preferences) as T
            }
        }
    }
}
