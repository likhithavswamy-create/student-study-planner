package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.StudyRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class TimetableSlotUi(
    val slot: TimetableSlot,
    val subject: Subject?
)

data class AssignmentUi(
    val assignment: Assignment,
    val subject: Subject?
)

data class ExamUi(
    val exam: Exam,
    val subject: Subject?
)

data class NoteUi(
    val note: Note,
    val subject: Subject?
)

class StudyViewModel(private val repository: StudyRepository) : ViewModel() {

    // --- SUBJECTS STATE ---
    val subjects: StateFlow<List<Subject>> = repository.allSubjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- TIMETABLE STATE ---
    val timetableSlotsUi: StateFlow<List<TimetableSlotUi>> = combine(
        repository.allTimetableSlots,
        subjects
    ) { slots, subs ->
        slots.map { slot ->
            TimetableSlotUi(slot, subs.find { it.id == slot.subjectId })
        }.sortedWith(compareBy({ getDayIndex(it.slot.dayOfWeek) }, { it.slot.startTime }))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- ASSIGNMENTS STATE ---
    val assignmentsUi: StateFlow<List<AssignmentUi>> = combine(
        repository.allAssignments,
        subjects
    ) { assigns, subs ->
        assigns.map { assign ->
            AssignmentUi(assign, subs.find { it.id == assign.subjectId })
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- EXAMS STATE ---
    val examsUi: StateFlow<List<ExamUi>> = combine(
        repository.allExams,
        subjects
    ) { exams, subs ->
        exams.map { exam ->
            ExamUi(exam, subs.find { it.id == exam.subjectId })
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- NOTES STATE ---
    val notesUi: StateFlow<List<NoteUi>> = combine(
        repository.allNotes,
        subjects
    ) { notes, subs ->
        notes.map { note ->
            NoteUi(note, subs.find { it.id == note.subjectId })
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- REMINDERS STATE ---
    val reminders: StateFlow<List<Reminder>> = repository.allReminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- STATISTICS FOR DASHBOARD ---
    val dashboardStats = combine(
        timetableSlotsUi,
        assignmentsUi,
        examsUi,
        reminders
    ) { slots, assigns, exams, rems ->
        val pendingAssignments = assigns.count { !it.assignment.isCompleted }
        val upcomingExams = exams.count { !it.exam.isCompleted }
        val pendingReminders = rems.count { !it.isCompleted }
        val classesToday = slots.count { it.slot.dayOfWeek == getCurrentDayOfWeekString() }
        
        DashboardStats(
            pendingAssignmentsCount = pendingAssignments,
            upcomingExamsCount = upcomingExams,
            pendingRemindersCount = pendingReminders,
            classesTodayCount = classesToday
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardStats())


    // --- OPERATIONS ---

    // Subject Ops
    fun addSubject(name: String, colorHex: String) {
        viewModelScope.launch {
            repository.insertSubject(Subject(name = name, colorHex = colorHex))
        }
    }

    fun deleteSubject(id: Int) {
        viewModelScope.launch {
            repository.deleteSubject(id)
        }
    }

    // Timetable Ops
    fun addTimetableSlot(subjectId: Int, dayOfWeek: String, startTime: String, endTime: String, room: String, instructor: String) {
        viewModelScope.launch {
            repository.insertTimetableSlot(
                TimetableSlot(
                    subjectId = subjectId,
                    dayOfWeek = dayOfWeek,
                    startTime = startTime,
                    endTime = endTime,
                    room = room,
                    instructor = instructor
                )
            )
        }
    }

    fun deleteTimetableSlot(id: Int) {
        viewModelScope.launch {
            repository.deleteTimetableSlot(id)
        }
    }

    // Assignment Ops
    fun addAssignment(title: String, description: String, subjectId: Int, dueDate: Long, priority: String) {
        viewModelScope.launch {
            repository.insertAssignment(
                Assignment(
                    title = title,
                    description = description,
                    subjectId = subjectId,
                    dueDate = dueDate,
                    priority = priority
                )
            )
        }
    }

    fun deleteAssignment(id: Int) {
        viewModelScope.launch {
            repository.deleteAssignment(id)
        }
    }

    fun toggleAssignmentStatus(id: Int, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.updateAssignmentStatus(id, isCompleted)
        }
    }

    // Exam Ops
    fun addExam(title: String, subjectId: Int, date: Long, venue: String, notes: String) {
        viewModelScope.launch {
            repository.insertExam(
                Exam(
                    title = title,
                    subjectId = subjectId,
                    date = date,
                    venue = venue,
                    notes = notes
                )
            )
        }
    }

    fun deleteExam(id: Int) {
        viewModelScope.launch {
            repository.deleteExam(id)
        }
    }

    fun toggleExamStatus(id: Int, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.updateExamStatus(id, isCompleted)
        }
    }

    // Note Ops
    fun addNote(subjectId: Int, title: String, content: String) {
        viewModelScope.launch {
            if (title.isNotBlank() || content.isNotBlank()) {
                repository.insertNote(
                    Note(
                        subjectId = subjectId,
                        title = title,
                        content = content,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    fun deleteNote(id: Int) {
        viewModelScope.launch {
            repository.deleteNote(id)
        }
    }

    // Reminder Ops
    fun addReminder(title: String, dateTime: Long, associatedType: String = "General", associatedId: Int = 0) {
        viewModelScope.launch {
            repository.insertReminder(
                Reminder(
                    title = title,
                    dateTime = dateTime,
                    associatedType = associatedType,
                    associatedId = associatedId
                )
            )
        }
    }

    fun deleteReminder(id: Int) {
        viewModelScope.launch {
            repository.deleteReminder(id)
        }
    }

    fun toggleReminderStatus(id: Int, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.updateReminderStatus(id, isCompleted)
        }
    }


    // --- HELPERS ---

    private fun getDayIndex(day: String): Int {
        return when (day.trim().lowercase()) {
            "monday" -> 1
            "tuesday" -> 2
            "wednesday" -> 3
            "thursday" -> 4
            "friday" -> 5
            "saturday" -> 6
            "sunday" -> 7
            else -> 8
        }
    }

    private fun getCurrentDayOfWeekString(): String {
        val calendar = java.util.Calendar.getInstance()
        return when (calendar.get(java.util.Calendar.DAY_OF_WEEK)) {
            java.util.Calendar.MONDAY -> "Monday"
            java.util.Calendar.TUESDAY -> "Tuesday"
            java.util.Calendar.WEDNESDAY -> "Wednesday"
            java.util.Calendar.THURSDAY -> "Thursday"
            java.util.Calendar.FRIDAY -> "Friday"
            java.util.Calendar.SATURDAY -> "Saturday"
            java.util.Calendar.SUNDAY -> "Sunday"
            else -> "Monday"
        }
    }
}

data class DashboardStats(
    val pendingAssignmentsCount: Int = 0,
    val upcomingExamsCount: Int = 0,
    val pendingRemindersCount: Int = 0,
    val classesTodayCount: Int = 0
)

class StudyViewModelFactory(private val repository: StudyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StudyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StudyViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
