package com.habitos.app.ui.viewmodel

import android.app.Application
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.*
import com.habitos.app.HabitOSApp
import com.habitos.app.data.models.*
import com.habitos.app.data.repository.HabitRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import java.time.LocalTime

val android.content.Context.dataStore by preferencesDataStore(name = "habitos_prefs")

val KEY_EOD_HOUR = intPreferencesKey("eod_hour")
val KEY_EOD_MIN  = intPreferencesKey("eod_min")
val KEY_PIN      = stringPreferencesKey("vault_pin")

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repo: HabitRepository = (application as HabitOSApp).repository
    private val ctx = application.applicationContext

    // ---- Tasks ----
    val myDayTasks   = repo.myDayTasks().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allMyDayTasks= repo.allMyDayTasks().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val plannedTasks = repo.plannedTasks().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val suggestions  = repo.suggestions().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allTasks     = repo.allTasks().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addTask(title: String, isMyDay: Boolean = true, isImportant: Boolean = false,
                dueDate: LocalDate? = null, listId: Long? = null) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repo.insertTask(Task(title = title.trim(), isMyDay = isMyDay, isImportant = isImportant,
                dueDate = dueDate, listId = listId))
        }
    }

    fun toggleTaskComplete(task: Task) {
        viewModelScope.launch {
            repo.updateTask(task.copy(
                isCompleted = !task.isCompleted,
                completedAt = if (!task.isCompleted) LocalDate.now() else null
            ))
        }
    }

    fun toggleTaskImportant(task: Task) {
        viewModelScope.launch { repo.updateTask(task.copy(isImportant = !task.isImportant)) }
    }

    fun toggleMyDay(task: Task) {
        viewModelScope.launch { repo.updateTask(task.copy(isMyDay = !task.isMyDay)) }
    }

    fun updateTaskNotes(task: Task, notes: String) {
        viewModelScope.launch { repo.updateTask(task.copy(notes = notes)) }
    }

    fun updateTaskTitle(task: Task, title: String) {
        if (title.isBlank()) return
        viewModelScope.launch { repo.updateTask(task.copy(title = title.trim())) }
    }

    fun updateTaskDueDate(task: Task, date: LocalDate?) {
        viewModelScope.launch { repo.updateTask(task.copy(dueDate = date)) }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repo.deleteSubtasksByTask(task.id)
            repo.deleteTask(task)
        }
    }

    // ---- Subtasks ----
    fun subtasksFor(taskId: Long): Flow<List<Subtask>> = repo.subtasksByTask(taskId)

    fun addSubtask(taskId: Long, title: String) {
        if (title.isBlank()) return
        viewModelScope.launch { repo.insertSubtask(Subtask(parentTaskId = taskId, title = title.trim())) }
    }

    fun toggleSubtask(subtask: Subtask) {
        viewModelScope.launch { repo.updateSubtask(subtask.copy(isCompleted = !subtask.isCompleted)) }
    }

    fun deleteSubtask(subtask: Subtask) {
        viewModelScope.launch { repo.deleteSubtask(subtask) }
    }

    // ---- Habits ----
    val habits = repo.allHabits().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addHabit(name: String, listId: Long? = null, color: String = "#10B981") {
        if (name.isBlank()) return
        viewModelScope.launch { repo.insertHabit(Habit(name = name.trim(), listId = listId, color = color)) }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch { repo.deleteHabit(habit) }
    }

    fun checkinsForRange(start: LocalDate, end: LocalDate): Flow<List<HabitCheckin>> =
        repo.checkinsForRange(start, end)

    fun checkinsForHabit(habitId: Long): Flow<List<HabitCheckin>> =
        repo.checkinsForHabit(habitId)

    fun toggleCheckin(habitId: Long, date: LocalDate, currentlyChecked: Boolean) {
        viewModelScope.launch {
            repo.toggleCheckin(habitId, date, !currentlyChecked)
        }
    }

    // ---- Lists ----
    val customLists = repo.allLists().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addList(name: String, colorHex: String = "#10B981") {
        if (name.isBlank()) return
        viewModelScope.launch { repo.insertList(CustomList(name = name.trim(), colorHex = colorHex)) }
    }

    fun deleteList(list: CustomList) {
        viewModelScope.launch { repo.deleteList(list) }
    }

    // ---- Birthdays ----
    val birthdays = repo.allBirthdays().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addBirthday(name: String, phone: String, date: LocalDate) {
        if (name.isBlank()) return
        viewModelScope.launch { repo.insertBirthday(Birthday(name = name.trim(), phone = phone.trim(), birthDate = date)) }
    }

    fun deleteBirthday(birthday: Birthday) {
        viewModelScope.launch { repo.deleteBirthday(birthday) }
    }

    // ---- DataStore: EOD time + PIN ----
    val eodHour: StateFlow<Int> = ctx.dataStore.data
        .map { it[KEY_EOD_HOUR] ?: 21 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 21)

    val eodMin: StateFlow<Int> = ctx.dataStore.data
        .map { it[KEY_EOD_MIN] ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val vaultPin: StateFlow<String> = ctx.dataStore.data
        .map { it[KEY_PIN] ?: "1234" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "1234")

    fun updateEodTime(hour: Int, min: Int) {
        viewModelScope.launch {
            ctx.dataStore.updateData { prefs ->
                prefs.toMutablePreferences().apply {
                    set(KEY_EOD_HOUR, hour)
                    set(KEY_EOD_MIN, min)
                }
            }
        }
    }

    fun updatePin(newPin: String) {
        viewModelScope.launch {
            ctx.dataStore.updateData { prefs ->
                prefs.toMutablePreferences().apply { set(KEY_PIN, newPin) }
            }
        }
    }

    // ---- Clear Data Vault ----
    fun clearAllData() {
        viewModelScope.launch { (getApplication() as HabitOSApp).repository.clearAll() }
    }

    // ---- Today summary for EOD notification ----
    suspend fun hasPendingWork(): Boolean {
        val today = LocalDate.now()
        val pendingTasks = repo.myDayTasks().first().any { !it.isCompleted }
        val totalHabits = repo.totalActiveHabits()
        val checkedToday = repo.checkinCountForDate(today)
        val pendingHabits = totalHabits > 0 && checkedToday < totalHabits
        val todayBirthdays = repo.allBirthdays().first().any {
            it.birthDate.month == today.month && it.birthDate.dayOfMonth == today.dayOfMonth
        }
        return pendingTasks || pendingHabits || todayBirthdays
    }
}

class MainViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return MainViewModel(application) as T
    }
}
