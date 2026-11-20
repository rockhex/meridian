package com.meridian.app.domain.model

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

enum class Role { EMPLEADO, LIDER }
enum class ClockType { ENTRADA, INICIO_DESCANSO, FIN_DESCANSO, SALIDA }
enum class Modality { PRESENCIAL, REMOTO }
enum class SyncState { PENDING, SYNCED, FLAGGED }
enum class AvailabilityStatus { SIN_FICHAR, DISPONIBLE, EN_LLAMADA, EN_DESCANSO, FUERA_DE_HORARIO }
enum class GoogleAccountStatus { CONNECTED, TOKEN_EXPIRED, DISCONNECTED }

data class Session(
    val userId: String,
    val name: String,
    val email: String,
    val role: Role,
    val teamId: String,
    val country: String,
) {
    val isLeader: Boolean get() = role == Role.LIDER
    val firstName: String get() = name.substringBefore(' ')
    val initials: String
        get() = name.split(' ').take(2).mapNotNull { it.firstOrNull() }.joinToString("").uppercase()
}

data class Office(val name: String, val latitude: Double, val longitude: Double, val radiusMeters: Float)

data class Team(
    val id: String,
    val name: String,
    val office: Office?,
    val fixedOfficeDays: Set<DayOfWeek>,
)

/** Fichaje. [id] lo genera el dispositivo (UUID) para que el servidor descarte duplicados (RF01). */
data class ClockEvent(
    val id: String,
    val userId: String,
    val type: ClockType,
    val timestamp: Instant,
    val modality: Modality,
    val syncState: SyncState,
)

/** Solo horario del evento: nunca su contenido (RNF03). */
data class CalendarSlot(val id: String, val start: Instant, val end: Instant) {
    fun isActive(now: Instant) = !now.isBefore(start) && now.isBefore(end)
}

data class TeamMember(
    val userId: String,
    val name: String,
    val country: String,
    val zoneId: String,
    val status: AvailabilityStatus,
    val busyUntil: Instant?,
    val onsiteToday: Boolean,
    val updatedAt: Instant,
)

data class TeamSummary(val total: Int, val available: Int, val inCall: Int, val notClocked: Int)

data class Holiday(val date: LocalDate, val name: String, val country: String)

data class WorkSchedule(val start: LocalTime, val end: LocalTime, val zoneId: String)

data class UserPreferences(
    val schedule: WorkSchedule,
    val suggestClockIn: Boolean,
    val exitReminder: Boolean,
    val pendingSyncAlert: Boolean,
)

data class PlanEntry(val userId: String, val date: LocalDate, val going: Boolean, val pending: Boolean)

data class WeekDayPlan(
    val date: LocalDate,
    val isFixed: Boolean,
    val holidayName: String?,
    val going: Boolean,
    val pendingSync: Boolean,
    val attendees: List<String>,
    val goingCount: Int,
)

data class OfficeWeek(
    val office: Office?,
    val days: List<WeekDayPlan>,
    val fixedDays: List<DayOfWeek>,
    val hasAnyPlan: Boolean,
    val hasPending: Boolean,
)

data class Availability(val status: AvailabilityStatus, val busyUntil: Instant? = null)
