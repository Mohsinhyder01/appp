package com.antigravity.app.data.offline

import android.content.Context
import android.media.MediaMetadataRetriever
import com.antigravity.app.data.local.AudioTrackDao
import com.antigravity.app.data.model.AudioTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class OfflineDownloadManager(
    private val context: Context,
    private val audioTrackDao: AudioTrackDao
) {
    private val _downloadingTrackIds = MutableStateFlow<Set<String>>(emptySet())
    val downloadingTrackIds: StateFlow<Set<String>> = _downloadingTrackIds.asStateFlow()

    private fun offlineDir(): File =
        File(context.filesDir, "offline_audio").apply { mkdirs() }

    suspend fun downloadTrack(track: AudioTrack): Result<String> = withContext(Dispatchers.IO) {
        val urlString = track.streamUrl
            ?: return@withContext Result.failure(IllegalStateException("Track has no stream URL"))

        val existingPath = track.localPath
        if (!existingPath.isNullOrBlank() && File(existingPath).exists()) {
            return@withContext Result.success(existingPath)
        }

        _downloadingTrackIds.update { it + track.id }
        val tempFile = File(offlineDir(), "${track.id}.tmp")
        val targetFile = File(offlineDir(), "${track.id}.mp3")

        try {
            val connection = (URL(urlString).openConnection() as HttpURLConnection).apply {
                connectTimeout = 15_000
                readTimeout = 30_000
                requestMethod = "GET"
                instanceFollowRedirects = true
            }
            connection.connect()
            if (connection.responseCode !in 200..299) {
                throw IllegalStateException("HTTP ${connection.responseCode}")
            }

            connection.inputStream.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output, bufferSize = 8192)
                }
            }
            connection.disconnect()

            if (targetFile.exists()) targetFile.delete()
            if (!tempFile.renameTo(targetFile)) {
                tempFile.copyTo(targetFile, overwrite = true)
                tempFile.delete()
            }

            val measuredDuration = readDurationMs(targetFile)
            audioTrackDao.markOfflineReady(
                id = track.id,
                localPath = targetFile.absolutePath,
                downloaded = true,
                durationMs = measuredDuration
            )
            Result.success(targetFile.absolutePath)
        } catch (t: Throwable) {
            tempFile.delete()
            Result.failure(t)
        } finally {
            _downloadingTrackIds.update { it - track.id }
        }
    }

    suspend fun downloadAll(tracks: List<AudioTrack>): Int {
        var successCount = 0
        for (track in tracks) {
            if (!track.isAvailableOffline && !track.streamUrl.isNullOrBlank()) {
                if (downloadTrack(track).isSuccess) {
                    successCount++
                }
            }
        }
        return successCount
    }

    private fun readDurationMs(file: File): Long {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(file.absolutePath)
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
        } catch (_: Throwable) {
            0L
        } finally {
            runCatching { retriever.release() }
        }
    }
}
