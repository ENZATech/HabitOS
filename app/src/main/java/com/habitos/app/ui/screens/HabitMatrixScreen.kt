package com.habitos.app.ui.screens

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitos.app.data.models.Habit
import com.habitos.app.data.models.HabitCheckin
import com.habitos.app.ui.components.*
import com.habitos.app.ui.theme.*
import com.habitos.app.ui.viewmodel.MainViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitMatrixScreen(vm: MainViewModel) {
    val habits by vm.habits.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    // Week navigation
    var weekOffset by remember { mutableStateOf(0) }
    val today = LocalDate.now()
    val weekStart = today.minusDays(today.dayOfWeek.value.toLong() - 1).plusWeeks(weekOffset.toLong())
    val weekDays = (0..6).map { weekStart.plusDays(it.toLong()) }

    val checkins by vm.checkinsForRange(weekStart, weekStart.plusDays(6)).collectAsState(emptyList())
    val checkinSet = remember(checkins) { checkins.map { "${it.habitId}_${it.date}" }.toSet() }

    // Stats
    val totalCells     = habits.size * 7
    val checkedCells   = checkins.size
    val weekPct        = if (totalCells > 0) (checkedCells * 100) / totalCells else 0
    val isPerfectWeek  = totalCells > 0 && checkedCells == totalCells
    val todayChecked   = checkins.count { it.date == today }
    val totalHabits    = habits.size
    val isPerfectDay   = totalHabits > 0 && todayChecked == totalHabits

    var pendingDeleteHabit by remember { mutableStateOf<Habit?>(null) }

    LaunchedEffect(pendingDeleteHabit) {
        pendingDeleteHabit?.let { h ->
            val result = snackbarHostState.showSnackbar(
                message = "\"${h.name}\" deleted",
                actionLabel = "Undo",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                vm.addHabit(h.name, h.listId, h.color)
            }
            pendingDeleteHabit = null
        }
    }

    var newHabitName by remember { mutableStateOf("") }

    Scaffold(
        containerColor = Background,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.GridView, null, tint = AccentEmerald, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🔥 7-Day Habit Matrix", style = MaterialTheme.typography.headlineMedium, color = OnSurface)
                            if (isPerfectWeek) {
                                Spacer(Modifier.width(6.dp))
                                Text("👑", fontSize = 20.sp)
                            }
                        }
                        Text("habitOS weekly check-in grid & streak chart", style = MaterialTheme.typography.bodySmall, color = OnSurfaceMuted)
                    }
                }
            }

            // Stats cards row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // My Day Progress
                    HabitOSCard(modifier = Modifier.weight(1f)) {
                        Text("MY DAY", style = MaterialTheme.typography.labelSmall, color = OnSurfaceMuted)
                        Text(
                            "$todayChecked/$totalHabits",
                            style = MaterialTheme.typography.headlineLarge,
                            color = OnSurface
                        )
                        Text(
                            "${if (totalHabits > 0) (todayChecked * 100) / totalHabits else 0}% done",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceMuted
                        )
                        if (isPerfectDay) Text("👑 Perfect Day!", color = Amber, style = MaterialTheme.typography.bodySmall)
                    }
                    // Weekly Score
                    HabitOSCard(modifier = Modifier.weight(1f)) {
                        Text("WEEKLY SCORE", style = MaterialTheme.typography.labelSmall, color = OnSurfaceMuted)
                        Text(
                            "$weekPct%",
                            style = MaterialTheme.typography.headlineLarge,
                            color = if (isPerfectWeek) AccentEmerald else OnSurface
                        )
                        Text(
                            "$checkedCells/$totalCells checks",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceMuted
                        )
                    }
                }
            }

            // Add habit
            item {
                AddItemTextField(
                    value = newHabitName,
                    onValueChange = { newHabitName = it },
                    onDone = {
                        if (newHabitName.isNotBlank()) {
                            vm.addHabit(newHabitName.trim())
                            newHabitName = ""
                        }
                    },
                    placeholder = "Add a daily habit…"
                )
            }

            // Week navigation header
            item {
                HabitOSCard {
                    // Week nav
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { weekOffset-- }) {
                            Icon(Icons.Default.ChevronLeft, null, tint = OnSurfaceMuted)
                        }
                        Text(
                            "${weekDays.first().let { "${it.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())} ${it.dayOfMonth}" }} – ${weekDays.last().let { "${it.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())} ${it.dayOfMonth}" }}",
                            style = MaterialTheme.typography.titleMedium,
                            color = OnSurface
                        )
                        IconButton(onClick = { weekOffset++ }) {
                            Icon(Icons.Default.ChevronRight, null, tint = OnSurfaceMuted)
                        }
                    }

                    // Day headers
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Spacer(Modifier.weight(2f))
                        weekDays.forEach { day ->
                            val isToday = day == today
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(3).uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isToday) AccentEmerald else OnSurfaceMuted
                                )
                                Text(
                                    day.dayOfMonth.toString(),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isToday) AccentEmerald else OnSurfaceDim
                                )
                            }
                        }
                        Spacer(Modifier.width(60.dp))
                    }

                    HorizontalDivider(color = Border, modifier = Modifier.padding(vertical = 6.dp))

                    if (habits.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No recurring habits yet. Add one above!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnSurfaceMuted,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        habits.forEach { habit ->
                            HabitMatrixRow(
                                habit     = habit,
                                days      = weekDays,
                                today     = today,
                                checkinSet= checkinSet,
                                onToggle  = { date, isChecked ->
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    vm.toggleCheckin(habit.id, date, isChecked)
                                },
                                onDelete  = {
                                    vm.deleteHabit(habit)
                                    pendingDeleteHabit = habit
                                }
                            )
                            HorizontalDivider(color = Border, thickness = 0.5.dp)
                        }
                    }
                }
            }

            // Weekly bar chart
            item {
                WeeklyBarChart(habits = habits, weekDays = weekDays, checkins = checkins)
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun HabitMatrixRow(
    habit: Habit,
    days: List<LocalDate>,
    today: LocalDate,
    checkinSet: Set<String>,
    onToggle: (LocalDate, Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val streak = computeStreak(habit.id, checkinSet, today)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Habit name
        Text(
            text = habit.name,
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurface,
            modifier = Modifier.weight(2f),
            maxLines = 2
        )

        // Day cells
        days.forEach { day ->
            val key     = "${habit.id}_$day"
            val checked = key in checkinSet
            val isToday = day == today
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .padding(2.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        when {
                            checked && isToday -> AccentEmerald
                            checked           -> AccentEmerald.copy(alpha = 0.6f)
                            isToday           -> Border
                            else              -> Background
                        }
                    )
                    .border(
                        width = if (isToday) 1.dp else 0.5.dp,
                        color = if (isToday) AccentEmerald else Border,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .clickable { onToggle(day, checked) },
                contentAlignment = Alignment.Center
            ) {
                if (checked) {
                    Text("✓", color = if (isToday) Background else OnSurface, fontSize = 10.sp)
                }
            }
        }

        // Streak + delete
        Row(
            modifier = Modifier.width(60.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (streak > 0) {
                Text("🔥$streak", style = MaterialTheme.typography.labelSmall, color = Amber)
            }
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.DeleteOutline, null, tint = OnSurfaceDim, modifier = Modifier.size(16.dp))
            }
        }
    }
}

