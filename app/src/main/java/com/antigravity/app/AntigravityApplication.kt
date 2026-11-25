package com.antigravity.app

import android.app.Application
import com.antigravity.app.data.local.AntigravityDatabase
import com.antigravity.app.data.local.PlaybackPreferences
import com.antigravity.app.data.offline.LocalAudioImporter
import com.antigravity.app.data.offline.OfflineAudioSeeder
import com.antigravity.app.data.offline.OfflineDownloadManager
import com.antigravity.app.data.repository.AudioRepository
import com.antigravity.app.data.repository.TaskRepository
import com.antigravity.app.player.AudioController

class AppContainer(application: Application) {
    private val database: AntigravityDatabase by lazy {
        AntigravityDatabase.getInstance(application)
    }

    val playbackPreferences: PlaybackPreferences by lazy {
        PlaybackPreferences(application)
    }

    private val offlineAudioSeeder: OfflineAudioSeeder by lazy {
        OfflineAudioSeeder(application)
    }

    private val offlineDownloadManager: OfflineDownloadManager by lazy {
        OfflineDownloadManager(application, database.audioTrackDao())
    }

    private val localAudioImporter: LocalAudioImporter by lazy {
        LocalAudioImporter(application)
    }

    val taskRepository: TaskRepository by lazy {
        TaskRepository(database.taskDao())
    }

    val audioRepository: AudioRepository by lazy {
        AudioRepository(
            audioTrackDao = database.audioTrackDao(),
            seeder = offlineAudioSeeder,
            downloadManager = offlineDownloadManager,
            localImporter = localAudioImporter
        )
    }

    val audioController: AudioController by lazy {
        AudioController(
            context = application,
            audioRepository = audioRepository,
            preferences = playbackPreferences
        )
    }
}

class AntigravityApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        container.audioController.initialize()
    }
}
