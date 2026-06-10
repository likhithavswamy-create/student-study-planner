package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val colorHex: String // e.g. "#4F46E5"
) {
    companion object {
        val DEFAULT_SUBJECTS = listOf(
            Subject(name = "Mathematics", colorHex = "#EF4444"),
            Subject(name = "Physics", colorHex = "#3B82F6"),
            Subject(name = "Chemistry", colorHex = "#10B981"),
            Subject(name = "Computer Science", colorHex = "#8B5CF6"),
            Subject(name = "English Literature", colorHex = "#F59E0B"),
            Subject(name = "History", colorHex = "#EC4899")
        )
    }
}

@Entity(tableName = "timetable_slots")
data class TimetableSlot(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subjectId: Int, // Refers to Subject.id, 0 if None
    val dayOfWeek: String, // e.g. "Monday", "Tuesday", etc.
    val startTime: String, // e.g. "09:00"
    val endTime: String, // e.g. "10:30"
    val room: String = "",
    val instructor: String = ""
)

@Entity(tableName = "assignments")
data class Assignment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String = "",
    val subjectId: Int, // Refers to Subject.id, 0 if None
    val dueDate: Long, // timestamp
    val isCompleted: Boolean = false,
    val priority: String = "Medium" // "Low", "Medium", "High"
)

@Entity(tableName = "exams")
data class Exam(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val subjectId: Int, // Refers to Subject.id, 0 if None
    val date: Long, // timestamp (date + time)
    val venue: String = "",
    val notes: String = "",
    val isCompleted: Boolean = false
)

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subjectId: Int, // Refers to Subject.id, 0 if None
    val title: String,
    val content: String,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val dateTime: Long, // timestamp
    val isCompleted: Boolean = false,
    val associatedType: String = "General", // "General", "Assignment", "Exam", "Timetable"
    val associatedId: Int = 0 // Reference ID
)
