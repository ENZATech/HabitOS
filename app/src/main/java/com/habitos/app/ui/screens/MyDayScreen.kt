package com.habitos.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.habitos.app.data.models.Task
import com.habitos.app.ui.components.*
import com.habitos.app.ui.theme.*
import com.habitos.app.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyDayScreen(vm: MainViewModel) {
    val tasks by vm.myDayTasks.collectAsState()
    val allTasks by vm.allMyDayTasks.collectAsState()
    val suggestions by vm.suggestions.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var newTaskTitle by remember { mutableStateOf("") }
    var showSuggestions by remember { mutableStateOf(false) }
    var selectedTask by remember { mutableStateOf<Task?>(null) }
    var pendingDelete by remember { mutableStateOf<Task?>(null) }

    val today = LocalDate.now()
    val dateStr = today.format(DateTimeFormatter.ofPattern("EEEE, MMM d"))
    val done   = allTasks.count { it.isCompleted }
    val total  = allTasks.size
    val pct    = if (total > 0) (done * 100) / total else 0

    // Undo delete logic
    LaunchedEffect(pendingDelete) {
        if (pendingDelete != null) {
            val task = pendingDelete!!
            val result = snackbarHostState.showSnackbar(
                message  = "\"${task.title}\" deleted",
                actionLabel = "Undo",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                vm.addTask(task.title, task.isMyDay, task.isImportant, task.dueDate, task.listId)
            }
            pendingDelete = null
        }
    }

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
            // Header
            item {
                Column(modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.WbSunny, null, tint = Amber, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("My Day", style = MaterialTheme.typography.headlineMedium, color = OnSurface)
                    }
                    Text(dateStr, style = MaterialTheme.typography.bodyMedium, color = OnSurfaceMuted)
                }
            }

            // Progress card
            item {
                HabitOSCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "$done / $total tasks done",
                                style = MaterialTheme.typography.titleLarge,
                                color = OnSurface
                            )
                            Text(
                                "$pct% complete",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceMuted
                            )
                        }
                        CircularProgressIndicator(
                            progress = { if (total > 0) done.toFloat() / total else 0f },
                            modifier = Modifier.size(48.dp),
                            color    = AccentEmerald,
                            trackColor = Border
                        )
                    }
                    if (total > 0) {
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { if (total > 0) done.toFloat() / total else 0f },
                            modifier = Modifier.fillMaxWidth().height(4.dp),
                            color    = AccentEmerald,
                            trackColor = Border
                        )
                    }
                }
            }

            // Add task input
            item {
                AddItemTextField(
                    value         = newTaskTitle,
                    onValueChange = { newTaskTitle = it },
                    onDone        = {
                        if (newTaskTitle.isNotBlank()) {
                            vm.addTask(newTaskTitle.trim(), isMyDay = true)
                            newTaskTitle = ""
                        }
                    },
                    placeholder = "Add a task to My Day…"
                )
            }

            // Suggestions toggle
            if (suggestions.isNotEmpty()) {
                item {
                    TextButton(
                        onClick = { showSuggestions = !showSuggestions },
                        colors  = ButtonDefaults.textButtonColors(contentColor = AccentEmerald)
                    ) {
                        Icon(Icons.Default.Lightbulb, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(if (showSuggestions) "Hide Suggestions" else "Suggestions (${suggestions.size})")
                    }
                }
                if (showSuggestions) {
                    item {
                        HabitOSCard {
                            Text("Suggested Tasks", style = MaterialTheme.typography.titleMedium, color = OnSurfaceMuted)
                            Spacer(Modifier.height(8.dp))
                            suggestions.take(5).forEach { t ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { vm.toggleMyDay(t) }
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Add, null, tint = AccentEmerald, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(t.title, color = OnSurface, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                }
                                Divider(color = Border, thickness = 0.5.dp)
                            }
                        }
                    }
                }
            }

            // Active tasks
            val active   = tasks.filter { !it.isCompleted }.sortedByDescending { it.isImportant }
            val completed = tasks.filter { it.isCompleted }

            if (active.isNotEmpty()) {
                item { SectionHeader("Tasks", active.size) }
                items(active, key = { it.id }) { task ->
                    HabitOSCard {
                        TaskRow(
                            task             = task,
                            onToggleComplete = { vm.toggleTaskComplete(task) },
                            onToggleImportant= { vm.toggleTaskImportant(task) },
                            onDelete         = {
                                vm.deleteTask(task)
                                pendingDelete = task
                            },
                            onClick          = { selectedTask = task }
                        )
                    }
                }
            }

            if (completed.isNotEmpty()) {
                item { SectionHeader("Completed", completed.size) }
                items(completed, key = { it.id }) { task ->
                    HabitOSCard {
                        TaskRow(
                            task             = task,
                            onToggleComplete = { vm.toggleTaskComplete(task) },
                            onToggleImportant= { vm.toggleTaskImportant(task) },
                            onDelete         = {
                                vm.deleteTask(task)
                                pendingDelete = task
                            },
                            onClick          = { selectedTask = task }
                        )
                    }
                }
            }

            if (allTasks.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("☀️", style = MaterialTheme.typography.headlineLarge)
                            Spacer(Modifier.height(8.dp))
                            Text("Your day is clear!", style = MaterialTheme.typography.titleMedium, color = OnSurface)
                            Text("Add tasks above to get started.", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceMuted)
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }

    // Task detail bottom sheet
    selectedTask?.let { task ->
        TaskDetailSheet(
            task     = task,
            vm       = vm,
            onDismiss= { selectedTask = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailSheet(task: Task, vm: MainViewModel, onDismiss: () -> Unit) {
    val subtasks by vm.subtasksFor(task.id).collectAsState(emptyList())
    var notes by remember(task.id) { mutableStateOf(task.notes) }
    var newStep by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = { onDismiss() },
        containerColor   = Surface,
        contentColor     = OnSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Title
            Text(task.title, style = MaterialTheme.typography.headlineSmall, color = OnSurface)
            Spacer(Modifier.height(16.dp))

            // Steps/Subtasks
            Text("Steps", style = MaterialTheme.typography.titleMedium, color = OnSurfaceMuted)
            Spacer(Modifier.height(8.dp))
            subtasks.forEach { st ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = st.isCompleted,
                        onCheckedChange = { vm.toggleSubtask(st) },
                        colors = CheckboxDefaults.colors(checkedColor = AccentEmerald)
                    )
                    Text(
                        st.title,
                        color = if (st.isCompleted) OnSurfaceDim else OnSurface,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { vm.deleteSubtask(st) }) {
                        Icon(Icons.Default.Close, null, tint = OnSurfaceMuted, modifier = Modifier.size(16.dp))
                    }
                }
                HorizontalDivider(color = Border, thickness = 0.5.dp)
            }
            OutlinedTextField(
                value = newStep,
                onValueChange = { newStep = it },
                placeholder = { Text("Add a step…", color = OnSurfaceDim) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentEmerald,
                    unfocusedBorderColor = Border,
                    focusedTextColor = OnSurface,
                    unfocusedTextColor = OnSurface,
                    containerColor = Background
                ),
                trailingIcon = {
                    IconButton(onClick = {
                        if (newStep.isNotBlank()) { vm.addSubtask(task.id, newStep); newStep = "" }
                    }) { Icon(Icons.Default.Add, null, tint = AccentEmerald) }
                }
            )

            Spacer(Modifier.height(16.dp))

            // Notes
            Text("Notes", style = MaterialTheme.typography.titleMedium, color = OnSurfaceMuted)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = notes,
                onValueChange = {
                    notes = it
                    scope.launch { vm.updateTaskNotes(task, it) }
                },
                placeholder = { Text("Add notes…", color = OnSurfaceDim) },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentEmerald,
                    unfocusedBorderColor = Border,
                    focusedTextColor = OnSurface,
                    unfocusedTextColor = OnSurface,
                    containerColor = Background
                )
            )

            Spacer(Modifier.height(16.dp))

            // Actions row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // My Day toggle
                FilterChip(
                    selected = task.isMyDay,
                    onClick  = { vm.toggleMyDay(task) },
                    label    = { Text("My Day") },
                    leadingIcon = { Icon(Icons.Default.WbSunny, null, Modifier.size(16.dp)) },
                    colors   = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AccentEmerald.copy(alpha = 0.2f),
                        selectedLabelColor     = AccentEmerald
                    )
                )
                // Important toggle
                FilterChip(
                    selected = task.isImportant,
                    onClick  = { vm.toggleTaskImportant(task) },
                    label    = { Text("Important") },
                    leadingIcon = { Icon(Icons.Default.Star, null, Modifier.size(16.dp)) },
                    colors   = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Amber.copy(alpha = 0.2f),
                        selectedLabelColor     = Amber
                    )
                )
            }
        }
    }
}
