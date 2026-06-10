package com.example.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Reminder
import com.example.ui.viewmodel.StudyViewModel
import com.example.ui.util.DateTimeFormatter
import java.util.*

@Composable
fun RemindersScreen(
    viewModel: StudyViewModel,
    modifier: Modifier = Modifier
) {
    val reminders by viewModel.reminders.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    // Split reminders into pending and past
    val activeReminders = remember(reminders) {
        reminders.filter { !it.isCompleted }.sortedBy { it.dateTime }
    }

    val pastReminders = remember(reminders) {
        reminders.filter { it.isCompleted }.sortedByDescending { it.dateTime }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_reminder_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Reminder")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (reminders.isEmpty()) {
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
                            Icons.Outlined.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No study reminders",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Add reminders to review topics, pack books, or participate in study sessions.",
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
                    contentPadding = PaddingValues(bottom = 80.dp, start = 16.dp, end = 16.dp, top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (activeReminders.isNotEmpty()) {
                        item {
                            Text(
                                text = "Active Study Alerts",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        items(activeReminders, key = { "act-${it.id}" }) { reminder ->
                            ReminderCardItem(
                                reminder = reminder,
                                onToggleComplete = { completed ->
                                    viewModel.toggleReminderStatus(reminder.id, completed)
                                },
                                onDelete = { viewModel.deleteReminder(reminder.id) }
                            )
                        }
                    }

                    if (pastReminders.isNotEmpty()) {
                        item {
                            Text(
                                text = "Logged History",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                            )
                        }

                        items(pastReminders, key = { "past-${it.id}" }) { reminder ->
                            ReminderCardItem(
                                reminder = reminder,
                                onToggleComplete = { completed ->
                                    viewModel.toggleReminderStatus(reminder.id, completed)
                                },
                                onDelete = { viewModel.deleteReminder(reminder.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddReminderDialog(
            onDismiss = { showAddDialog = false },
            viewModel = viewModel
        )
    }
}

@Composable
fun ReminderCardItem(
    reminder: Reminder,
    onToggleComplete: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val isCompleted = reminder.isCompleted

    ElevatedCard(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isCompleted,
                onCheckedChange = onToggleComplete,
                modifier = Modifier.testTag("reminder_check_${reminder.id}")
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reminder.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.NotificationsActive,
                        contentDescription = "Alert Time",
                        tint = if (isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = DateTimeFormatter.formatDateTime(reminder.dateTime),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete Reminder",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderDialog(
    onDismiss: () -> Unit,
    viewModel: StudyViewModel
) {
    var title by remember { mutableStateOf("") }

    // native pickers setups
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    var selectedYear by remember { mutableStateOf(calendar.get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableStateOf(calendar.get(Calendar.MONTH)) }
    var selectedDay by remember { mutableStateOf(calendar.get(Calendar.DAY_OF_MONTH)) }
    var selectedHour by remember { mutableStateOf(calendar.get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableStateOf(calendar.get(Calendar.MINUTE)) }

    val formattedString by remember {
        derivedStateOf {
            val cal = Calendar.getInstance().apply {
                set(Calendar.YEAR, selectedYear)
                set(Calendar.MONTH, selectedMonth)
                set(Calendar.DAY_OF_MONTH, selectedDay)
                set(Calendar.HOUR_OF_DAY, selectedHour)
                set(Calendar.MINUTE, selectedMinute)
            }
            DateTimeFormatter.formatDateTime(cal.timeInMillis)
        }
    }

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            selectedYear = year
            selectedMonth = month
            selectedDay = dayOfMonth
        },
        selectedYear,
        selectedMonth,
        selectedDay
    )

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hr, min ->
            selectedHour = hr
            selectedMinute = min
        },
        selectedHour,
        selectedMinute,
        false
    )

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
                    text = "Add Study Reminder",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Reminder Title") },
                    placeholder = { Text("e.g. Return books to library, call study group") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reminder_title_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = "${selectedMonth + 1}/${selectedDay}/${selectedYear}",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Day") },
                        trailingIcon = {
                            IconButton(onClick = { datePickerDialog.show() }) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = "Pick day")
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .clickable { datePickerDialog.show() }
                    )

                    OutlinedTextField(
                        value = DateTimeFormatter.formatTime(selectedHour, selectedMinute),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Hour") },
                        trailingIcon = {
                            IconButton(onClick = { timePickerDialog.show() }) {
                                Icon(Icons.Outlined.Timer, contentDescription = "Pick hour")
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .clickable { timePickerDialog.show() }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Alert setup at: $formattedString",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

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
                                val cal = Calendar.getInstance().apply {
                                    set(Calendar.YEAR, selectedYear)
                                    set(Calendar.MONTH, selectedMonth)
                                    set(Calendar.DAY_OF_MONTH, selectedDay)
                                    set(Calendar.HOUR_OF_DAY, selectedHour)
                                    set(Calendar.MINUTE, selectedMinute)
                                }
                                viewModel.addReminder(
                                    title = title,
                                    dateTime = cal.timeInMillis
                                )
                                onDismiss()
                            }
                        },
                        modifier = Modifier.testTag("save_reminder_button"),
                        enabled = title.isNotBlank()
                    ) {
                        Text("Save Alert")
                    }
                }
            }
        }
    }
}
