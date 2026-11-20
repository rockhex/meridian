package com.meridian.app.data.local

import com.meridian.app.domain.model.*
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate

fun ClockEventEntity.toDomain() = ClockEvent(id, userId, type, Instant.ofEpochMilli(timestampMs), modality, syncState)
fun ClockEvent.toEntity() = ClockEventEntity(id, userId, type, timestamp.toEpochMilli(), modality, syncState)

fun TeamEntity.toDomain() = Team(
    id = id,
    name = name,
    office = if (officeName != null && officeLat != null && officeLon != null && officeRadius != null) {
        Office(officeName, officeLat, officeLon, officeRadius)
    } else null,
    fixedOfficeDays = fixedDays.split(',').filter { it.isNotBlank() }.map { DayOfWeek.valueOf(it) }.toSet(),
)

fun TeamMemberEntity.toDomain() = TeamMember(
    userId = userId, name = name, country = country, zoneId = zoneId, status = status,
    busyUntil = busyUntilMs?.let(Instant::ofEpochMilli), onsiteToday = onsiteToday,
    updatedAt = Instant.ofEpochMilli(updatedAtMs),
)

fun CalendarSlotEntity.toDomain() = CalendarSlot(id, Instant.ofEpochMilli(startMs), Instant.ofEpochMilli(endMs))

fun HolidayEntity.toDomain() = Holiday(LocalDate.parse(date), name, country)
