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
    val scannedCandidates: List<DeviceAudioCandidate> = emptyList(),
    val isScanningDevice: Boolean = false,
    val statusBanner: String? = null,
    val showArabicText: Boolean = true,
    val showTranslation: Boolean = true
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
        PlayerUiState(
            playbackState = playback,
            visibleTracks = modeTracks,
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

    fun switchMode(mode: AudioMode) = audioController.switchMode(mode)

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

    fun importLocalFiles(
        uris: List<Uri>,
        mode: AudioMode,
        surahName: String? = null,
        ayatNumber: Int? = null
    ) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            val imported = audioRepository.importLocalAudioUris(uris, mode, surahName, ayatNumber)
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
            if (found.isNotEmpty()) {
                audioRepository.addDeviceCandidates(found, AudioMode.SONG)
            }
            localFlags.update { it.copy(isScanningDevice = false, scannedCandidates = emptyList()) }
        }
    }

    fun deleteTrack(track: AudioTrack) {
        viewModelScope.launch { audioRepository.deleteTrack(track) }
    }

    fun dismissBanner() {
        localFlags.update { it.copy(statusBanner = null) }
    }

    fun clearPlayerError() = audioController.clearError()

    fun dismissScannedCandidates() {
        localFlags.update { it.copy(scannedCandidates = emptyList()) }
    }

    fun importScannedCandidates(candidates: List<DeviceAudioCandidate>, mode: AudioMode) {
        viewModelScope.launch {
            audioRepository.addDeviceCandidates(candidates, mode)
            localFlags.update { it.copy(scannedCandidates = emptyList()) }
        }
    }

    fun toggleArabicText() { localFlags.update { it.copy(showArabicText = !it.showArabicText) } }
    fun toggleTranslation() { localFlags.update { it.copy(showTranslation = !it.showTranslation) } }
    fun toggleOfflineOnly() {
        viewModelScope.launch { preferences.saveOfflineOnlyFilter(!uiState.value.offlineOnly) }
    }
    fun downloadTrackOffline(track: AudioTrack) {
        viewModelScope.launch { audioRepository.downloadTrackOffline(track) }
    }
    fun downloadAllAyatsOffline() {
        viewModelScope.launch { audioRepository.downloadAllPendingAyats() }
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
