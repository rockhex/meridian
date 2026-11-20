package com.meridian.app.data.repository

import com.meridian.app.data.local.ClockEventDao
import com.meridian.app.data.local.toDomain
import com.meridian.app.data.local.toEntity
import com.meridian.app.domain.model.ClockEvent
import com.meridian.app.domain.repository.ClockRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZoneId

class ClockRepositoryImpl(private val dao: ClockEventDao) : ClockRepository {

    private fun bounds(date: LocalDate, zone: ZoneId): Pair<Long, Long> =
        date.atStartOfDay(zone).toInstant().toEpochMilli() to
            date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()

    override fun observeDay(userId: String, date: LocalDate, zone: ZoneId): Flow<List<ClockEvent>> {
        val (from, to) = bounds(date, zone)
        return dao.observeBetween(userId, from, to).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun eventsForDay(userId: String, date: LocalDate, zone: ZoneId): List<ClockEvent> {
        val (from, to) = bounds(date, zone)
        return dao.getBetween(userId, from, to).map { it.toDomain() }
    }

    override suspend fun register(event: ClockEvent) { dao.insert(event.toEntity()) }
    override suspend fun void(id: String) = dao.markVoided(id)
    override fun observePendingCount(): Flow<Int> = dao.observePendingCount()
}
