package com.habitos.app.data.db

import androidx.room.*
import com.habitos.app.data.models.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface HabitDao {
    // ---------- Tasks ----------
    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun allTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE isMyDay = 1 AND isCompleted = 0 ORDER BY isImportant DESC")
    fun myDayTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE isMyDay = 1 ORDER BY isImportant DESC, createdAt DESC")
    fun allMyDayTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE dueDate IS NOT NULL ORDER BY dueDate ASC")
    fun plannedTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE listId = :listId ORDER BY isImportant DESC, createdAt DESC")
    fun tasksByList(listId: Long): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 AND isMyDay = 0 ORDER BY createdAt DESC LIMIT 20")
    fun suggestions(): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun taskById(id: Long): Task?

    // ---------- Subtasks ----------
    @Query("SELECT * FROM subtasks WHERE parentTaskId = :taskId ORDER BY id ASC")
    fun subtasksByTask(taskId: Long): Flow<List<Subtask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubtask(subtask: Subtask): Long

    @Update
    suspend fun updateSubtask(subtask: Subtask)

    @Delete
    suspend fun deleteSubtask(subtask: Subtask)

    @Query("DELETE FROM subtasks WHERE parentTaskId = :taskId")
    suspend fun deleteSubtasksByTask(taskId: Long)

    // ---------- Habits ----------
    @Query("SELECT * FROM habits WHERE isArchived = 0 ORDER BY createdAt ASC")
    fun allHabits(): Flow<List<Habit>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit): Long

    @Update
    suspend fun updateHabit(habit: Habit)

    @Delete
    suspend fun deleteHabit(habit: Habit)

    // ---------- Habit Check-Ins ----------
    @Query("SELECT * FROM habit_checkins WHERE date BETWEEN :start AND :end")
    fun checkinsForRange(start: LocalDate, end: LocalDate): Flow<List<HabitCheckin>>

    @Query("SELECT * FROM habit_checkins WHERE habitId = :habitId ORDER BY date ASC")
    fun checkinsForHabit(habitId: Long): Flow<List<HabitCheckin>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckin(checkin: HabitCheckin)

    @Delete
    suspend fun deleteCheckin(checkin: HabitCheckin)

    @Query("SELECT COUNT(*) FROM habit_checkins WHERE date = :date")
    suspend fun checkinCountForDate(date: LocalDate): Int

    @Query("SELECT COUNT(*) FROM habits WHERE isArchived = 0")
    suspend fun totalActiveHabits(): Int

    // ---------- Custom Lists ----------
    @Query("SELECT * FROM custom_lists ORDER BY createdAt ASC")
    fun allLists(): Flow<List<CustomList>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(list: CustomList): Long

    @Update
    suspend fun updateList(list: CustomList)

    @Delete
    suspend fun deleteList(list: CustomList)

    // ---------- Birthdays ----------
    @Query("SELECT * FROM birthdays ORDER BY birthDate ASC")
    fun allBirthdays(): Flow<List<Birthday>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBirthday(birthday: Birthday): Long

    @Update
    suspend fun updateBirthday(birthday: Birthday)

    @Delete
    suspend fun deleteBirthday(birthday: Birthday)

    // ---------- Clear all ----------
    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()

    @Query("DELETE FROM subtasks")
    suspend fun deleteAllSubtasks()

    @Query("DELETE FROM habits")
    suspend fun deleteAllHabits()

    @Query("DELETE FROM habit_checkins")
    suspend fun deleteAllCheckins()

    @Query("DELETE FROM custom_lists")
    suspend fun deleteAllLists()

    @Query("DELETE FROM birthdays")
    suspend fun deleteAllBirthdays()
}
