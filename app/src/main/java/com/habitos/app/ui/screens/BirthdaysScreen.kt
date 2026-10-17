package com.habitos.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.habitos.app.data.models.Birthday
import com.habitos.app.ui.components.*
import com.habitos.app.ui.theme.*
import com.habitos.app.ui.viewmodel.MainViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BirthdaysScreen(vm: MainViewModel) {
    val birthdays by vm.birthdays.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showAddDialog by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<Birthday?>(null) }

    val today     = LocalDate.now()
    val yesterday = today.minusDays(1)
    val tomorrow  = today.plusDays(1)

    // Categorize
    val todayBirthdays     = birthdays.filter { it.birthDate.month == today.month && it.birthDate.dayOfMonth == today.dayOfMonth }
    val yesterdayBirthdays = birthdays.filter { it.birthDate.month == yesterday.month && it.birthDate.dayOfMonth == yesterday.dayOfMonth }
    val upcomingBirthdays  = birthdays
        .filter { b ->
            val thisYear = b.birthDate.withYear(today.year)
            val target   = if (thisYear.isBefore(today)) thisYear.plusYears(1) else thisYear
            val days = ChronoUnit.DAYS.between(today, target)
            days in 1..30
        }
        .sortedBy {
            val thisYear = it.birthDate.withYear(today.year)
            val target   = if (thisYear.isBefore(today)) thisYear.plusYears(1) else thisYear
            ChronoUnit.DAYS.between(today, target)
        }

    LaunchedEffect(pendingDelete) {
        pendingDelete?.let { b ->
            val result = snackbarHostState.showSnackbar(
                message = "\"${b.name}\" deleted",
                actionLabel = "Undo",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                vm.addBirthday(b.name, b.phone, b.birthDate)
            }
            pendingDelete = null
        }
    }

    Scaffold(
        containerColor = Background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = AccentEmerald,
                contentColor   = Background
            ) { Icon(Icons.Default.Add, "Add Birthday") }
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
                Row(
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🎂", style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("Birthdays", style = MaterialTheme.typography.headlineMedium, color = OnSurface)
                        Text("${birthdays.size} friends tracked", style = MaterialTheme.typography.bodySmall, color = OnSurfaceMuted)
                    }
                }
            }

            // Yesterday (missed)
            if (yesterdayBirthdays.isNotEmpty()) {
                item { SectionHeader("🚨 Missed Yesterday", yesterdayBirthdays.size) }
                items(yesterdayBirthdays, key = { "y_${it.id}" }) { b ->
                    BirthdayCard(
                        birthday = b,
                        label    = "🚨 YESTERDAY - Missed!",
                        labelColor = Rose,
                        context  = context,
                        onDelete = { vm.deleteBirthday(b); pendingDelete = b }
                    )
                }
            }

            // Today
            if (todayBirthdays.isNotEmpty()) {
                item { SectionHeader("🎉 Today", todayBirthdays.size) }
                items(todayBirthdays, key = { "t_${it.id}" }) { b ->
                    BirthdayCard(
                        birthday = b,
                        label    = "🎂 TODAY!",
                        labelColor = AccentEmerald,
                        context  = context,
                        onDelete = { vm.deleteBirthday(b); pendingDelete = b }
                    )
                }
            }

            // Upcoming
            if (upcomingBirthdays.isNotEmpty()) {
                item { SectionHeader("📅 Upcoming (30 days)", upcomingBirthdays.size) }
                items(upcomingBirthdays, key = { "u_${it.id}" }) { b ->
                    val thisYear = b.birthDate.withYear(today.year)
                    val target   = if (thisYear.isBefore(today)) thisYear.plusYears(1) else thisYear
                    val days     = ChronoUnit.DAYS.between(today, target)
                    BirthdayCard(
                        birthday = b,
                        label    = "in $days day${if (days == 1L) "" else "s"}",
                        labelColor = Amber,
                        context  = context,
                        onDelete = { vm.deleteBirthday(b); pendingDelete = b }
                    )
                }
            }

            // All birthdays
            val others = birthdays.filter { b ->
                b !in todayBirthdays && b !in yesterdayBirthdays && b !in upcomingBirthdays
            }.sortedWith(compareBy({ it.birthDate.monthValue }, { it.birthDate.dayOfMonth }))

            if (others.isNotEmpty()) {
                item { SectionHeader("All Birthdays", others.size) }
                items(others, key = { "a_${it.id}" }) { b ->
                    val dateStr = b.birthDate.format(DateTimeFormatter.ofPattern("MMM d"))
                    BirthdayCard(
                        birthday = b,
                        label    = dateStr,
                        labelColor = OnSurfaceMuted,
                        context  = context,
                        onDelete = { vm.deleteBirthday(b); pendingDelete = b }
                    )
                }
            }

            if (birthdays.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🎂", style = MaterialTheme.typography.headlineLarge)
                            Spacer(Modifier.height(8.dp))
                            Text("No birthdays tracked yet", style = MaterialTheme.typography.titleMedium, color = OnSurface)
                            Text("Tap + to add your first friend's birthday.", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceMuted, textAlign = TextAlign.Center)
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    if (showAddDialog) {
        AddBirthdayDialog(vm = vm, onDismiss = { showAddDialog = false })
    }
}

