package com.antigravity.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.antigravity.app.data.model.TaskItem
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query(
        """
        SELECT * FROM tasks
        ORDER BY isCompleted ASC,
                 CASE priority
                     WHEN 'HIGH' THEN 3
                     WHEN 'MEDIUM' THEN 2
                     ELSE 1
                 END DESC,
                 COALESCE(dueAtMillis, 9223372036854775807) ASC,
                 createdAtMillis DESC
        """
    )
    fun observeAllTasks(): Flow<List<TaskItem>>

    @Query("SELECT COUNT(*) FROM tasks")
    suspend fun countTasks(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTask(task: TaskItem)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(tasks: List<TaskItem>)

    @Update
    suspend fun updateTask(task: TaskItem)

    @Delete
    suspend fun deleteTask(task: TaskItem)

    @Query("DELETE FROM tasks WHERE isCompleted = 1")
    suspend fun deleteCompletedTasks()
}
