package com.meridian.app.data.repository

import androidx.room.withTransaction
import com.meridian.app.data.local.*
import com.meridian.app.data.remote.*
import com.meridian.app.domain.model.AvailabilityStatus
import com.meridian.app.domain.model.GoogleAccountStatus
import com.meridian.app.domain.model.Session
import com.meridian.app.domain.model.SyncState
import com.meridian.app.domain.repository.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.IOException
import java.time.*
import java.time.temporal.TemporalAdjusters

/**
 * Sincronización: primero se EMPUJA la cola local (fichajes en orden, planes) y recién después se
 * descarga. Así una descarga nunca pisa un cambio local pendiente. Offline-first: la UI solo lee Room.
 */
class SyncRepositoryImpl(
    private val db: MeridianDatabase,
    private val remote: RemoteDataSource,
    private val auth: AuthRepository,
    private val prefs: PreferencesRepository,
    private val geofencing: OfficeGeofencing,
    private val clock: Clock,
) : SyncRepository {

    private val mutex = Mutex()
    private val _isSyncing = MutableStateFlow(false)
    override val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    override suspend fun sync(): SyncResult = mutex.withLock {
        val session = auth.currentSession() ?: return@withLock SyncResult.NOT_SIGNED_IN
        _isSyncing.value = true
        try {
            val zone = ZoneId.of(prefs.current().schedule.zoneId)
            pushClockEvents()
            pushPlans(session)
            pullTeam(session)
            pullPlans(zone)
            pullCalendar(session, zone)
            pullHolidays(zone)
            prefs.setLastSync(clock.instant())
            SyncResult.SUCCESS
        } catch (e: UnauthorizedException) {
            auth.setGoogleStatus(GoogleAccountStatus.TOKEN_EXPIRED) // RNF07: mostrar motivo y acción
            SyncResult.AUTH_EXPIRED
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            SyncResult.NETWORK_ERROR
        } finally {
            _isSyncing.value = false
        }
    }

    private suspend fun pushClockEvents() {
        val dao = db.clockEventDao()
        for (e in dao.pending()) {
            if (e.voided) {
                remote.voidClockEvent(e.id)
                dao.delete(e.id)
            } else {
                val response = remote.pushClockEvent(
                    ClockEventDto(e.id, e.type.name, Instant.ofEpochMilli(e.timestampMs).toString(), e.modality.name),
                )
                // FLAGGED: secuencia inválida, el servidor lo guardó para revisión (no se descarta).
                dao.setState(e.id, if (response.status == "FLAGGED") SyncState.FLAGGED else SyncState.SYNCED)
            }
        }
    }

    private suspend fun pushPlans(session: Session) {
        val dao = db.officePlanDao()
        for (p in dao.pending(session.userId)) {
            remote.pushPlan(PlanDto(p.userId, p.date, p.going))
            dao.markSynced(p.userId, p.date, p.going)
        }
    }

    private suspend fun pullTeam(session: Session) {
        val dto = remote.getTeam(session.teamId)
        db.withTransaction {
            val dao = db.teamDao()
            dao.upsertTeam(
                TeamEntity(
                    id = dto.id, name = dto.name,
                    officeName = dto.office?.name, officeLat = dto.office?.latitude,
                    officeLon = dto.office?.longitude, officeRadius = dto.office?.radiusMeters,
                    fixedDays = dto.fixedDays.joinToString(","),
                ),
            )
            dao.deleteMembers(dto.id)
            dao.upsertMembers(dto.members.map { it.toEntity(dto.id) })
        }
        dto.office?.let { geofencing.register(com.meridian.app.domain.model.Office(it.name, it.latitude, it.longitude, it.radiusMeters)) }
            ?: geofencing.unregister()
    }

    private suspend fun pullPlans(zone: ZoneId) {
        val monday = LocalDate.now(clock.withZone(zone)).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val plans = remote.getPlans(monday.toString(), monday.plusDays(13).toString())
        db.officePlanDao().upsertAll(plans.map { OfficePlanEntity(it.userId, it.date, it.going, SyncState.SYNCED) })
    }

    private suspend fun pullCalendar(session: Session, zone: ZoneId) {
        try {
            val today = LocalDate.now(clock.withZone(zone))
            val slots = remote.getCalendar(today.toString())
            db.withTransaction {
                db.calendarDao().deleteFor(session.userId)
                db.calendarDao().insertAll(
                    slots.map {
                        CalendarSlotEntity(it.id, session.userId, Instant.parse(it.start).toEpochMilli(), Instant.parse(it.end).toEpochMilli())
                    },
                )
            }
        } catch (e: GoogleTokenExpiredException) {
            // Se usan los eventos cacheados; el fichaje sigue disponible (sección 15).
            auth.setGoogleStatus(GoogleAccountStatus.TOKEN_EXPIRED)
        }
    }

    private suspend fun pullHolidays(zone: ZoneId) {
        val year = LocalDate.now(clock.withZone(zone)).year
        val dao = db.holidayDao()
        if (dao.countForYear("$year-%") == 0) { // los feriados se bajan una vez por año
            dao.upsertAll(remote.getHolidays(year).map { HolidayEntity(it.date, it.name, "UY") })
        }
    }
}

fun MemberDto.toEntity(teamId: String) = TeamMemberEntity(
    userId = userId, teamId = teamId, name = name, country = country, zoneId = zoneId,
    status = runCatching { AvailabilityStatus.valueOf(status) }.getOrDefault(AvailabilityStatus.SIN_FICHAR),
    busyUntilMs = busyUntil?.let { Instant.parse(it).toEpochMilli() },
    onsiteToday = onsiteToday,
    updatedAtMs = Instant.parse(updatedAt).toEpochMilli(),
)
