package com.meridian.app.domain.repository

import com.meridian.app.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

sealed interface SignInResult {
    data object Success : SignInResult
    data object WrongDomain : SignInResult
    data object Offline : SignInResult
    data object Error : SignInResult
}

enum class SyncResult { SUCCESS, NETWORK_ERROR, AUTH_EXPIRED, NOT_SIGNED_IN }

interface AuthRepository {
    val session: Flow<Session?>
    val googleStatus: Flow<GoogleAccountStatus>
    suspend fun currentSession(): Session?
    suspend fun signIn(idToken: String): SignInResult
    suspend fun reconnectGoogle(idToken: String): Boolean
    suspend fun setGoogleStatus(status: GoogleAccountStatus)
    suspend fun signOut()
}

interface PreferencesRepository {
    val preferences: Flow<UserPreferences>
    val lastSync: Flow<Instant?>
    suspend fun current(): UserPreferences
    suspend fun setSchedule(start: LocalTime, end: LocalTime, zoneId: String)
    suspend fun setSuggestClockIn(enabled: Boolean)
    suspend fun setExitReminder(enabled: Boolean)
    suspend fun setPendingSyncAlert(enabled: Boolean)
    suspend fun setLastSync(instant: Instant)
    suspend fun lastSuggestionDate(): String?
    suspend fun setLastSuggestionDate(date: String)
}

interface ClockRepository {
    fun observeDay(userId: String, date: LocalDate, zone: ZoneId): Flow<List<ClockEvent>>
    suspend fun eventsForDay(userId: String, date: LocalDate, zone: ZoneId): List<ClockEvent>
    /** Persiste de forma durable antes de volver (RNF06). */
    suspend fun register(event: ClockEvent)
    suspend fun void(id: String)
    fun observePendingCount(): Flow<Int>
}

interface TeamRepository {
    fun observeTeam(teamId: String): Flow<Team?>
    suspend fun getTeam(teamId: String): Team?
    fun observeMembers(teamId: String): Flow<List<TeamMember>>
    fun observeSlots(userId: String, from: Instant, to: Instant): Flow<List<CalendarSlot>>
    fun observeNextHoliday(from: LocalDate): Flow<Holiday?>
    fun observeHolidays(from: LocalDate, to: LocalDate): Flow<List<Holiday>>
}

interface OfficePlanRepository {
    fun observeRange(from: LocalDate, to: LocalDate): Flow<List<PlanEntry>>
    suspend fun setGoing(userId: String, date: LocalDate, going: Boolean)
}

interface SyncRepository {
    val isSyncing: StateFlow<Boolean>
    suspend fun sync(): SyncResult
}

interface ConnectivityRepository {
    val isOnline: Flow<Boolean>
}

/** Pide una sincronización diferida (WorkManager en la implementación). */
interface SyncTrigger {
    fun requestSync()
}

/** Geofencing del SO sobre la oficina del equipo (RNF05). */
interface OfficeGeofencing {
    fun register(office: Office)
    fun unregister()
}
