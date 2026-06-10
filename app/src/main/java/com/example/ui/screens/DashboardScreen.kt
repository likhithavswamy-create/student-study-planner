package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.StudyViewModel
import com.example.ui.viewmodel.TimetableSlotUi
import com.example.ui.viewmodel.AssignmentUi
import com.example.ui.viewmodel.ExamUi
import com.example.ui.util.DateTimeFormatter
import java.util.*

@Composable
fun DashboardScreen(
    viewModel: StudyViewModel,
    onNavigateToTab: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val stats by viewModel.dashboardStats.collectAsState()
    val timetableSlots by viewModel.timetableSlotsUi.collectAsState()
    val assignments by viewModel.assignmentsUi.collectAsState()
    val exams by viewModel.examsUi.collectAsState()

    // Get today's calendar and format day
    val calendar = Calendar.getInstance()
    val dayOfWeekString = when (calendar.get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> "Monday"
        Calendar.TUESDAY -> "Tuesday"
        Calendar.WEDNESDAY -> "Wednesday"
        Calendar.THURSDAY -> "Thursday"
        Calendar.FRIDAY -> "Friday"
        Calendar.SATURDAY -> "Saturday"
        Calendar.SUNDAY -> "Sunday"
        else -> "Monday"
    }
    
    val todayDateFormatted = DateTimeFormatter.formatDate(calendar.timeInMillis)

    // Filter today's timetable slots
    val todayClasses = remember(timetableSlots, dayOfWeekString) {
        timetableSlots.filter { it.slot.dayOfWeek.equals(dayOfWeekString, ignoreCase = true) }
    }

    // Get closest upcoming exams and pending assignments
    val urgentExams = remember(exams) {
        exams.filter { !it.exam.isCompleted && it.exam.date >= System.currentTimeMillis() }
            .take(2)
    }

    val pendingAssignments = remember(assignments) {
        assignments.filter { !it.assignment.isCompleted }
            .sortedBy { it.assignment.dueDate }
            .take(2)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome and Header Card with beautiful Gradient Accent
        item {
            WelcomeHeader(todayDateFormatted)
        }

        // Quick Stats row
        item {
            StatsGrid(
                stats = stats,
                onNavigateToTab = onNavigateToTab
            )
        }

        // Today's classes preview section
        item {
            SectionHeader(
                title = "Today's Schedule ($dayOfWeekString)",
                actionLabel = "View All",
                onActionClick = { onNavigateToTab("Schedule") }
            )
        }

        if (todayClasses.isEmpty()) {
            item {
                EmptyStateCard(
                    message = "No classes scheduled for today. Take some time to study or rest!",
                    icon = Icons.Outlined.CalendarToday,
                    onClickAction = { onNavigateToTab("Schedule") },
                    actionLabel = "Modify Schedule"
                )
            }
        } else {
            items(todayClasses) { slotUi ->
                TodayClassItem(slotUi)
            }
        }

        // Pending assignments section
        item {
            SectionHeader(
                title = "Upcoming Assignments",
                actionLabel = "View All",
                onActionClick = { onNavigateToTab("Assignments") }
            )
        }

        if (pendingAssignments.isEmpty()) {
            item {
                EmptyStateCard(
                    message = "Hooray! No pending assignments. You're all caught up!",
                    icon = Icons.Outlined.CheckCircle,
                    onClickAction = { onNavigateToTab("Assignments") },
                    actionLabel = "Add Assignment"
                )
            }
        } else {
            items(pendingAssignments) { assignUi ->
                AssignmentCard(assignUi, onToggleCheck = { completed ->
                    viewModel.toggleAssignmentStatus(assignUi.assignment.id, completed)
                })
            }
        }

        // Upcoming exams section
        item {
            SectionHeader(
                title = "Exam Countdowns",
                actionLabel = "View All",
                onActionClick = { onNavigateToTab("Exams") }
            )
        }

        if (urgentExams.isEmpty()) {
            item {
                EmptyStateCard(
                    message = "No upcoming exams on the horizon. Excellent!",
                    icon = Icons.Outlined.SentimentSatisfiedAlt,
                    onClickAction = { onNavigateToTab("Exams") },
                    actionLabel = "Schedule Exam"
                )
            }
        } else {
            items(urgentExams) { examUi ->
                ExamCountdownCard(examUi)
            }
        }

        // Aesthetic motivational note card
        item {
            MotivationalQuoteCard()
        }
    }
}

