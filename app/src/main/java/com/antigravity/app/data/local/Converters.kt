package com.antigravity.app.data.local

import androidx.room.TypeConverter
import com.antigravity.app.data.model.AudioMode
import com.antigravity.app.data.model.TaskCategory
import com.antigravity.app.data.model.TaskPriority

class Converters {
    @TypeConverter
    fun fromAudioMode(value: AudioMode): String = value.name

    @TypeConverter
    fun toAudioMode(value: String): AudioMode =
        runCatching { AudioMode.valueOf(value) }.getOrDefault(AudioMode.SONG)

    @TypeConverter
    fun fromTaskPriority(value: TaskPriority): String = value.name

    @TypeConverter
    fun toTaskPriority(value: String): TaskPriority =
        runCatching { TaskPriority.valueOf(value) }.getOrDefault(TaskPriority.MEDIUM)

    @TypeConverter
    fun fromTaskCategory(value: TaskCategory): String = value.name

    @TypeConverter
    fun toTaskCategory(value: String): TaskCategory =
        runCatching { TaskCategory.valueOf(value) }.getOrDefault(TaskCategory.PERSONAL)
}
