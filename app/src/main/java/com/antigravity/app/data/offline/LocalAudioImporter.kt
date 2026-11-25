package com.antigravity.app.data.offline

import android.content.ContentUris
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import com.antigravity.app.data.model.AudioMode
import com.antigravity.app.data.model.AudioTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

data class DeviceAudioCandidate(
    val contentUri: Uri,
    val title: String,
    val artist: String,
    val durationMs: Long
)

class LocalAudioImporter(private val context: Context) {

    private fun offlineDir(): File =
        File(context.filesDir, "offline_audio").apply { mkdirs() }

    /**
     * Copies a picked local document URI into app-private offline storage so it stays
     * playable offline even if external permissions or source paths change.
     */
    suspend fun importFromUri(
        uri: Uri,
        mode: AudioMode,
        surahName: String? = null,
        ayatNumber: Int? = null
    ): Result<AudioTrack> = withContext(Dispatchers.IO) {
        runCatching {
            val id = "local_${UUID.randomUUID()}"
            val displayName = resolveDisplayName(uri) ?: "Imported Audio"
            val ext = displayName.substringAfterLast('.', "mp3").lowercase().take(5)
            val targetFile = File(offlineDir(), "$id.$ext")

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output, bufferSize = 8192)
                }
            } ?: throw IllegalStateException("Unable to read selected audio file")

            val metadata = extractMetadata(targetFile.absolutePath)
            val cleanTitle = metadata.title?.takeIf { it.isNotBlank() }
                ?: displayName.substringBeforeLast('.')
            val cleanSubtitle = metadata.artist?.takeIf { it.isNotBlank() }
                ?: if (mode == AudioMode.AYAT) "Local Recitation" else "Local Device Audio"

            AudioTrack(
                id = id,
                mode = mode,
                title = if (mode == AudioMode.AYAT && !surahName.isNullOrBlank() && ayatNumber != null) {
                    "$surahName • Ayat $ayatNumber"
                } else {
                    cleanTitle
                },
                subtitle = cleanSubtitle,
                durationMs = metadata.durationMs,
                localPath = targetFile.absolutePath,
                contentUri = uri.toString(),
                surahName = if (mode == AudioMode.AYAT) surahName?.takeIf { it.isNotBlank() } else null,
                ayatNumber = if (mode == AudioMode.AYAT) ayatNumber else null,
                isFocusTrack = mode == AudioMode.SONG,
                isUserImported = true,
                isDownloadedOffline = true,
                sortOrder = 0
            )
        }
    }

    /**
     * Queries MediaStore for offline music/recitation files already stored on the device.
     */
    suspend fun scanDeviceAudio(limit: Int = 60): List<DeviceAudioCandidate> = withContext(Dispatchers.IO) {
        val results = mutableListOf<DeviceAudioCandidate>()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 OR ${MediaStore.Audio.Media.DURATION} > 3000"
        val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"

        runCatching {
            context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

                while (cursor.moveToNext() && results.size < limit) {
                    val id = cursor.getLong(idCol)
                    val title = cursor.getString(titleCol)?.takeIf { it.isNotBlank() } ?: "Audio Track"
                    val artist = cursor.getString(artistCol)
                        ?.takeUnless { it.equals("<unknown>", ignoreCase = true) }
                        ?: "Device Storage"
                    val duration = cursor.getLong(durationCol)
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    )
                    results += DeviceAudioCandidate(
                        contentUri = contentUri,
                        title = title,
                        artist = artist,
                        durationMs = duration
                    )
                }
            }
        }
        results
    }

    fun fromMediaStoreCandidate(candidate: DeviceAudioCandidate, mode: AudioMode): AudioTrack {
        return AudioTrack(
            id = "ms_${candidate.contentUri.lastPathSegment ?: UUID.randomUUID()}",
            mode = mode,
            title = candidate.title,
            subtitle = candidate.artist,
            durationMs = candidate.durationMs,
            contentUri = candidate.contentUri.toString(),
            isFocusTrack = mode == AudioMode.SONG,
            isUserImported = true,
            isDownloadedOffline = true,
            sortOrder = 0
        )
    }

    private fun resolveDisplayName(uri: Uri): String? {
        return runCatching {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (idx >= 0) cursor.getString(idx) else null
                    } else null
                }
        }.getOrNull()
    }

    private data class ExtractedMeta(
        val title: String?,
        val artist: String?,
        val durationMs: Long
    )

    private fun extractMetadata(filePath: String): ExtractedMeta {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(filePath)
            val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull() ?: 0L
            ExtractedMeta(title, artist, duration)
        } catch (_: Throwable) {
            ExtractedMeta(null, null, 0L)
        } finally {
            runCatching { retriever.release() }
        }
    }
}
