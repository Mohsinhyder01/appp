package com.antigravity.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.antigravity.app.data.model.AudioMode
import com.antigravity.app.data.model.TaskCategory
import com.antigravity.app.data.model.TaskItem
import com.antigravity.app.data.repository.TaskRepository
import com.antigravity.app.player.AudioController
import com.antigravity.app.player.PlaybackState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class TaskStatusFilter(val label: String) {
    ALL("All"),
    PENDING("Pending"),
    COMPLETED("Done")
}

data class TodoUiState(
    val tasks: List<TaskItem> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: TaskCategory? = null,
    val statusFilter: TaskStatusFilter = TaskStatusFilter.ALL,
    val playbackState: PlaybackState = PlaybackState()
)

class TodoViewModel(
    private val taskRepository: TaskRepository,
    private val audioController: AudioController
) : ViewModel() {

    private data class FilterState(
        val query: String = "",
        val category: TaskCategory? = null,
        val status: TaskStatusFilter = TaskStatusFilter.ALL
    )

    private val filterState = MutableStateFlow(FilterState())

    val uiState: StateFlow<TodoUiState> = combine(
        taskRepository.allTasks,
        filterState,
        audioController.playbackState
    ) { allTasks, filter, playback ->
        val filtered = filterTasks(
            tasks = allTasks,
            query = filter.query,
            category = filter.category,
            status = filter.status
        )
        TodoUiState(
            tasks = filtered,
            searchQuery = filter.query,
            selectedCategory = filter.category,
            statusFilter = filter.status,
            playbackState = playback
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = TodoUiState()
    )

    init {
        viewModelScope.launch {
            taskRepository.ensureSeeded()
        }
    }

    fun onSearchQueryChange(query: String) {
        filterState.update { it.copy(query = query) }
    }

    fun onCategorySelected(category: TaskCategory?) {
        filterState.update {
            it.copy(category = if (it.category == category) null else category)
        }
    }

    fun onStatusFilterSelected(status: TaskStatusFilter) {
        filterState.update { it.copy(status = status) }
    }

    fun saveTask(task: TaskItem) {
        viewModelScope.launch {
            taskRepository.saveTask(task)
        }
    }

    fun toggleTaskCompleted(task: TaskItem) {
        viewModelScope.launch {
            taskRepository.toggleCompleted(task)
        }
    }

    fun deleteTask(task: TaskItem) {
        viewModelScope.launch {
            taskRepository.deleteTask(task)
        }
    }

    fun restoreTask(task: TaskItem) {
        viewModelScope.launch {
            taskRepository.saveTask(task)
        }
    }

    fun clearCompleted() {
        viewModelScope.launch {
            taskRepository.clearCompleted()
        }
    }

    fun startFocusAudio(mode: AudioMode) {
        audioController.playFocusAudio(mode)
    }

    companion object {
        fun filterTasks(
            tasks: List<TaskItem>,
            query: String,
            category: TaskCategory?,
            status: TaskStatusFilter
        ): List<TaskItem> {
            val trimmedQuery = query.trim()
            return tasks.filter { task ->
                val matchesCategory = category == null || task.category == category
                val matchesStatus = when (status) {
                    TaskStatusFilter.ALL -> true
                    TaskStatusFilter.PENDING -> !task.isCompleted
                    TaskStatusFilter.COMPLETED -> task.isCompleted
                }
                val matchesQuery = trimmedQuery.isEmpty() ||
                    task.title.contains(trimmedQuery, ignoreCase = true) ||
                    task.notes.contains(trimmedQuery, ignoreCase = true)
                matchesCategory && matchesStatus && matchesQuery
            }
        }

        fun factory(
            taskRepository: TaskRepository,
            audioController: AudioController
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TodoViewModel(taskRepository, audioController) as T
            }
        }
    }
}
