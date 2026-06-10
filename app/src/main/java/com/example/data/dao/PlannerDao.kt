package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PlannerDao {

    // --- SUBJECTS ---
    @Query("SELECT * FROM subjects ORDER BY name ASC")
    fun getAllSubjects(): Flow<List<Subject>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: Subject): Long

    @Query("DELETE FROM subjects WHERE id = :id")
    suspend fun deleteSubjectById(id: Int)

    @Query("SELECT * FROM subjects WHERE id = :id")
    suspend fun getSubjectById(id: Int): Subject?

    // --- TIMETABLE SLOTS ---
    @Query("SELECT * FROM timetable_slots")
    fun getAllTimetableSlots(): Flow<List<TimetableSlot>>

    @Query("SELECT * FROM timetable_slots WHERE dayOfWeek = :day")
    fun getTimetableSlotsForDay(day: String): Flow<List<TimetableSlot>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimetableSlot(slot: TimetableSlot): Long

    @Query("DELETE FROM timetable_slots WHERE id = :id")
    suspend fun deleteTimetableSlotById(id: Int)

    // --- ASSIGNMENTS ---
    @Query("SELECT * FROM assignments ORDER BY dueDate ASC")
    fun getAllAssignments(): Flow<List<Assignment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignment(assignment: Assignment): Long

    @Query("DELETE FROM assignments WHERE id = :id")
    suspend fun deleteAssignmentById(id: Int)

    @Query("UPDATE assignments SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateAssignmentStatus(id: Int, isCompleted: Boolean)

    // --- EXAMS ---
    @Query("SELECT * FROM exams ORDER BY date ASC")
    fun getAllExams(): Flow<List<Exam>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExam(exam: Exam): Long

    @Query("DELETE FROM exams WHERE id = :id")
    suspend fun deleteExamById(id: Int)

    @Query("UPDATE exams SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateExamStatus(id: Int, isCompleted: Boolean)

    // --- NOTES ---
    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE subjectId = :subjectId ORDER BY updatedAt DESC")
    fun getNotesBySubject(subjectId: Int): Flow<List<Note>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note): Long

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteById(id: Int)

    // --- REMINDERS ---
    @Query("SELECT * FROM reminders ORDER BY dateTime ASC")
    fun getAllReminders(): Flow<List<Reminder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder): Long

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteReminderById(id: Int)

    @Query("UPDATE reminders SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateReminderStatus(id: Int, isCompleted: Boolean)
}
