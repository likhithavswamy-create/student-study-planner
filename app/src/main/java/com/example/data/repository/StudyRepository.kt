package com.example.data.repository

import com.example.data.dao.PlannerDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

class StudyRepository(private val plannerDao: PlannerDao) {

    // --- SUBJECTS ---
    val allSubjects: Flow<List<Subject>> = plannerDao.getAllSubjects()

    suspend fun insertSubject(subject: Subject): Long {
        return plannerDao.insertSubject(subject)
    }

    suspend fun deleteSubject(id: Int) {
        plannerDao.deleteSubjectById(id)
    }

    suspend fun getSubjectById(id: Int): Subject? {
        return plannerDao.getSubjectById(id)
    }

    // --- TIMETABLE SLOTS ---
    val allTimetableSlots: Flow<List<TimetableSlot>> = plannerDao.getAllTimetableSlots()

    fun getTimetableSlotsForDay(day: String): Flow<List<TimetableSlot>> {
        return plannerDao.getTimetableSlotsForDay(day)
    }

    suspend fun insertTimetableSlot(slot: TimetableSlot): Long {
        return plannerDao.insertTimetableSlot(slot)
    }

    suspend fun deleteTimetableSlot(id: Int) {
        plannerDao.deleteTimetableSlotById(id)
    }

    // --- ASSIGNMENTS ---
    val allAssignments: Flow<List<Assignment>> = plannerDao.getAllAssignments()

    suspend fun insertAssignment(assignment: Assignment): Long {
        return plannerDao.insertAssignment(assignment)
    }

    suspend fun deleteAssignment(id: Int) {
        plannerDao.deleteAssignmentById(id)
    }

    suspend fun updateAssignmentStatus(id: Int, isCompleted: Boolean) {
        plannerDao.updateAssignmentStatus(id, isCompleted)
    }

    // --- EXAMS ---
    val allExams: Flow<List<Exam>> = plannerDao.getAllExams()

    suspend fun insertExam(exam: Exam): Long {
        return plannerDao.insertExam(exam)
    }

    suspend fun deleteExam(id: Int) {
        plannerDao.deleteExamById(id)
    }

    suspend fun updateExamStatus(id: Int, isCompleted: Boolean) {
        plannerDao.updateExamStatus(id, isCompleted)
    }

    // --- NOTES ---
    val allNotes: Flow<List<Note>> = plannerDao.getAllNotes()

    fun getNotesBySubject(subjectId: Int): Flow<List<Note>> {
        return plannerDao.getNotesBySubject(subjectId)
    }

    suspend fun insertNote(note: Note): Long {
        return plannerDao.insertNote(note)
    }

    suspend fun deleteNote(id: Int) {
        plannerDao.deleteNoteById(id)
    }

    // --- REMINDERS ---
    val allReminders: Flow<List<Reminder>> = plannerDao.getAllReminders()

    suspend fun insertReminder(reminder: Reminder): Long {
        return plannerDao.insertReminder(reminder)
    }

    suspend fun deleteReminder(id: Int) {
        plannerDao.deleteReminderById(id)
    }

    suspend fun updateReminderStatus(id: Int, isCompleted: Boolean) {
        plannerDao.updateReminderStatus(id, isCompleted)
    }
}
