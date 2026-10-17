package com.habitos.app.data.db

import android.content.Context
import androidx.room.*
import com.habitos.app.data.models.*

@Database(
    entities = [Task::class, Subtask::class, Habit::class, HabitCheckin::class, CustomList::class, Birthday::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class HabitDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao

    companion object {
        @Volatile private var INSTANCE: HabitDatabase? = null

        fun getInstance(context: Context): HabitDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context.applicationContext, HabitDatabase::class.java, "habitos_db")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
