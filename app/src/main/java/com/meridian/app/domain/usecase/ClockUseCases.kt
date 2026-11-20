package com.meridian.app.domain.usecase

import com.meridian.app.domain.model.*
import com.meridian.app.domain.repository.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.UUID

sealed interface RegisterResult {
    data class Registered(val event: ClockEvent) : RegisterResult
    data class InvalidSequence(val last: ClockType?) : RegisterResult
    data object NotSignedIn : RegisterResult
}

/**
 * RF01 / RNF06: el fichaje se guarda con la hora del dispositivo y un UUID, en estado PENDING,
 * ANTES de intentar cualquier red. La sincronización posterior es idempotente por ese UUID.
 */
class RegisterClockEventUseCase(
    private val auth: AuthRepository,
    private val prefs: PreferencesRepository,
    private val clockRepository: ClockRepository,
    private val syncTrigger: SyncTrigger,
    private val clock: Clock,
    private val idGenerator: () -> String = { UUID.randomUUID().toString() },
) {
    suspend operator fun invoke(type: ClockType, modality: Modality): RegisterResult {
        val session = auth.currentSession() ?: return RegisterResult.NotSignedIn
        val zone = ZoneId.of(prefs.current().schedule.zoneId)
        val now = clock.instant()
        val today = now.atZone(zone).toLocalDate()
        val last = clockRepository.eventsForDay(session.userId, today, zone).lastOrNull()?.type
        if (!ClockRules.isValid(last, type)) return RegisterResult.InvalidSequence(last)

        val event = ClockEvent(idGenerator(), session.userId, type, now, modality, SyncState.PENDING)
        clockRepository.register(event)
        syncTrigger.requestSync()
        return RegisterResult.Registered(event)
    }
}

/** Deshacer un fichaje por error durante unos segundos (criterio de prevención de errores). */
class UndoClockEventUseCase(
    private val clockRepository: ClockRepository,
    private val syncTrigger: SyncTrigger,
) {
    suspend operator fun invoke(eventId: String) {
        clockRepository.void(eventId)
        syncTrigger.requestSync()
    }
}

fun tickerFlow(clock: Clock, periodMs: Long = 30_000): Flow<Instant> = flow {
    while (true) {
        emit(clock.instant())
        delay(periodMs)
    }
}

/** Disponibilidad propia, recalculada cada 30 s (se usa para la fila propia en Equipo). */
class ObserveOwnAvailabilityUseCase(
    private val prefs: PreferencesRepository,
    private val clockRepository: ClockRepository,
    private val teamRepository: TeamRepository,
    private val computeAvailability: ComputeAvailability,
    private val clock: Clock,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(userId: String): Flow<Availability> =
        prefs.preferences.distinctUntilChanged().flatMapLatest { p ->
            val zone = ZoneId.of(p.schedule.zoneId)
            tickerFlow(clock).map { it.atZone(zone).toLocalDate() }.distinctUntilChanged().flatMapLatest { date ->
                val from = date.atStartOfDay(zone).toInstant()
                val to = date.plusDays(1).atStartOfDay(zone).toInstant()
                combine(
                    clockRepository.observeDay(userId, date, zone),
                    teamRepository.observeSlots(userId, from, to),
                    tickerFlow(clock),
                ) { events, slots, now -> computeAvailability(now, p.schedule, events, slots) }
            }
        }
}
