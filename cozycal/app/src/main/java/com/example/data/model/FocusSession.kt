package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "focus_sessions")
data class FocusSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userEmail: String = "guest",
    val durationMinutes: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val petExpGained: Int = 15,
    val name: String = "Cozy Focus"
)
