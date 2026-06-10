package com.example.ui.util

import java.text.SimpleDateFormat
import java.util.*

object DateTimeFormatter {

    fun formatDate(timestamp: Long): String {
        return try {
            val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            sdf.format(Date(timestamp))
        } catch (e: Exception) {
            "Unknown date"
        }
    }

    fun formatTime(hour: Int, minute: Int): String {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(calendar.time)
    }

    fun formatDateTime(timestamp: Long): String {
        return try {
            val sdf = SimpleDateFormat("MMM d, yyyy 'at' hh:mm a", Locale.getDefault())
            sdf.format(Date(timestamp))
        } catch (e: Exception) {
            "Unknown"
        }
    }

    fun getRemainingDaysText(timestamp: Long): String {
        val diff = timestamp - System.currentTimeMillis()
        if (diff < 0) {
            val daysOverdue = Math.abs(diff / (1000 * 60 * 60 * 24))
            return if (daysOverdue == 0L) "Due today" else "$daysOverdue days ago"
        }
        val days = diff / (1000 * 60 * 60 * 24)
        return when {
            days == 0L -> {
                val hours = diff / (1000 * 60 * 60)
                if (hours == 0L) "Due now" else "In $hours hours"
            }
            days == 1L -> "Tomorrow"
            else -> "In $days days"
        }
    }

    fun parseTime(timeStr: String): Calendar {
        val cal = Calendar.getInstance()
        try {
            val parts = timeStr.split(":")
            val hr = parts[0].trim().toInt()
            val min = parts[1].trim().toInt()
            cal.set(Calendar.HOUR_OF_DAY, hr)
            cal.set(Calendar.MINUTE, min)
        } catch (e: Exception) {}
        return cal
    }
}