@Composable
fun BirthdayCard(
    birthday: Birthday,
    label: String,
    labelColor: androidx.compose.ui.graphics.Color,
    context: Context,
    onDelete: () -> Unit
) {
    HabitOSCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(birthday.name, style = MaterialTheme.typography.titleMedium, color = OnSurface)
                Text(
                    label,
                    style = MaterialTheme.typography.bodySmall,
                    color = labelColor
                )
                if (birthday.phone.isNotBlank()) {
                    Text(birthday.phone, style = MaterialTheme.typography.bodySmall, color = OnSurfaceMuted)
                }
            }

            // WhatsApp wish button
            IconButton(
                onClick = {
                    val msg = generateBirthdayWish(birthday.name)
                    if (birthday.phone.isNotBlank()) {
                        val phone = birthday.phone.replace(Regex("[^0-9+]"), "")
                        try {
                            val intent = Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://api.whatsapp.com/send?phone=$phone&text=${Uri.encode(msg)}"))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            val clip = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clip.setPrimaryClip(ClipData.newPlainText("wish", msg))
                        }
                    } else {
                        val clip = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clip.setPrimaryClip(ClipData.newPlainText("wish", msg))
                    }
                }
            ) {
                Icon(Icons.Default.Message, "Wish", tint = AccentEmerald)
            }

            // Copy wish
            IconButton(
                onClick = {
                    val msg = generateBirthdayWish(birthday.name)
                    val clip = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clip.setPrimaryClip(ClipData.newPlainText("wish", msg))
                }
            ) {
                Icon(Icons.Default.ContentCopy, "Copy Wish", tint = OnSurfaceMuted)
            }

            // Delete
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.DeleteOutline, "Delete", tint = OnSurfaceMuted)
            }
        }
    }
}

fun generateBirthdayWish(name: String): String {
    val wishes = listOf(
        "🎉 Happy Birthday, $name! 🎂 Wishing you a day full of joy, laughter, and everything you love. May this year bring you amazing adventures and endless happiness! 🌟",
        "🎂 Hey $name! It's your special day! 🎉 Hope you're surrounded by love, good vibes, and lots of cake. Have an incredible birthday! ❤️",
        "⭐ Happy Birthday $name! 🎈 May your day be as amazing as you are. Wishing you all the happiness in the world today and always! 🎂",
        "🎉 Many happy returns of the day, $name! 🎂 May this birthday mark the beginning of a wonderful new chapter filled with love, success, and endless smiles! 😊"
    )
    return wishes.random()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBirthdayDialog(vm: MainViewModel, onDismiss: () -> Unit) {
    var name  by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var day   by remember { mutableStateOf("1") }
    var month by remember { mutableStateOf("1") }
    var year  by remember { mutableStateOf("2000") }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = Surface,
        title = { Text("🎂 Add Birthday", color = OnSurface) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("Name") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentEmerald, unfocusedBorderColor = Border,
                        focusedTextColor = OnSurface, unfocusedTextColor = OnSurface, containerColor = Background
                    )
                )
                OutlinedTextField(
                    value = phone, onValueChange = { phone = it },
                    label = { Text("Phone (optional)") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentEmerald, unfocusedBorderColor = Border,
                        focusedTextColor = OnSurface, unfocusedTextColor = OnSurface, containerColor = Background
                    )
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = day, onValueChange = { day = it },
                        label = { Text("Day") }, singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentEmerald, unfocusedBorderColor = Border,
                            focusedTextColor = OnSurface, unfocusedTextColor = OnSurface, containerColor = Background
                        )
                    )
                    OutlinedTextField(
                        value = month, onValueChange = { month = it },
                        label = { Text("Month") }, singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentEmerald, unfocusedBorderColor = Border,
                            focusedTextColor = OnSurface, unfocusedTextColor = OnSurface, containerColor = Background
                        )
                    )
                    OutlinedTextField(
                        value = year, onValueChange = { year = it },
                        label = { Text("Year") }, singleLine = true,
                        modifier = Modifier.weight(1.3f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentEmerald, unfocusedBorderColor = Border,
                            focusedTextColor = OnSurface, unfocusedTextColor = OnSurface, containerColor = Background
                        )
                    )
                }
                if (error.isNotBlank()) Text(error, color = Rose, style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val d = day.toIntOrNull(); val m = month.toIntOrNull(); val y = year.toIntOrNull()
                    if (name.isBlank()) { error = "Name is required"; return@Button }
                    if (d == null || m == null || y == null || d !in 1..31 || m !in 1..12) {
                        error = "Invalid date"; return@Button
                    }
                    try {
                        val date = LocalDate.of(y, m, d)
                        vm.addBirthday(name, phone, date)
                        onDismiss()
                    } catch (e: Exception) { error = "Invalid date" }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald)
            ) { Text("Add", color = Background) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = OnSurfaceMuted) }
        }
    )
}
