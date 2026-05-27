package com.example.data.repository

import com.example.data.dao.AppDao
import com.example.data.model.CalendarEvent
import com.example.data.model.FocusSession
import com.example.data.model.Habit
import com.example.data.model.PetState
import com.example.data.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart

class AppRepository(private val appDao: AppDao) {

    // --- User Accounts ---
    suspend fun getUser(email: String): User? = appDao.getUser(email)

    suspend fun insertUser(user: User) = appDao.insertUser(user)

    suspend fun updateUser(user: User) = appDao.updateUser(user)

    suspend fun getLoggedInUser(): User? = appDao.getLoggedInUser()

    suspend fun updateUserLoginStatus(email: String, isLoggedIn: Boolean) = appDao.updateUserLoginStatus(email, isLoggedIn)

    // --- Events ---
    fun getAllEvents(email: String): Flow<List<CalendarEvent>> = appDao.getAllEvents(email)

    fun getEventsInPeriod(email: String, start: Long, end: Long): Flow<List<CalendarEvent>> {
        return appDao.getEventsInPeriod(email, start, end)
    }

    suspend fun insertEvent(event: CalendarEvent): Long {
        return appDao.insertEvent(event)
    }

    suspend fun getEventById(id: Long): CalendarEvent? = appDao.getEventById(id)

    suspend fun updateEventCompletion(id: Long, completed: Boolean) {
        appDao.updateEventCompletion(id, completed)
    }

    suspend fun deleteEvent(event: CalendarEvent) {
        appDao.deleteEvent(event)
    }

    suspend fun deleteEventById(id: Long) {
        appDao.deleteEventById(id)
    }

    // --- Habits ---
    fun getAllHabits(email: String): Flow<List<Habit>> = appDao.getAllHabits(email)

    suspend fun insertHabit(habit: Habit): Long {
        return appDao.insertHabit(habit)
    }

    suspend fun updateHabit(habit: Habit) {
        appDao.updateHabit(habit)
    }

    suspend fun deleteHabit(habit: Habit) {
        appDao.deleteHabit(habit)
    }

    // --- Pet State ---
    fun getPetStateFlow(email: String): Flow<PetState?> {
        return appDao.getPetStateFlow(email)
    }

    suspend fun getPetStateDirect(email: String): PetState {
        return appDao.getPetStateDirect(email) ?: PetState(email = email).also {
            appDao.insertPetState(it)
        }
    }

    suspend fun insertPetState(state: PetState) {
        appDao.insertPetState(state)
    }

    // --- Focus Sessions ---
    fun getAllFocusSessions(email: String): Flow<List<FocusSession>> = appDao.getAllFocusSessions(email)

    suspend fun insertFocusSession(session: FocusSession): Long {
        return appDao.insertFocusSession(session)
    }
}
