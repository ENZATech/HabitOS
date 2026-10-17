package com.habitos.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.habitos.app.HabitOSApp
import com.habitos.app.data.models.CustomList
import com.habitos.app.data.models.Task
import com.habitos.app.ui.components.*
import com.habitos.app.ui.theme.*
import com.habitos.app.ui.viewmodel.MainViewModel

val LIST_COLORS = listOf(
    "#10B981", "#0D9488", "#F59E0B", "#F43F5E",
    "#6366F1", "#8B5CF6", "#EC4899", "#3B82F6", "#F97316"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListsScreen(vm: MainViewModel) {
    val lists by vm.customLists.collectAsState()
    val allTasks by vm.allTasks.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showAddListDialog by remember { mutableStateOf(false) }
    var selectedList by remember { mutableStateOf<CustomList?>(null) }
    var pendingDeleteList by remember { mutableStateOf<CustomList?>(null) }

    // Vault / Clear Data
    var showVaultDialog by remember { mutableStateOf(false) }

    LaunchedEffect(pendingDeleteList) {
        pendingDeleteList?.let { l ->
            val result = snackbarHostState.showSnackbar(
                message = "\"${l.name}\" deleted",
                actionLabel = "Undo",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                vm.addList(l.name, l.colorHex)
            }
            pendingDeleteList = null
        }
    }

    Scaffold(
        containerColor = Background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddListDialog = true },
                containerColor = AccentEmerald,
                contentColor   = Background
            ) { Icon(Icons.Default.Add, "New List") }
        }
    ) { padding ->
        if (selectedList == null) {
            // Lists overview
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.List, null, tint = AccentEmerald, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("My Lists", style = MaterialTheme.typography.headlineMedium, color = OnSurface, modifier = Modifier.weight(1f))
                        // Clear Data Vault button
                        IconButton(onClick = { showVaultDialog = true }) {
                            Icon(Icons.Default.Lock, "Clear Data Vault", tint = Rose)
                        }
                    }
                }

                items(lists, key = { it.id }) { list ->
                    val taskCount = allTasks.count { it.listId == list.id }
                    HabitOSCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ColorDot(list.colorHex, size = 14)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(list.name, style = MaterialTheme.typography.titleMedium, color = OnSurface)
                                Text("$taskCount tasks", style = MaterialTheme.typography.bodySmall, color = OnSurfaceMuted)
                            }
                            IconButton(onClick = { selectedList = list }) {
                                Icon(Icons.Default.ChevronRight, null, tint = OnSurfaceMuted)
                            }
                            IconButton(onClick = { vm.deleteList(list); pendingDeleteList = list }) {
                                Icon(Icons.Default.DeleteOutline, null, tint = OnSurfaceMuted)
                            }
                        }
                    }
                }

                if (lists.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("📚", style = MaterialTheme.typography.headlineLarge)
                                Spacer(Modifier.height(8.dp))
                                Text("No lists yet", style = MaterialTheme.typography.titleMedium, color = OnSurface)
                                Text("Tap + to create your first list.", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceMuted, textAlign = TextAlign.Center)
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(80.dp)) }
            }
        } else {
            // List detail view
            ListDetailView(
                list     = selectedList!!,
                vm       = vm,
                onBack   = { selectedList = null },
                allTasks = allTasks
            )
        }
    }

    if (showAddListDialog) {
        AddListDialog(vm = vm, onDismiss = { showAddListDialog = false })
    }

    if (showVaultDialog) {
        ClearDataVaultDialog(
            vm = vm,
            onDismiss = { showVaultDialog = false }
        )
    }
}

