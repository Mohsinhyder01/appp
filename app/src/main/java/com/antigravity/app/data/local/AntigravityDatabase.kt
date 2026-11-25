package com.antigravity.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.antigravity.app.data.model.AudioTrack
import com.antigravity.app.data.model.TaskItem

@Database(
    entities = [TaskItem::class, AudioTrack::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AntigravityDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun audioTrackDao(): AudioTrackDao

    companion object {
        @Volatile
        private var INSTANCE: AntigravityDatabase? = null

        fun getInstance(context: Context): AntigravityDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AntigravityDatabase::class.java,
                    "antigravity.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
