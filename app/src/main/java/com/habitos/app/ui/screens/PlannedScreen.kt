package com.habitos.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.habitos.app.data.models.Task
import com.habitos.app.ui.components.*
import com.habitos.app.ui.theme.*
import com.habitos.app.ui.viewmodel.MainViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun PlannedScreen(vm: MainViewModel) {
    val plannedTasks by vm.plannedTasks.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTask by remember { mutableStateOf<Task?>(null) }
    var pendingDelete by remember { mutableStateOf<Task?>(null) }
    val today = LocalDate.now()

    LaunchedEffect(pendingDelete) {
        pendingDelete?.let { t ->
            val result = snackbarHostState.showSnackbar(
                message = "\"${t.title}\" deleted",
                actionLabel = "Undo",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                vm.addTask(t.title, t.isMyDay, t.isImportant, t.dueDate, t.listId)
            }
            pendingDelete = null
        }
    }

    val overdue   = plannedTasks.filter { it.dueDate != null && it.dueDate.isBefore(today) && !it.isCompleted }
    val todayList = plannedTasks.filter { it.dueDate == today }
    val upcoming  = plannedTasks.filter { it.dueDate != null && it.dueDate.isAfter(today) }

    Scaffold(
        containerColor = Background,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CalendarMonth, null, tint = AccentEmerald, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Planned", style = MaterialTheme.typography.headlineMedium, color = OnSurface)
                }
            }

            if (overdue.isNotEmpty()) {
                item { SectionHeader("🚨 Overdue", overdue.size) }
                items(overdue, key = { "o_${it.id}" }) { task ->
                    HabitOSCard {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                task.dueDate?.format(DateTimeFormatter.ofPattern("MMM d")) ?: "",
                                style = MaterialTheme.typography.labelSmall,
                                color = Rose
                            )
                        }
                        TaskRow(
                            task = task,
                            onToggleComplete = { vm.toggleTaskComplete(task) },
                            onToggleImportant = { vm.toggleTaskImportant(task) },
                            onDelete = { vm.deleteTask(task); pendingDelete = task },
                            onClick = { selectedTask = task }
                        )
                    }
                }
            }

            if (todayList.isNotEmpty()) {
                item { SectionHeader("📌 Today", todayList.size) }
                items(todayList, key = { "t_${it.id}" }) { task ->
                    HabitOSCard {
                        TaskRow(
                            task = task,
                            onToggleComplete = { vm.toggleTaskComplete(task) },
                            onToggleImportant = { vm.toggleTaskImportant(task) },
                            onDelete = { vm.deleteTask(task); pendingDelete = task },
                            onClick = { selectedTask = task }
                        )
                    }
                }
            }

            if (upcoming.isNotEmpty()) {
                item { SectionHeader("📅 Upcoming", upcoming.size) }
                items(upcoming, key = { "u_${it.id}" }) { task ->
                    HabitOSCard {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                        ) {
                            Text(
                                task.dueDate?.format(DateTimeFormatter.ofPattern("MMM d, yyyy")) ?: "",
                                style = MaterialTheme.typography.labelSmall,
                                color = Amber
                            )
                        }
                        TaskRow(
                            task = task,
                            onToggleComplete = { vm.toggleTaskComplete(task) },
                            onToggleImportant = { vm.toggleTaskImportant(task) },
                            onDelete = { vm.deleteTask(task); pendingDelete = task },
                            onClick = { selectedTask = task }
                        )
                    }
                }
            }

            if (plannedTasks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📅", style = MaterialTheme.typography.headlineLarge)
                            Spacer(Modifier.height(8.dp))
                            Text("No planned tasks", style = MaterialTheme.typography.titleMedium, color = OnSurface)
                            Text("Add tasks with due dates from My Day.", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceMuted, textAlign = TextAlign.Center)
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }

    selectedTask?.let { task ->
        TaskDetailSheet(task = task, vm = vm, onDismiss = { selectedTask = null })
    }
}
