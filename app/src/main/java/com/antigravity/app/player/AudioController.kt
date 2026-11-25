package com.antigravity.app.player

import android.content.ComponentName
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.antigravity.app.data.local.PlaybackPreferences
import com.antigravity.app.data.model.AudioMode
import com.antigravity.app.data.model.AudioTrack
import com.antigravity.app.data.model.RepeatMode
import com.antigravity.app.data.repository.AudioRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

class AudioController(
    private val context: Context,
    private val audioRepository: AudioRepository,
    private val preferences: PlaybackPreferences
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private var mediaController: MediaController? = null
    private var progressJob: Job? = null
    private var trackLookup: Map<String, AudioTrack> = emptyMap()
    private var initialized = false

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _playbackState.update { it.copy(isPlaying = isPlaying, errorMessage = null) }
            if (isPlaying) startProgressUpdates() else stopProgressUpdates()
        }

        override fun onPlaybackStateChanged(state: Int) {
            val controller = mediaController ?: return
            val duration = controller.duration.takeIf { it > 0L }
                ?: _playbackState.value.currentTrack?.durationMs
                ?: 0L
            _playbackState.update {
                it.copy(
                    isBuffering = state == Player.STATE_BUFFERING,
                    durationMs = duration,
                    positionMs = controller.currentPosition.coerceAtLeast(0L)
                )
            }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            val mediaId = mediaItem?.mediaId ?: return
            val resolvedTrack = trackLookup[mediaId]
                ?: _playbackState.value.queue.firstOrNull { it.id == mediaId }
            if (resolvedTrack != null) {
                _playbackState.update {
                    it.copy(
                        currentTrack = resolvedTrack,
                        activeMode = resolvedTrack.mode,
                        durationMs = resolvedTrack.durationMs,
                        errorMessage = null
                    )
                }
                scope.launch {
                    preferences.saveLastTrack(resolvedTrack.id, resolvedTrack.mode, 0L)
                }
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            val track = _playbackState.value.currentTrack
            val message = if (track != null && !track.isAvailableOffline) {
                "Offline audio not downloaded yet for \"${track.title}\". Connect once to download or import a local MP3."
            } else {
                error.localizedMessage ?: "Unable to play selected audio."
            }
            _playbackState.update { it.copy(isPlaying = false, isBuffering = false, errorMessage = message) }
            stopProgressUpdates()
        }
    }

    fun initialize() {
        if (initialized) return
        initialized = true

        scope.launch {
            audioRepository.ensureSeededOffline()

            // Keep track metadata synchronized (e.g., after offline downloads or imports)
            launch {
                audioRepository.allTracks.collect { allTracks ->
                    trackLookup = allTracks.associateBy { it.id }
                    val currentId = _playbackState.value.currentTrack?.id
                    if (currentId != null) {
                        val updated = trackLookup[currentId]
                        if (updated != null) {
                            _playbackState.update { it.copy(currentTrack = updated) }
                        }
                    }
                }
            }

            val savedPrefs = preferences.preferencesFlow.first()
            _playbackState.update {
                it.copy(
                    activeMode = savedPrefs.activeMode,
                    shuffleEnabled = savedPrefs.shuffleEnabled,
                    repeatMode = savedPrefs.repeatMode
                )
            }

            val controller = connectController()
            controller?.let { player ->
                player.shuffleModeEnabled = savedPrefs.shuffleEnabled
                player.repeatMode = savedPrefs.repeatMode.toMedia3RepeatMode()

                val lastTrack = savedPrefs.lastTrackId?.let { audioRepository.getTrackById(it) }
                    ?: audioRepository.getTracksByMode(savedPrefs.activeMode).firstOrNull()
                    ?: audioRepository.getTracksByMode(AudioMode.SONG).firstOrNull()

                if (lastTrack != null) {
                    val queue = audioRepository.getTracksByMode(lastTrack.mode)
                    prepareQueue(
                        player = player,
                        tracks = queue.ifEmpty { listOf(lastTrack) },
                        startTrack = lastTrack,
                        startPositionMs = savedPrefs.lastPositionMs,
                        autoPlay = false
                    )
                }
            }
        }
    }

    private suspend fun connectController(): MediaController? {
        mediaController?.let { return it }
        return runCatching {
            val token = SessionToken(context, ComponentName(context, AntigravityPlaybackService::class.java))
            val controller = MediaController.Builder(context, token).buildAsync().await()
            controller.addListener(playerListener)
            mediaController = controller
            controller
        }.getOrNull()
    }

    fun playTrack(track: AudioTrack, queue: List<AudioTrack>) {
        scope.launch {
            val player = connectController() ?: return@launch
            val resolvedQueue = if (queue.any { it.id == track.id }) queue else listOf(track) + queue
            prepareQueue(
                player = player,
                tracks = resolvedQueue,
                startTrack = track,
                startPositionMs = 0L,
                autoPlay = true
            )
            preferences.saveLastTrack(track.id, track.mode, 0L)
        }
    }

    fun playFocusAudio(mode: AudioMode) {
        scope.launch {
            val tracks = audioRepository.getTracksByMode(mode)
            val preferred = tracks.firstOrNull { it.isFocusTrack && it.isAvailableOffline }
                ?: tracks.firstOrNull { it.isAvailableOffline }
                ?: tracks.firstOrNull()
                ?: return@launch
            playTrack(preferred, tracks)
        }
    }

    fun togglePlayPause() {
        scope.launch {
            val player = connectController() ?: return@launch
            if (player.mediaItemCount == 0) {
                val fallback = _playbackState.value.currentTrack
                    ?: audioRepository.getTracksByMode(_playbackState.value.activeMode).firstOrNull()
                if (fallback != null) {
                    val queue = audioRepository.getTracksByMode(fallback.mode)
                    playTrack(fallback, queue)
                }
                return@launch
            }
            if (player.isPlaying) {
                player.pause()
                preferences.savePosition(player.currentPosition.coerceAtLeast(0L))
            } else {
                if (player.playbackState == Player.STATE_ENDED) {
                    player.seekTo(0L)
                }
                if (player.playbackState == Player.STATE_IDLE) {
                    player.prepare()
                }
                player.play()
            }
        }
    }

    fun next() {
        scope.launch {
            val player = connectController() ?: return@launch
            if (player.hasNextMediaItem()) {
                player.seekToNextMediaItem()
                player.play()
            } else if (player.mediaItemCount > 0) {
                player.seekTo(0, 0L)
                player.play()
            }
        }
    }

    fun previous() {
        scope.launch {
            val player = connectController() ?: return@launch
            if (player.currentPosition > 3_000L) {
                player.seekTo(0L)
            } else if (player.hasPreviousMediaItem()) {
                player.seekToPreviousMediaItem()
                player.play()
            } else {
                player.seekTo(0L)
            }
        }
    }

    fun seekTo(positionMs: Long) {
        val player = mediaController ?: return
        val clamped = positionMs.coerceAtLeast(0L)
        player.seekTo(clamped)
        _playbackState.update { it.copy(positionMs = clamped) }
        scope.launch { preferences.savePosition(clamped) }
    }

    fun switchMode(mode: AudioMode) {
        _playbackState.update { it.copy(activeMode = mode) }
        scope.launch { preferences.saveActiveMode(mode) }
    }

    fun toggleShuffle() {
        val nextShuffle = !_playbackState.value.shuffleEnabled
        mediaController?.shuffleModeEnabled = nextShuffle
        _playbackState.update { it.copy(shuffleEnabled = nextShuffle) }
        scope.launch { preferences.saveShuffle(nextShuffle) }
    }

    fun cycleRepeatMode() {
        val nextRepeat = _playbackState.value.repeatMode.next()
        mediaController?.repeatMode = nextRepeat.toMedia3RepeatMode()
        _playbackState.update { it.copy(repeatMode = nextRepeat) }
        scope.launch { preferences.saveRepeatMode(nextRepeat) }
    }

    fun clearError() {
        _playbackState.update { it.copy(errorMessage = null) }
    }

    private fun prepareQueue(
        player: MediaController,
        tracks: List<AudioTrack>,
        startTrack: AudioTrack,
        startPositionMs: Long,
        autoPlay: Boolean
    ) {
        val playablePairs = tracks.mapNotNull { track ->
            resolveMediaUri(track)?.let { uri -> track to buildMediaItem(track, uri) }
        }
        if (playablePairs.isEmpty()) {
            _playbackState.update {
                it.copy(
                    currentTrack = startTrack,
                    queue = tracks,
                    errorMessage = "No playable URI found for \"${startTrack.title}\"."
                )
            }
            return
        }

        val startIndex = playablePairs.indexOfFirst { it.first.id == startTrack.id }.coerceAtLeast(0)
        val mediaItems = playablePairs.map { it.second }
        val resolvedTracks = playablePairs.map { it.first }

        _playbackState.update {
            it.copy(
                currentTrack = resolvedTracks[startIndex],
                queue = resolvedTracks,
                activeMode = resolvedTracks[startIndex].mode,
                positionMs = startPositionMs,
                durationMs = resolvedTracks[startIndex].durationMs,
                errorMessage = null
            )
        }

        player.setMediaItems(mediaItems, startIndex, startPositionMs)
        player.prepare()
        if (autoPlay) {
            player.play()
        }
    }

    private fun resolveMediaUri(track: AudioTrack): Uri? {
        val local = track.localPath
        if (!local.isNullOrBlank()) {
            val file = File(local)
            if (file.exists()) return Uri.fromFile(file)
        }
        val content = track.contentUri
        if (!content.isNullOrBlank()) {
            return Uri.parse(content)
        }
        val stream = track.streamUrl
        if (!stream.isNullOrBlank() && isNetworkAvailable()) {
            return Uri.parse(stream)
        }
        val fallback = track.fallbackLocalPath
        if (!fallback.isNullOrBlank()) {
            val file = File(fallback)
            if (file.exists()) return Uri.fromFile(file)
        }
        if (!stream.isNullOrBlank()) {
            return Uri.parse(stream)
        }
        return null
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun buildMediaItem(track: AudioTrack, uri: Uri): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setTitle(track.title)
            .setArtist(track.displayReference)
            .setAlbumTitle(
                if (track.mode == AudioMode.AYAT) {
                    track.surahName ?: "Quranic Ayats"
                } else {
                    "Antigravity Songs"
                }
            )
            .build()

        return MediaItem.Builder()
            .setMediaId(track.id)
            .setUri(uri)
            .setMediaMetadata(metadata)
            .build()
    }

    private fun startProgressUpdates() {
        progressJob?.cancel()
        progressJob = scope.launch {
            var tick = 0
            while (isActive) {
                val player = mediaController
                if (player != null) {
                    val pos = player.currentPosition.coerceAtLeast(0L)
                    val dur = player.duration.takeIf { it > 0L }
                        ?: _playbackState.value.currentTrack?.durationMs
                        ?: 0L
                    _playbackState.update {
                        it.copy(
                            positionMs = pos,
                            durationMs = dur,
                            isPlaying = player.isPlaying
                        )
                    }
                    if (tick % 5 == 0) {
                        preferences.savePosition(pos)
                    }
                }
                tick++
                delay(400L)
            }
        }
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun RepeatMode.toMedia3RepeatMode(): Int = when (this) {
        RepeatMode.OFF -> Player.REPEAT_MODE_OFF
        RepeatMode.ALL -> Player.REPEAT_MODE_ALL
        RepeatMode.ONE -> Player.REPEAT_MODE_ONE
    }
}
