package com.habitos.app.data.repository

import com.habitos.app.data.db.HabitDao
import com.habitos.app.data.models.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class HabitRepository(private val dao: HabitDao) {

    // Tasks
    fun myDayTasks(): Flow<List<Task>> = dao.myDayTasks()
    fun allMyDayTasks(): Flow<List<Task>> = dao.allMyDayTasks()
    fun plannedTasks(): Flow<List<Task>> = dao.plannedTasks()
    fun tasksByList(listId: Long): Flow<List<Task>> = dao.tasksByList(listId)
    fun suggestions(): Flow<List<Task>> = dao.suggestions()
    fun allTasks(): Flow<List<Task>> = dao.allTasks()
    suspend fun insertTask(task: Task): Long = dao.insertTask(task)
    suspend fun updateTask(task: Task) = dao.updateTask(task)
    suspend fun deleteTask(task: Task) = dao.deleteTask(task)
    suspend fun taskById(id: Long) = dao.taskById(id)

    // Subtasks
    fun subtasksByTask(taskId: Long): Flow<List<Subtask>> = dao.subtasksByTask(taskId)
    suspend fun insertSubtask(subtask: Subtask): Long = dao.insertSubtask(subtask)
    suspend fun updateSubtask(subtask: Subtask) = dao.updateSubtask(subtask)
    suspend fun deleteSubtask(subtask: Subtask) = dao.deleteSubtask(subtask)
    suspend fun deleteSubtasksByTask(taskId: Long) = dao.deleteSubtasksByTask(taskId)

    // Habits
    fun allHabits(): Flow<List<Habit>> = dao.allHabits()
    suspend fun insertHabit(habit: Habit): Long = dao.insertHabit(habit)
    suspend fun updateHabit(habit: Habit) = dao.updateHabit(habit)
    suspend fun deleteHabit(habit: Habit) = dao.deleteHabit(habit)

    // Habit Check-Ins
    fun checkinsForRange(start: LocalDate, end: LocalDate): Flow<List<HabitCheckin>> = dao.checkinsForRange(start, end)
    fun checkinsForHabit(habitId: Long): Flow<List<HabitCheckin>> = dao.checkinsForHabit(habitId)
    suspend fun toggleCheckin(habitId: Long, date: LocalDate, isChecked: Boolean) {
        if (isChecked) dao.insertCheckin(HabitCheckin(habitId, date))
        else dao.deleteCheckin(HabitCheckin(habitId, date))
    }
    suspend fun checkinCountForDate(date: LocalDate) = dao.checkinCountForDate(date)
    suspend fun totalActiveHabits() = dao.totalActiveHabits()

    // Custom Lists
    fun allLists(): Flow<List<CustomList>> = dao.allLists()
    suspend fun insertList(list: CustomList): Long = dao.insertList(list)
    suspend fun updateList(list: CustomList) = dao.updateList(list)
    suspend fun deleteList(list: CustomList) = dao.deleteList(list)

    // Birthdays
    fun allBirthdays(): Flow<List<Birthday>> = dao.allBirthdays()
    suspend fun insertBirthday(b: Birthday): Long = dao.insertBirthday(b)
    suspend fun updateBirthday(b: Birthday) = dao.updateBirthday(b)
    suspend fun deleteBirthday(b: Birthday) = dao.deleteBirthday(b)

    // Clear All
    suspend fun clearAll() {
        dao.deleteAllTasks()
        dao.deleteAllSubtasks()
        dao.deleteAllHabits()
        dao.deleteAllCheckins()
        dao.deleteAllLists()
        dao.deleteAllBirthdays()
    }
}
