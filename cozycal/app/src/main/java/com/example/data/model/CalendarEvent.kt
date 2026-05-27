package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calendar_events")
data class CalendarEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userEmail: String = "guest",
    val title: String,
    val description: String = "",
    val startTime: Long, // Epoch millis
    val endTime: Long, // Epoch millis
    val colorHex: String, // Pastel hex token
    val isCompleted: Boolean = false,
    val category: String = "general", // "study", "self-care", "work", "routine"
    val isRecurring: Boolean = false
)
