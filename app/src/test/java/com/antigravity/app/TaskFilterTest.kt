package com.antigravity.app

import com.antigravity.app.data.model.TaskCategory
import com.antigravity.app.data.model.TaskItem
import com.antigravity.app.data.model.TaskPriority
import com.antigravity.app.ui.screens.TaskStatusFilter
import com.antigravity.app.ui.screens.TodoViewModel
import org.junit.Assert.assertEquals
import org.junit.Test

class TaskFilterTest {

    private val sampleTasks = listOf(
        TaskItem(
            id = "1",
            title = "Read Surah Al-Kahf",
            notes = "Friday reflection",
            priority = TaskPriority.HIGH,
            category = TaskCategory.SPIRITUAL,
            isCompleted = false
        ),
        TaskItem(
            id = "2",
            title = "Finish Kotlin architecture doc",
            notes = "Room & Media3",
            priority = TaskPriority.MEDIUM,
            category = TaskCategory.WORK,
            isCompleted = true
        ),
        TaskItem(
            id = "3",
            title = "Study Compose animations",
            notes = "",
            priority = TaskPriority.LOW,
            category = TaskCategory.STUDY,
            isCompleted = false
        )
    )

    @Test
    fun `filters by status, category, and search query`() {
        val pendingOnly = TodoViewModel.filterTasks(
            tasks = sampleTasks,
            query = "",
            category = null,
            status = TaskStatusFilter.PENDING
        )
        assertEquals(listOf("1", "3"), pendingOnly.map { it.id })

        val workDone = TodoViewModel.filterTasks(
            tasks = sampleTasks,
            query = "media3",
            category = TaskCategory.WORK,
            status = TaskStatusFilter.COMPLETED
        )
        assertEquals(listOf("2"), workDone.map { it.id })
    }
}
