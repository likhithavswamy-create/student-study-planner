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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.EventNote
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.ui.viewmodel.StudyViewModel
import com.example.ui.viewmodel.ExamUi
import com.example.ui.util.DateTimeFormatter
import java.util.*

@Composable
fun ExamsScreen(
    viewModel: StudyViewModel,
    modifier: Modifier = Modifier
) {
    val examsUi by viewModel.examsUi.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    // Split into upcoming and past
    val upcomingExams = remember(examsUi) {
        examsUi.filter { !it.exam.isCompleted }.sortedBy { it.exam.date }
    }

    val pastExams = remember(examsUi) {
        examsUi.filter { it.exam.isCompleted }.sortedByDescending { it.exam.date }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_exam_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Exam")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (examsUi.isEmpty()) {
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
                            Icons.Outlined.EventNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No exams registered",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Log upcoming exams and midterms to track milestones and stay prepared.",
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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (upcomingExams.isNotEmpty()) {
                        item {
                            Text(
                                text = "Upcoming Exams",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        items(upcomingExams, key = { "up-${it.exam.id}" }) { examUi ->
                            ExamCardItem(
                                examUi = examUi,
                                onToggleComplete = { viewModel.toggleExamStatus(examUi.exam.id, true) },
                                onDelete = { viewModel.deleteExam(examUi.exam.id) }
                            )
                        }
                    }

                    if (pastExams.isNotEmpty()) {
                        item {
                            Text(
                                text = "Completed Exams",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                            )
                        }

                        items(pastExams, key = { "past-${it.exam.id}" }) { examUi ->
                            ExamCardItem(
                                examUi = examUi,
                                onToggleComplete = { viewModel.toggleExamStatus(examUi.exam.id, false) },
                                onDelete = { viewModel.deleteExam(examUi.exam.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddExamDialog(
            onDismiss = { showAddDialog = false },
            viewModel = viewModel
        )
    }
}

@Composable
fun ExamCardItem(
    examUi: ExamUi,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit
) {
    val subjectColor = examUi.subject?.colorHex?.let {
        Color(android.graphics.Color.parseColor(it))
    } ?: MaterialTheme.colorScheme.onSurfaceVariant

    val isCompleted = examUi.exam.isCompleted

    ElevatedCard(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Header of the card containing Subject Tag and Completion controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(subjectColor)
                    )
                    Text(
                        text = examUi.subject?.name ?: "No Subject",
                        style = MaterialTheme.typography.labelSmall,
                        color = subjectColor,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = onToggleComplete, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = "Toggle Complete",
                            tint = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete Exam",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Exam Title
            Text(
                text = examUi.exam.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
            )

            if (examUi.exam.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = examUi.exam.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

            Spacer(modifier = Modifier.height(12.dp))

            // Venue and Date rows
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = "Time",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = DateTimeFormatter.formatDateTime(examUi.exam.date),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (examUi.exam.venue.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Room,
                                contentDescription = "Venue",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = examUi.exam.venue,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                if (!isCompleted) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (examUi.exam.date - System.currentTimeMillis() < (1000 * 60 * 60 * 24 * 2)) {
                                    MaterialTheme.colorScheme.errorContainer
                                } else {
                                    MaterialTheme.colorScheme.primaryContainer
                                },
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = DateTimeFormatter.getRemainingDaysText(examUi.exam.date),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (examUi.exam.date - System.currentTimeMillis() < (1000 * 60 * 60 * 24 * 2)) {
                                MaterialTheme.colorScheme.onErrorContainer
                            } else {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            }
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .background(
                                Color.Gray.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Completed",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExamDialog(
    onDismiss: () -> Unit,
    viewModel: StudyViewModel
) {
    var title by remember { mutableStateOf("") }
    var venue by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedSubjectId by remember { mutableStateOf(0) }

    // Multi-picker Calendar calculations
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    var selectedYear by remember { mutableStateOf(calendar.get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableStateOf(calendar.get(Calendar.MONTH)) }
    var selectedDay by remember { mutableStateOf(calendar.get(Calendar.DAY_OF_MONTH)) }
    var selectedHour by remember { mutableStateOf(10) }
    var selectedMinute by remember { mutableStateOf(0) }

    val formattedDateString by remember {
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
                    text = "Schedule Exam",
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
                    label = { Text("Exam / Test Title") },
                    placeholder = { Text("e.g. Chemistry Midterm Exam") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("exam_title_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = venue,
                    onValueChange = { venue = it },
                    label = { Text("Venue / Room") },
                    placeholder = { Text("e.g. Gym Hall C, Auditorium") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("exam_venue_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Date/Time pickers combined in Row trigger slots
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = "${selectedMonth + 1}/${selectedDay}/${selectedYear}",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Date") },
                        trailingIcon = {
                            IconButton(onClick = { datePickerDialog.show() }) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = "Pick Date")
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
                        label = { Text("Time") },
                        trailingIcon = {
                            IconButton(onClick = { timePickerDialog.show() }) {
                                Icon(Icons.Outlined.Timer, contentDescription = "Pick Time")
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .clickable { timePickerDialog.show() }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Preparation Notes") },
                    placeholder = { Text("e.g. Focus on Chapters 3-5 and lecture slides") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("exam_notes_input")
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Scheduled: $formattedDateString",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
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
                                viewModel.addExam(
                                    title = title,
                                    subjectId = selectedSubjectId,
                                    date = cal.timeInMillis,
                                    venue = venue,
                                    notes = notes
                                )
                                onDismiss()
                            }
                        },
                        modifier = Modifier.testTag("save_exam_button"),
                        enabled = title.isNotBlank()
                    ) {
                        Text("Save Exam")
                    }
                }
            }
        }
    }
}
