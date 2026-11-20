package com.meridian.app.domain.usecase

import com.meridian.app.domain.model.*
import com.meridian.app.domain.repository.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate

/**
 * RF04: arma la semana (lun-vie). Los días fijos del equipo aparecen marcados por defecto;
 * los feriados no se pueden marcar; el cambio propio queda pendiente hasta sincronizar.
 */
class ObserveOfficeWeekUseCase(
    private val teamRepository: TeamRepository,
    private val planRepository: OfficePlanRepository,
) {
    operator fun invoke(session: Session, weekStart: LocalDate): Flow<OfficeWeek> {
        val weekEnd = weekStart.plusDays(4)
        return combine(
            teamRepository.observeTeam(session.teamId),
            teamRepository.observeMembers(session.teamId),
            planRepository.observeRange(weekStart, weekEnd),
            teamRepository.observeHolidays(weekStart, weekEnd),
        ) { team, members, plans, holidays ->
            val fixed = team?.fixedOfficeDays.orEmpty()
            val holidayByDate = holidays.associateBy { it.date }
            val days = (0L..4L).map { i ->
                val date = weekStart.plusDays(i)
                val holiday = holidayByDate[date]
                val isFixed = date.dayOfWeek in fixed
                val own = plans.firstOrNull { it.userId == session.userId && it.date == date }
                val going = holiday == null && (own?.going ?: isFixed)
                val others = members
                    .filter { it.userId != session.userId }
                    .filter { m ->
                        holiday == null &&
                            (plans.firstOrNull { it.userId == m.userId && it.date == date }?.going ?: isFixed)
                    }
                WeekDayPlan(
                    date = date,
                    isFixed = isFixed,
                    holidayName = holiday?.name,
                    going = going,
                    pendingSync = own?.pending == true,
                    attendees = others.map { it.name },
                    goingCount = others.size + if (going) 1 else 0,
                )
            }
            OfficeWeek(
                office = team?.office,
                days = days,
                fixedDays = fixed.sorted(),
                hasAnyPlan = days.any { it.going || it.attendees.isNotEmpty() },
                hasPending = days.any { it.pendingSync },
            )
        }
    }
}

/** Última escritura del propio usuario gana (sección 15 del documento). */
class ToggleOfficeDayUseCase(
    private val auth: AuthRepository,
    private val planRepository: OfficePlanRepository,
    private val syncTrigger: SyncTrigger,
) {
    suspend operator fun invoke(date: LocalDate, going: Boolean) {
        val session = auth.currentSession() ?: return
        planRepository.setGoing(session.userId, date, going)
        syncTrigger.requestSync()
    }
}

class SignInWithGoogleUseCase(private val auth: AuthRepository) {
    suspend operator fun invoke(idToken: String): SignInResult = auth.signIn(idToken)
}
