package com.antigravity.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.antigravity.app.data.model.AudioMode
import com.antigravity.app.data.model.DailyAyat
import com.antigravity.app.data.model.TaskItem
import com.antigravity.app.data.repository.AudioRepository
import com.antigravity.app.data.repository.SeedCatalog
import com.antigravity.app.data.repository.TaskRepository
import com.antigravity.app.player.AudioController
import com.antigravity.app.player.PlaybackState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val pendingTodayTasks: List<TaskItem> = emptyList(),
    val pendingCount: Int = 0,
    val completedCount: Int = 0,
    val dailyAyat: DailyAyat = SeedCatalog.dailyAyatForToday(),
    val playbackState: PlaybackState = PlaybackState()
)

class HomeViewModel(
    private val taskRepository: TaskRepository,
    private val audioRepository: AudioRepository,
    private val audioController: AudioController
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        taskRepository.allTasks,
        audioController.playbackState
    ) { tasks, playback ->
        val pending = tasks.filter { !it.isCompleted }
        val completed = tasks.count { it.isCompleted }
        HomeUiState(
            pendingTodayTasks = pending.take(4),
            pendingCount = pending.size,
            completedCount = completed,
            dailyAyat = SeedCatalog.dailyAyatForToday(),
            playbackState = playback
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = HomeUiState()
    )

    init {
        viewModelScope.launch {
            taskRepository.ensureSeeded()
        }
    }

    fun toggleTaskCompleted(task: TaskItem) {
        viewModelScope.launch {
            taskRepository.toggleCompleted(task)
        }
    }

    fun playDailyAyat(trackId: String) {
        viewModelScope.launch {
            val ayats = audioRepository.getTracksByMode(AudioMode.AYAT)
            val target = ayats.firstOrNull { it.id == trackId } ?: ayats.firstOrNull() ?: return@launch
            audioController.playTrack(target, ayats)
        }
    }

    fun togglePlayPause() {
        audioController.togglePlayPause()
    }

    companion object {
        fun factory(
            taskRepository: TaskRepository,
            audioRepository: AudioRepository,
            audioController: AudioController
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HomeViewModel(taskRepository, audioRepository, audioController) as T
            }
        }
    }
}
