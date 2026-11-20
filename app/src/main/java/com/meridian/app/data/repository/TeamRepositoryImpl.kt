package com.meridian.app.data.repository

import com.meridian.app.data.local.MeridianDatabase
import com.meridian.app.data.local.toDomain
import com.meridian.app.domain.model.*
import com.meridian.app.domain.repository.TeamRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate

class TeamRepositoryImpl(private val db: MeridianDatabase) : TeamRepository {
    override fun observeTeam(teamId: String): Flow<Team?> = db.teamDao().observeTeam(teamId).map { it?.toDomain() }
    override suspend fun getTeam(teamId: String): Team? = db.teamDao().getTeam(teamId)?.toDomain()

    override fun observeMembers(teamId: String): Flow<List<TeamMember>> =
        db.teamDao().observeMembers(teamId).map { list -> list.map { it.toDomain() } }

    override fun observeSlots(userId: String, from: Instant, to: Instant): Flow<List<CalendarSlot>> =
        db.calendarDao().observe(userId, from.toEpochMilli(), to.toEpochMilli()).map { list -> list.map { it.toDomain() } }

    override fun observeNextHoliday(from: LocalDate): Flow<Holiday?> =
        db.holidayDao().observeNext(from.toString()).map { it?.toDomain() }

    override fun observeHolidays(from: LocalDate, to: LocalDate): Flow<List<Holiday>> =
        db.holidayDao().observeRange(from.toString(), to.toString()).map { list -> list.map { it.toDomain() } }
}
