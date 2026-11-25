package com.antigravity.app.data.repository

import android.net.Uri
import com.antigravity.app.data.local.AudioTrackDao
import com.antigravity.app.data.model.AudioMode
import com.antigravity.app.data.model.AudioTrack
import com.antigravity.app.data.offline.DeviceAudioCandidate
import com.antigravity.app.data.offline.LocalAudioImporter
import com.antigravity.app.data.offline.OfflineAudioSeeder
import com.antigravity.app.data.offline.OfflineDownloadManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

class AudioRepository(
    private val audioTrackDao: AudioTrackDao,
    private val seeder: OfflineAudioSeeder,
    private val downloadManager: OfflineDownloadManager,
    private val localImporter: LocalAudioImporter
) {
    val allTracks: Flow<List<AudioTrack>> = audioTrackDao.observeAllTracks()
    val downloadingTrackIds: StateFlow<Set<String>> = downloadManager.downloadingTrackIds

    fun observeTracksByMode(mode: AudioMode): Flow<List<AudioTrack>> =
        audioTrackDao.observeTracksByMode(mode)

    suspend fun getTracksByMode(mode: AudioMode): List<AudioTrack> =
        audioTrackDao.getTracksByMode(mode)

    suspend fun getTrackById(id: String): AudioTrack? =
        audioTrackDao.getTrackById(id)

    suspend fun ensureSeededOffline() {
        val songPaths = seeder.ensureStarterSongsSeeded()
        val ayatCompanionPath = seeder.ensureAyatOfflineCompanionSeeded()
        if (audioTrackDao.countTracks() == 0) {
            val seededSongs = SeedCatalog.starterSongs.map { song ->
                val path = songPaths[song.id]
                song.copy(localPath = path, isDownloadedOffline = path != null)
            }
            val seededAyats = SeedCatalog.starterAyats.map { ayat ->
                ayat.copy(fallbackLocalPath = ayatCompanionPath)
            }
            audioTrackDao.insertAll(seededSongs + seededAyats)
        } else {
            // Repair starter song local paths if cleared/reinstalled
            for ((trackId, path) in songPaths) {
                val existing = audioTrackDao.getTrackById(trackId)
                if (existing != null && (existing.localPath.isNullOrBlank() || !File(existing.localPath).exists())) {
                    audioTrackDao.markOfflineReady(trackId, path, downloaded = true)
                }
            }
            // Ensure starter Ayats retain a valid offline companion fallback path
            for (ayat in SeedCatalog.starterAyats) {
                val existing = audioTrackDao.getTrackById(ayat.id)
                if (existing != null && (existing.fallbackLocalPath.isNullOrBlank() || !File(existing.fallbackLocalPath).exists())) {
                    audioTrackDao.upsertTrack(existing.copy(fallbackLocalPath = ayatCompanionPath))
                }
            }
        }
    }

    suspend fun downloadTrackOffline(track: AudioTrack): Result<String> =
        downloadManager.downloadTrack(track)

    suspend fun downloadAllPendingAyats(): Int {
        val ayats = audioTrackDao.getTracksByMode(AudioMode.AYAT)
        return downloadManager.downloadAll(ayats)
    }

    suspend fun importLocalAudioUris(
        uris: List<Uri>,
        mode: AudioMode,
        surahName: String? = null,
        ayatNumber: Int? = null
    ): List<AudioTrack> {
        val imported = mutableListOf<AudioTrack>()
        for ((idx, uri) in uris.withIndex()) {
            val numberedAyat = if (ayatNumber != null) ayatNumber + idx else null
            localImporter.importFromUri(uri, mode, surahName, numberedAyat).onSuccess { track ->
                audioTrackDao.upsertTrack(track)
                imported += track
            }
        }
        return imported
    }

    suspend fun scanDeviceAudio(): List<DeviceAudioCandidate> =
        localImporter.scanDeviceAudio()

    suspend fun addDeviceCandidates(candidates: List<DeviceAudioCandidate>, mode: AudioMode): Int {
        val tracks = candidates.map { localImporter.fromMediaStoreCandidate(it, mode) }
        audioTrackDao.insertAll(tracks)
        return tracks.size
    }

    suspend fun deleteTrack(track: AudioTrack) {
        if (track.isUserImported && !track.localPath.isNullOrBlank()) {
            runCatching { File(track.localPath).delete() }
        }
        audioTrackDao.deleteTrack(track)
    }
}