@Composable
fun ListDetailView(
    list: CustomList,
    vm: MainViewModel,
    onBack: () -> Unit,
    allTasks: List<Task>
) {
    val context = LocalContext.current
    val listTasksFlow = remember(list.id) {
        (context.applicationContext as HabitOSApp).repository.tasksByList(list.id)
    }
    val listTasks by listTasksFlow.collectAsState(emptyList())
    var newTask   by remember { mutableStateOf("") }
    var selectedTask by remember { mutableStateOf<Task?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingDelete by remember { mutableStateOf<Task?>(null) }

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

    Scaffold(
        containerColor = Background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = OnSurface)
                }
                ColorDot(list.colorHex, size = 12)
                Spacer(Modifier.width(8.dp))
                Text(list.name, style = MaterialTheme.typography.headlineSmall, color = OnSurface)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                AddItemTextField(
                    value = newTask,
                    onValueChange = { newTask = it },
                    onDone = {
                        if (newTask.isNotBlank()) {
                            vm.addTask(newTask.trim(), isMyDay = false, listId = list.id)
                            newTask = ""
                        }
                    },
                    placeholder = "Add task to ${list.name}…"
                )
            }

            val active = listTasks.filter { !it.isCompleted }
            val done   = listTasks.filter { it.isCompleted }

            if (active.isNotEmpty()) {
                item { SectionHeader("Active", active.size) }
                items(active, key = { it.id }) { task ->
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

            if (done.isNotEmpty()) {
                item { SectionHeader("Completed", done.size) }
                items(done, key = { it.id }) { task ->
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

            if (listTasks.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No tasks in this list yet.", color = OnSurfaceMuted, textAlign = TextAlign.Center)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddListDialog(vm: MainViewModel, onDismiss: () -> Unit) {
    var name     by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf(LIST_COLORS[0]) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = Surface,
        title = { Text("New List", color = OnSurface) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("List name") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentEmerald, unfocusedBorderColor = Border,
                        focusedTextColor = OnSurface, unfocusedTextColor = OnSurface, containerColor = Background
                    )
                )
                Text("Color", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceMuted)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(LIST_COLORS) { color ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(parseColor(color))
                                .border(
                                    width = if (selected == color) 3.dp else 0.dp,
                                    color = if (selected == color) OnSurface else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selected = color }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) { vm.addList(name.trim(), selected); onDismiss() } },
                colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald)
            ) { Text("Create", color = Background) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = OnSurfaceMuted) }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClearDataVaultDialog(vm: MainViewModel, onDismiss: () -> Unit) {
    val vaultPin by vm.vaultPin.collectAsState()
    var pinInput   by remember { mutableStateOf("") }
    var clearInput by remember { mutableStateOf("") }
    var error      by remember { mutableStateOf("") }
    var newPin     by remember { mutableStateOf("") }
    var showPinChange by remember { mutableStateOf(false) }
    var cleared    by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = Surface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lock, null, tint = Rose, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Clear Data Vault", color = Rose)
            }
        },
        text = {
            if (cleared) {
                Text("✅ All data cleared. Fresh start!", color = AccentEmerald)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Enter your 4-digit PIN and type CLEAR to reset all data.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceMuted
                    )
                    OutlinedTextField(
                        value = pinInput, onValueChange = { if (it.length <= 4) pinInput = it },
                        label = { Text("4-digit PIN") }, singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Rose, unfocusedBorderColor = Border,
                            focusedTextColor = OnSurface, unfocusedTextColor = OnSurface, containerColor = Background
                        )
                    )
                    OutlinedTextField(
                        value = clearInput, onValueChange = { clearInput = it },
                        label = { Text("Type CLEAR") }, singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Rose, unfocusedBorderColor = Border,
                            focusedTextColor = OnSurface, unfocusedTextColor = OnSurface, containerColor = Background
                        )
                    )
                    if (error.isNotBlank()) Text(error, color = Rose, style = MaterialTheme.typography.bodySmall)

                    if (!showPinChange) {
                        TextButton(onClick = { showPinChange = true }, colors = ButtonDefaults.textButtonColors(contentColor = OnSurfaceMuted)) {
                            Text("Change PIN", style = MaterialTheme.typography.bodySmall)
                        }
                    } else {
                        OutlinedTextField(
                            value = newPin, onValueChange = { if (it.length <= 4) newPin = it },
                            label = { Text("New 4-digit PIN") }, singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.NumberPassword
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentEmerald, unfocusedBorderColor = Border,
                                focusedTextColor = OnSurface, unfocusedTextColor = OnSurface, containerColor = Background
                            )
                        )
                        Button(
                            onClick = {
                                if (pinInput == vaultPin && newPin.length == 4) {
                                    vm.updatePin(newPin)
                                    showPinChange = false
                                    pinInput = ""
                                    newPin = ""
                                    error = "PIN updated!"
                                } else error = "Wrong current PIN or invalid new PIN"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald)
                        ) { Text("Save PIN", color = Background) }
                    }
                }
            }
        },
        confirmButton = {
            if (!cleared) {
                Button(
                    onClick = {
                        if (pinInput != vaultPin) { error = "Wrong PIN"; return@Button }
                        if (clearInput != "CLEAR") { error = "Type exactly CLEAR in uppercase"; return@Button }
                        vm.clearAllData()
                        cleared = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Rose)
                ) { Text("Clear All Data", color = OnSurface) }
            } else {
                Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald)) {
                    Text("Done", color = Background)
                }
            }
        },
        dismissButton = {
            if (!cleared) TextButton(onClick = onDismiss) { Text("Cancel", color = OnSurfaceMuted) }
        }
    )
}
