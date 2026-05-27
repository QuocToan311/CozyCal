package com.example.data.dao

import androidx.room.*
import com.example.data.model.CalendarEvent
import com.example.data.model.FocusSession
import com.example.data.model.Habit
import com.example.data.model.PetState
import com.example.data.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // --- User Queries ---
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUser(email: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    suspend fun getLoggedInUser(): User?

    @Query("UPDATE users SET isLoggedIn = :isLoggedIn WHERE email = :email")
    suspend fun updateUserLoginStatus(email: String, isLoggedIn: Boolean)

    // --- Calendar Event Queries ---
    @Query("SELECT * FROM calendar_events WHERE userEmail = :email ORDER BY startTime ASC")
    fun getAllEvents(email: String): Flow<List<CalendarEvent>>

    @Query("SELECT * FROM calendar_events WHERE userEmail = :email AND startTime >= :start AND endTime <= :end ORDER BY startTime ASC")
    fun getEventsInPeriod(email: String, start: Long, end: Long): Flow<List<CalendarEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: CalendarEvent): Long

    @Query("UPDATE calendar_events SET isCompleted = :completed WHERE id = :id")
    suspend fun updateEventCompletion(id: Long, completed: Boolean)

    @Delete
    suspend fun deleteEvent(event: CalendarEvent)

    @Query("DELETE FROM calendar_events WHERE id = :id")
    suspend fun deleteEventById(id: Long)

    // --- Habit Queries ---
    @Query("SELECT * FROM habits WHERE userEmail = :email ORDER BY id DESC")
    fun getAllHabits(email: String): Flow<List<Habit>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit): Long

    @Update
    suspend fun updateHabit(habit: Habit)

    @Delete
    suspend fun deleteHabit(habit: Habit)

    // --- Pet State Queries ---
    @Query("SELECT * FROM pet_state WHERE email = :email LIMIT 1")
    fun getPetStateFlow(email: String): Flow<PetState?>

    @Query("SELECT * FROM pet_state WHERE email = :email LIMIT 1")
    suspend fun getPetStateDirect(email: String): PetState?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPetState(petState: PetState)

    // --- Focus Session Queries ---
    @Query("SELECT * FROM focus_sessions WHERE userEmail = :email ORDER BY timestamp DESC")
    fun getAllFocusSessions(email: String): Flow<List<FocusSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFocusSession(session: FocusSession): Long
}
