package com.antigravity.app.data.repository

import com.antigravity.app.data.local.TaskDao
import com.antigravity.app.data.model.TaskItem
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {
    val allTasks: Flow<List<TaskItem>> = taskDao.observeAllTasks()

    suspend fun ensureSeeded() {
        if (taskDao.countTasks() == 0) {
            taskDao.insertAll(SeedCatalog.starterTasks())
        }
    }

    suspend fun saveTask(task: TaskItem) {
        taskDao.upsertTask(task)
    }

    suspend fun toggleCompleted(task: TaskItem) {
        val nowCompleted = !task.isCompleted
        taskDao.updateTask(
            task.copy(
                isCompleted = nowCompleted,
                completedAtMillis = if (nowCompleted) System.currentTimeMillis() else null
            )
        )
    }

    suspend fun deleteTask(task: TaskItem) {
        taskDao.deleteTask(task)
    }

    suspend fun clearCompleted() {
        taskDao.deleteCompletedTasks()
    }
}
