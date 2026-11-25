package com.antigravity.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.antigravity.app.data.model.AudioMode
import com.antigravity.app.data.model.AudioTrack
import kotlinx.coroutines.flow.Flow

@Dao
interface AudioTrackDao {
    @Query("SELECT * FROM audio_tracks ORDER BY sortOrder ASC, addedAt DESC")
    fun observeAllTracks(): Flow<List<AudioTrack>>

    @Query("SELECT * FROM audio_tracks WHERE mode = :mode ORDER BY sortOrder ASC, addedAt DESC")
    fun observeTracksByMode(mode: AudioMode): Flow<List<AudioTrack>>

    @Query("SELECT * FROM audio_tracks WHERE id = :id LIMIT 1")
    suspend fun getTrackById(id: String): AudioTrack?

    @Query("SELECT * FROM audio_tracks WHERE mode = :mode ORDER BY sortOrder ASC, addedAt DESC")
    suspend fun getTracksByMode(mode: AudioMode): List<AudioTrack>

    @Query("SELECT COUNT(*) FROM audio_tracks")
    suspend fun countTracks(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTrack(track: AudioTrack)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(tracks: List<AudioTrack>)

    @Query("UPDATE audio_tracks SET localPath = :localPath, isDownloadedOffline = :downloaded, durationMs = CASE WHEN :durationMs > 0 THEN :durationMs ELSE durationMs END WHERE id = :id")
    suspend fun markOfflineReady(id: String, localPath: String, downloaded: Boolean, durationMs: Long = 0L)

    @Delete
    suspend fun deleteTrack(track: AudioTrack)
}
