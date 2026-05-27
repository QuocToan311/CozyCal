package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userEmail: String = "guest",
    val name: String,
    val icon: String = "🌱", // Emoji representation
    val streak: Int = 0,
    val lastCompletedDate: String = "" // "yyyy-MM-dd" Format
)
