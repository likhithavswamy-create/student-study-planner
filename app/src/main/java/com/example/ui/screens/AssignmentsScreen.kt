package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.ui.viewmodel.StudyViewModel
import com.example.ui.viewmodel.AssignmentUi
import com.example.ui.util.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentsScreen(
    viewModel: StudyViewModel,
    modifier: Modifier = Modifier
) {
    val assignmentsUi by viewModel.assignmentsUi.collectAsState()

    var selectedFilter by remember { mutableStateOf("Pending") } // "All", "Pending", "Completed"
    var showAddDialog by remember { mutableStateOf(false) }

    // Computations
    val totalCount = assignmentsUi.size
    val completedCount = assignmentsUi.count { it.assignment.isCompleted }
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0.0f

    val filteredAssignments = remember(assignmentsUi, selectedFilter) {
        when (selectedFilter) {
            "Pending" -> assignmentsUi.filter { !it.assignment.isCompleted }
            "Completed" -> assignmentsUi.filter { it.assignment.isCompleted }
            else -> assignmentsUi
        }.sortedBy { it.assignment.dueDate }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_assignment_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // High-fidelity progress summary banner
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Assignment Progress",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$completedCount/$totalCount Completed",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }
            }

            // Segments Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Pending", "Completed", "All").forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredAssignments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Assignment,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No coursework found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (selectedFilter == "Pending") "You are completely free of pending coursework!" else "No records here.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 80.dp, start = 16.dp, end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredAssignments, key = { it.assignment.id }) { assignUi ->
                        CourseworkCard(
                            assignUi = assignUi,
                            onToggleComplete = { completed ->
                                viewModel.toggleAssignmentStatus(assignUi.assignment.id, completed)
                            },
                            onDelete = {
                                viewModel.deleteAssignment(assignUi.assignment.id)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddAssignmentDialog(
            onDismiss = { showAddDialog = false },
            viewModel = viewModel
        )
    }
}

@Composable
fun CourseworkCard(
    assignUi: AssignmentUi,
    onToggleComplete: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val subjectColor = assignUi.subject?.colorHex?.let {
        Color(android.graphics.Color.parseColor(it))
    } ?: MaterialTheme.colorScheme.onSurfaceVariant

    ElevatedCard(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Checkbox(
                checked = assignUi.assignment.isCompleted,
                onCheckedChange = onToggleComplete,
                modifier = Modifier.testTag("todo_checkbox_${assignUi.assignment.id}")
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = assignUi.assignment.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (assignUi.assignment.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                    )

                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete item",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                if (assignUi.assignment.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = assignUi.assignment.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Subject tag
                    Box(
                        modifier = Modifier
                            .background(subjectColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = assignUi.subject?.name ?: "None",
                            style = MaterialTheme.typography.labelSmall,
                            color = subjectColor,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Priority tag
                    val priorityColor = when (assignUi.assignment.priority) {
                        "High" -> MaterialTheme.colorScheme.error
                        "Medium" -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.secondary
                    }
                    Box(
                        modifier = Modifier
                            .background(priorityColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${assignUi.assignment.priority} Priority",
                            style = MaterialTheme.typography.labelSmall,
                            color = priorityColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Date displays
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Due: ${DateTimeFormatter.formatDate(assignUi.assignment.dueDate)} (${DateTimeFormatter.getRemainingDaysText(assignUi.assignment.dueDate)})",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (assignUi.assignment.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAssignmentDialog(
    onDismiss: () -> Unit,
    viewModel: StudyViewModel
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedSubjectId by remember { mutableStateOf(0) }
    var selectedPriority by remember { mutableStateOf("Medium") }
    
    // Calendar dates
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    var dueDateTimestamp by remember { mutableStateOf(calendar.timeInMillis) }
    var dueDateString by remember { mutableStateOf(DateTimeFormatter.formatDate(dueDateTimestamp)) }

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val selectedCalendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, dayOfMonth)
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
            }
            dueDateTimestamp = selectedCalendar.timeInMillis
            dueDateString = DateTimeFormatter.formatDate(dueDateTimestamp)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    var expandedPriority by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Add Assignment",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Subject Selector
                SubjectSelector(
                    selectedSubjectId = selectedSubjectId,
                    onSubjectSelected = { selectedSubjectId = it },
                    viewModel = viewModel
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title") },
                    placeholder = { Text("e.g. Science Lab Report") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("assignment_title_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Short Description") },
                    placeholder = { Text("e.g. Pages 50 to 52 details on osmosis") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("assignment_description_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Due Date Trigger Input
                OutlinedTextField(
                    value = dueDateString,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Due Date") },
                    trailingIcon = {
                        IconButton(onClick = { datePickerDialog.show() }) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = "Choose Date")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { datePickerDialog.show() }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Priority Trigger Input
                ExposedDropdownMenuBox(
                    expanded = expandedPriority,
                    onExpandedChange = { expandedPriority = !expandedPriority }
                ) {
                    OutlinedTextField(
                        value = selectedPriority,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Priority Level") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPriority) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedPriority,
                        onDismissRequest = { expandedPriority = false }
                    ) {
                        listOf("Low", "Medium", "High").forEach { level ->
                            DropdownMenuItem(
                                text = { Text(level) },
                                onClick = {
                                    selectedPriority = level
                                    expandedPriority = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                viewModel.addAssignment(
                                    title = title,
                                    description = description,
                                    subjectId = selectedSubjectId,
                                    dueDate = dueDateTimestamp,
                                    priority = selectedPriority
                                )
                                onDismiss()
                            }
                        },
                        modifier = Modifier.testTag("save_assignment_button"),
                        enabled = title.isNotBlank()
                    ) {
                        Text("Save Task")
                    }
                }
            }
        }
    }
}
