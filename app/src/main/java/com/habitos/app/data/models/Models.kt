package com.habitos.app.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.time.LocalDate

// ---------- Converters ----------
class Converters {
    @TypeConverter fun fromLocalDate(d: LocalDate?): String? = d?.toString()
    @TypeConverter fun toLocalDate(s: String?): LocalDate? = s?.let { LocalDate.parse(it) }
    @TypeConverter fun fromStringList(list: List<String>): String = list.joinToString("|")
    @TypeConverter fun toStringList(s: String): List<String> = if (s.isEmpty()) emptyList() else s.split("|")
}

// ---------- Task ----------
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val notes: String = "",
    val isImportant: Boolean = false,
    val isMyDay: Boolean = false,
    val isCompleted: Boolean = false,
    val dueDate: LocalDate? = null,
    val listId: Long? = null,
    val createdAt: LocalDate = LocalDate.now(),
    val completedAt: LocalDate? = null
)

// ---------- Subtask (Step) ----------
@Entity(tableName = "subtasks")
data class Subtask(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val parentTaskId: Long,
    val title: String,
    val isCompleted: Boolean = false
)

// ---------- Habit ----------
@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val listId: Long? = null,
    val color: String = "#10B981",
    val isArchived: Boolean = false,
    val createdAt: LocalDate = LocalDate.now()
)

// ---------- Habit Check-In ----------
@Entity(tableName = "habit_checkins", primaryKeys = ["habitId", "date"])
data class HabitCheckin(
    val habitId: Long,
    val date: LocalDate
)

// ---------- Custom List ----------
@Entity(tableName = "custom_lists")
data class CustomList(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val colorHex: String = "#10B981",
    val createdAt: LocalDate = LocalDate.now()
)

// ---------- Birthday ----------
@Entity(tableName = "birthdays")
data class Birthday(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String = "",
    val birthDate: LocalDate,
    val reminderEnabled: Boolean = true
)

// ---------- UI helpers ----------
data class TaskWithSubtasks(
    val task: Task,
    val subtasks: List<Subtask>
)

enum class NavTab { MY_DAY, HABIT_MATRIX, BIRTHDAYS, PLANNED, LISTS }