fun computeStreak(habitId: Long, checkinSet: Set<String>, today: LocalDate): Int {
    var streak = 0
    var date   = today
    while (true) {
        if ("${habitId}_$date" in checkinSet) { streak++; date = date.minusDays(1) }
        else break
    }
    return streak
}

@Composable
fun WeeklyBarChart(
    habits: List<Habit>,
    weekDays: List<LocalDate>,
    checkins: List<HabitCheckin>
) {
    val totalHabits = habits.size
    if (totalHabits == 0) return

    HabitOSCard {
        Text("Weekly Streak Chart", style = MaterialTheme.typography.titleMedium, color = OnSurface)
        Text("Daily completion rate vs 65% streak line", style = MaterialTheme.typography.bodySmall, color = OnSurfaceMuted)
        Spacer(Modifier.height(12.dp))

        val maxBarHeight = 80.dp
        val streakLine   = 0.65f

        Row(
            modifier = Modifier.fillMaxWidth().height(maxBarHeight + 30.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            weekDays.forEach { day ->
                val dayChecked = checkins.count { it.date == day }
                val pct = if (totalHabits > 0) dayChecked.toFloat() / totalHabits else 0f
                val isToday = day == LocalDate.now()

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        "${(pct * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceDim,
                        fontSize = 8.sp
                    )
                    Spacer(Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(maxBarHeight * pct.coerceAtLeast(0.02f))
                            .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                            .background(
                                if (pct >= streakLine) AccentEmerald
                                else if (isToday) Amber
                                else Border
                            )
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(2),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isToday) AccentEmerald else OnSurfaceDim,
                        fontSize = 8.sp
                    )
                }
            }
        }
    }
}
