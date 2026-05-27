package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pet_state")
data class PetState(
    @PrimaryKey val email: String = "guest",
    val name: String = "Mochi",
    val mood: Int = 85, // 0 - 100
    val energy: Int = 60, // 0 - 100
    val level: Int = 1,
    val exp: Int = 0,
    val coins: Int = 150,
    val activeAccessories: String = "none", // Comma-separated accessory IDs active in the room
    val unlockedAccessories: String = "none", // Comma-separated accessory IDs owned by the user
    // Premium Extended onboarding/auth fields
    val petType: String = "Cat", // "Cat", "Bunny", "Fox", "Bear", "Penguin", "Hamster"
    val petPersonality: String = "Năng động", // "Năng động", "Thư thái", "Tập trung", "Tò mò"
    val hasCompletedOnboarding: Boolean = false,
    val isLoggedIn: Boolean = false,
    val userName: String = "Quốc Toàn",
    val productivityGoal: String = "Tập trung sâu",
    val isCalendarConnected: Boolean = false
)
