package com.habitos.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.habitos.app.data.models.NavTab
import com.habitos.app.ui.screens.*
import com.habitos.app.ui.theme.*
import com.habitos.app.ui.viewmodel.MainViewModel
import com.habitos.app.ui.viewmodel.MainViewModelFactory

data class BottomNavItem(val tab: NavTab, val label: String, val icon: ImageVector)

val bottomNavItems = listOf(
    BottomNavItem(NavTab.MY_DAY,      "My Day",    Icons.Default.WbSunny),
    BottomNavItem(NavTab.HABIT_MATRIX,"Habit Grid",Icons.Default.GridView),
    BottomNavItem(NavTab.BIRTHDAYS,   "Birthdays", Icons.Default.Cake),
    BottomNavItem(NavTab.PLANNED,     "Planned",   Icons.Default.CalendarMonth),
    BottomNavItem(NavTab.LISTS,       "Lists",     Icons.Default.List),
)

@Composable
fun HabitOSRoot() {
    val context = LocalContext.current
    val vm: MainViewModel = viewModel(factory = MainViewModelFactory(context.applicationContext as android.app.Application))
    val navController = rememberNavController()

    Scaffold(
        containerColor = com.habitos.app.ui.theme.Background,
        bottomBar = {
            NavigationBar(
                containerColor = NavBar,
                tonalElevation = 0.dp
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                bottomNavItems.forEach { item ->
                    val selected = currentDestination?.hierarchy?.any { it.route == item.tab.name } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(item.tab.name) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor   = AccentEmerald,
                            selectedTextColor   = AccentEmerald,
                            indicatorColor      = AccentEmerald.copy(alpha = 0.15f),
                            unselectedIconColor = OnSurfaceMuted,
                            unselectedTextColor = OnSurfaceMuted,
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController    = navController,
            startDestination = NavTab.MY_DAY.name,
            modifier         = Modifier.padding(innerPadding)
        ) {
            composable(NavTab.MY_DAY.name)       { MyDayScreen(vm) }
            composable(NavTab.HABIT_MATRIX.name) { HabitMatrixScreen(vm) }
            composable(NavTab.BIRTHDAYS.name)    { BirthdaysScreen(vm) }
            composable(NavTab.PLANNED.name)      { PlannedScreen(vm) }
            composable(NavTab.LISTS.name)        { ListsScreen(vm) }
        }
    }
}
