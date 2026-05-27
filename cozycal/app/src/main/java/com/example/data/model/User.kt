package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val email: String,
    val name: String,
    val passwordHash: String,
    val isLoggedIn: Boolean = false,
    val hasCompletedOnboarding: Boolean = false
)
