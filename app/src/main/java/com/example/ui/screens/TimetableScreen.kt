package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Room
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.ui.viewmodel.StudyViewModel
import com.example.ui.viewmodel.TimetableSlotUi
import java.util.*

val DAYS_OF_WEEK = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableScreen(
    viewModel: StudyViewModel,
    modifier: Modifier = Modifier
) {
    val timetableSlotsUi by viewModel.timetableSlotsUi.collectAsState()

    // Determine current day of week to set as default select index
    val calendar = Calendar.getInstance()
    val currentDayIndex = when (calendar.get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> 0
        Calendar.TUESDAY -> 1
        Calendar.WEDNESDAY -> 2
        Calendar.THURSDAY -> 3
        Calendar.FRIDAY -> 4
        Calendar.SATURDAY -> 5
        Calendar.SUNDAY -> 6
        else -> 0
    }

    var selectedDayIndex by remember { mutableStateOf(currentDayIndex) }
    val selectedDay = DAYS_OF_WEEK[selectedDayIndex]

    // Filter items based on selected day
    val activeSlots = remember(timetableSlotsUi, selectedDay) {
        timetableSlotsUi.filter { it.slot.dayOfWeek.equals(selectedDay, ignoreCase = true) }
    }

    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_timetable_slot_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Class")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Elegant scrolling TabRow for Days
            PrimaryScrollableTabRow(
                selectedTabIndex = selectedDayIndex,
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                DAYS_OF_WEEK.forEachIndexed { index, day ->
                    Tab(
                        selected = selectedDayIndex == index,
                        onClick = { selectedDayIndex = index },
                        text = {
                            Text(
                                text = day.take(3), // Mon, Tue, etc.
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (selectedDayIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (activeSlots.isEmpty()) {
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
                            Icons.Outlined.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No classes for $selectedDay",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap the plus button to register a class class or study block.",
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
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(activeSlots, key = { it.slot.id }) { slotUi ->
                        TimetableItemCard(slotUi, onDelete = {
                            viewModel.deleteTimetableSlot(slotUi.slot.id)
                        })
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddTimetableSlotDialog(
            defaultDay = selectedDay,
            onDismiss = { showAddDialog = false },
            viewModel = viewModel
        )
    }
}

@Composable
fun TimetableItemCard(
    slotUi: TimetableSlotUi,
    onDelete: () -> Unit
) {
    val subjectColor = slotUi.subject?.colorHex?.let {
        Color(android.graphics.Color.parseColor(it))
    } ?: MaterialTheme.colorScheme.primary

    ElevatedCard(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.height(intrinsicSize = IntrinsicSize.Min)
        ) {
            // High-fidelity subject color left-accent band
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .fillMaxHeight()
                    .background(subjectColor)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = slotUi.subject?.name ?: "Independent Study",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete class",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Room,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = slotUi.slot.room.ifBlank { "Unassigned Room" },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (slotUi.slot.instructor.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = slotUi.slot.instructor,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${slotUi.slot.startTime} — ${slotUi.slot.endTime}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTimetableSlotDialog(
    defaultDay: String,
    onDismiss: () -> Unit,
    viewModel: StudyViewModel
) {
    var selectedSubjectId by remember { mutableStateOf(0) }
    var dayOfWeek by remember { mutableStateOf(defaultDay) }
    var startTime by remember { mutableStateOf("09:00") }
    var endTime by remember { mutableStateOf("10:30") }
    var room by remember { mutableStateOf("") }
    var instructor by remember { mutableStateOf("") }

    var expandedDayDropdown by remember { mutableStateOf(false) }

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
                    text = "Add Class Session",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                // Subject picker utilizing reusable Selector
                SubjectSelector(
                    selectedSubjectId = selectedSubjectId,
                    onSubjectSelected = { selectedSubjectId = it },
                    viewModel = viewModel
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Day Selection Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedDayDropdown,
                    onExpandedChange = { expandedDayDropdown = !expandedDayDropdown }
                ) {
                    OutlinedTextField(
                        value = dayOfWeek,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Day of Week") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDayDropdown) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedDayDropdown,
                        onDismissRequest = { expandedDayDropdown = false }
                    ) {
                        DAYS_OF_WEEK.forEach { day ->
                            DropdownMenuItem(
                                text = { Text(day) },
                                onClick = {
                                    dayOfWeek = day
                                    expandedDayDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Time Inputs Side-by-Side
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Start (e.g. 09:00)") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("start_time_input")
                    )

                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("End (e.g. 10:30)") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("end_time_input")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = room,
                    onValueChange = { room = it },
                    label = { Text("Room / Hall") },
                    placeholder = { Text("e.g. Lab 3, Hall 4B") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("class_room_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = instructor,
                    onValueChange = { instructor = it },
                    label = { Text("Instructor / Prof") },
                    placeholder = { Text("e.g. Dr. Alan Turing") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("class_instructor_input")
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
                            viewModel.addTimetableSlot(
                                subjectId = selectedSubjectId,
                                dayOfWeek = dayOfWeek,
                                startTime = startTime,
                                endTime = endTime,
                                room = room,
                                instructor = instructor
                            )
                            onDismiss()
                        },
                        modifier = Modifier.testTag("save_timetable_slot_button")
                    ) {
                        Text("Save Block")
                    }
                }
            }
        }
    }
}
