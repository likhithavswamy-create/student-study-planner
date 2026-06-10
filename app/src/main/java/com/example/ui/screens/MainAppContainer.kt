package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.example.ui.viewmodel.StudyViewModel

sealed class AppDestination(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Dashboard : AppDestination(
        route = "dashboard",
        title = "Dashboard",
        selectedIcon = Icons.Filled.Dashboard,
        unselectedIcon = Icons.Outlined.Dashboard
    )
    object Schedule : AppDestination(
        route = "schedule",
        title = "Schedule",
        selectedIcon = Icons.Filled.Schedule,
        unselectedIcon = Icons.Outlined.Schedule
    )
    object Assignments : AppDestination(
        route = "assignments",
        title = "Assignments",
        selectedIcon = Icons.Filled.Assignment,
        unselectedIcon = Icons.Outlined.Assignment
    )
    object Exams : AppDestination(
        route = "exams",
        title = "Exams",
        selectedIcon = Icons.Filled.School,
        unselectedIcon = Icons.Outlined.School
    )
    object Notes : AppDestination(
        route = "notes",
        title = "Notes",
        selectedIcon = Icons.Filled.EditNote,
        unselectedIcon = Icons.Outlined.EditNote
    )
    object Reminders : AppDestination(
        route = "reminders",
        title = "Reminders",
        selectedIcon = Icons.Filled.Notifications,
        unselectedIcon = Icons.Outlined.Notifications
    )

    companion object {
        val ALL get() = listOf(Dashboard, Schedule, Assignments, Exams, Notes, Reminders)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContainer(
    viewModel: StudyViewModel
) {
    var currentDestination by remember { mutableStateOf<AppDestination>(AppDestination.Dashboard) }

    // Adaptive layout: Determine screen width to show Bottom Bar or Navigation Rail
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    Scaffold(
        bottomBar = {
            if (!isTablet) {
                NavigationBar(
                    windowInsets = WindowInsets.navigationBars
                ) {
                    AppDestination.ALL.forEach { destination ->
                        NavigationBarItem(
                            selected = currentDestination == destination,
                            onClick = { currentDestination = destination },
                            icon = {
                                Icon(
                                    imageVector = if (currentDestination == destination) destination.selectedIcon else destination.unselectedIcon,
                                    contentDescription = destination.title
                                )
                            },
                            label = { 
                                Text(
                                    text = destination.title,
                                    style = MaterialTheme.typography.labelSmall
                                ) 
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Screen Sidebar / Rail for tablets
            if (isTablet) {
                NavigationRail(
                    modifier = Modifier.fillMaxHeight(),
                    header = {
                        Icon(
                            Icons.Filled.School,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(48.dp)
                                .padding(vertical = 12.dp)
                        )
                    }
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    AppDestination.ALL.forEach { destination ->
                        NavigationRailItem(
                            selected = currentDestination == destination,
                            onClick = { currentDestination = destination },
                            icon = {
                                Icon(
                                    imageVector = if (currentDestination == destination) destination.selectedIcon else destination.unselectedIcon,
                                    contentDescription = destination.title
                                )
                            },
                            label = { Text(destination.title) }
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            // Central active view panel
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                when (currentDestination) {
                    AppDestination.Dashboard -> {
                        DashboardScreen(
                            viewModel = viewModel,
                            onNavigateToTab = { tabName ->
                                val dest = AppDestination.ALL.find { it.title.equals(tabName, ignoreCase = true) }
                                if (dest != null) {
                                    currentDestination = dest
                                }
                            }
                        )
                    }
                    AppDestination.Schedule -> {
                        TimetableScreen(viewModel = viewModel)
                    }
                    AppDestination.Assignments -> {
                        AssignmentsScreen(viewModel = viewModel)
                    }
                    AppDestination.Exams -> {
                        ExamsScreen(viewModel = viewModel)
                    }
                    AppDestination.Notes -> {
                        NotesScreen(viewModel = viewModel)
                    }
                    AppDestination.Reminders -> {
                        RemindersScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