@Composable
fun WelcomeHeader(dateString: String) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = listOf(primary, secondary.copy(alpha = 0.8f))
                    )
                )
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = dateString.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.8f),
                        letterSpacing = 1.sp
                    )
                    Icon(
                        Icons.Default.School,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Welcome back, Student!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Stay organized and master your study goals today.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
fun StatsGrid(
    stats: com.example.ui.viewmodel.DashboardStats,
    onNavigateToTab: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatsCard(
            title = "Today's Classes",
            count = stats.classesTodayCount.toString(),
            color = MaterialTheme.colorScheme.primary,
            icon = Icons.Outlined.Book,
            onClick = { onNavigateToTab("Schedule") },
            modifier = Modifier.weight(1f)
        )
        StatsCard(
            title = "Due Tasks",
            count = stats.pendingAssignmentsCount.toString(),
            color = MaterialTheme.colorScheme.error,
            icon = Icons.Outlined.Assignment,
            onClick = { onNavigateToTab("Assignments") },
            modifier = Modifier.weight(1f)
        )
    }
    
    Spacer(modifier = Modifier.height(12.dp))
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatsCard(
            title = "Exams",
            count = stats.upcomingExamsCount.toString(),
            color = MaterialTheme.colorScheme.tertiary,
            icon = Icons.Outlined.EventNote,
            onClick = { onNavigateToTab("Exams") },
            modifier = Modifier.weight(1f)
        )
        StatsCard(
            title = "Reminders",
            count = stats.pendingRemindersCount.toString(),
            color = MaterialTheme.colorScheme.secondary,
            icon = Icons.Outlined.Notifications,
            onClick = { onNavigateToTab("Reminders") },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatsCard(
    title: String,
    count: String,
    color: Color,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                Text(
                    text = count,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    actionLabel: String,
    onActionClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        TextButton(onClick = onActionClick) {
            Text(
                text = actionLabel,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun TodayClassItem(slotUi: TimetableSlotUi) {
    val subjectColor = slotUi.subject?.colorHex?.let {
        Color(android.graphics.Color.parseColor(it))
    } ?: MaterialTheme.colorScheme.primary

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier.height(intrinsicSize = IntrinsicSize.Min)
        ) {
            // Color band on the left
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(subjectColor)
            )

            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = slotUi.subject?.name ?: "General Class",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Room,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = slotUi.slot.room.ifBlank { "N/A" },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (slotUi.slot.instructor.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = slotUi.slot.instructor,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = slotUi.slot.startTime,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "to ${slotUi.slot.endTime}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun AssignmentCard(
    assignUi: AssignmentUi,
    onToggleCheck: (Boolean) -> Unit
) {
    val color = assignUi.subject?.colorHex?.let {
        Color(android.graphics.Color.parseColor(it))
    } ?: MaterialTheme.colorScheme.primary

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = assignUi.assignment.isCompleted,
                onCheckedChange = onToggleCheck
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = assignUi.assignment.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    
                    val priorityColor = when (assignUi.assignment.priority) {
                        "High" -> MaterialTheme.colorScheme.error
                        "Medium" -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.secondary
                    }
                    
                    Box(
                        modifier = Modifier
                            .background(priorityColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = assignUi.assignment.priority,
                            style = MaterialTheme.typography.labelSmall,
                            color = priorityColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = assignUi.subject?.name ?: "No Subject",
                        style = MaterialTheme.typography.bodySmall,
                        color = color,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = "Due: ${DateTimeFormatter.formatDate(assignUi.assignment.dueDate)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ExamCountdownCard(examUi: ExamUi) {
    val color = examUi.subject?.colorHex?.let {
        Color(android.graphics.Color.parseColor(it))
    } ?: MaterialTheme.colorScheme.primary

    ElevatedCard(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.HourglassEmpty,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = examUi.exam.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = examUi.subject?.name ?: "General Exam",
                    style = MaterialTheme.typography.bodySmall,
                    color = color,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = DateTimeFormatter.formatDateTime(examUi.exam.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = DateTimeFormatter.getRemainingDaysText(examUi.exam.date),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun EmptyStateCard(
    message: String,
    icon: ImageVector,
    onClickAction: () -> Unit,
    actionLabel: String
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(onClick = onClickAction) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(actionLabel, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
fun MotivationalQuoteCard() {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.FormatQuote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "\"Live as if you were to die tomorrow. Learn as if you were to live forever.\"",
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "— Mahatma Gandhi",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
