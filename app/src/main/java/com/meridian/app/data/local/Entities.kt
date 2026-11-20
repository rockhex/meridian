package com.meridian.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.meridian.app.domain.model.AvailabilityStatus
import com.meridian.app.domain.model.ClockType
import com.meridian.app.domain.model.Modality
import com.meridian.app.domain.model.SyncState

@Entity(tableName = "clock_events")
data class ClockEventEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val type: ClockType,
    val timestampMs: Long,
    val modality: Modality,
    val syncState: SyncState,
    /** Deshecho por el usuario; se informa al servidor y luego se borra. */
    val voided: Boolean = false,
)

@Entity(tableName = "team")
data class TeamEntity(
    @PrimaryKey val id: String,
    val name: String,
    val officeName: String?,
    val officeLat: Double?,
    val officeLon: Double?,
    val officeRadius: Float?,
    /** DayOfWeek separados por coma. */
    val fixedDays: String,
)

@Entity(tableName = "team_members")
data class TeamMemberEntity(
    @PrimaryKey val userId: String,
    val teamId: String,
    val name: String,
    val country: String,
    val zoneId: String,
    val status: AvailabilityStatus,
    val busyUntilMs: Long?,
    val onsiteToday: Boolean,
    val updatedAtMs: Long,
)

@Entity(tableName = "calendar_slots")
data class CalendarSlotEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val startMs: Long,
    val endMs: Long,
)

@Entity(tableName = "holidays")
data class HolidayEntity(
    @PrimaryKey val date: String, // ISO yyyy-MM-dd
    val name: String,
    val country: String,
)

@Entity(tableName = "office_plan", primaryKeys = ["userId", "date"])
data class OfficePlanEntity(
    val userId: String,
    val date: String, // ISO yyyy-MM-dd
    val going: Boolean,
    val syncState: SyncState,
)
