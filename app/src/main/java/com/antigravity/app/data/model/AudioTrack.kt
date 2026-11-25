package com.antigravity.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audio_tracks")
data class AudioTrack(
    @PrimaryKey val id: String,
    val mode: AudioMode,
    val title: String,
    val subtitle: String,
    val durationMs: Long = 0L,
    val streamUrl: String? = null,
    val localPath: String? = null,
    val fallbackLocalPath: String? = null,
    val contentUri: String? = null,
    val surahNumber: Int? = null,
    val surahName: String? = null,
    val surahNameArabic: String? = null,
    val ayatNumber: Int? = null,
    val arabicText: String? = null,
    val translationText: String? = null,
    val isFocusTrack: Boolean = false,
    val isUserImported: Boolean = false,
    val isDownloadedOffline: Boolean = false,
    val sortOrder: Int = 0,
    val addedAt: Long = System.currentTimeMillis()
) {
    val isAvailableOffline: Boolean
        get() = isDownloadedOffline || !localPath.isNullOrBlank() || !contentUri.isNullOrBlank()

    val displayReference: String
        get() = if (mode == AudioMode.AYAT && surahName != null && ayatNumber != null) {
            "$surahName • Ayat $ayatNumber"
        } else {
            subtitle
        }
}
