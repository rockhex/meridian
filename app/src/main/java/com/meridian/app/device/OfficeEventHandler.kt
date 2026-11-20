package com.meridian.app.device

import com.meridian.app.domain.model.ClockType
import com.meridian.app.domain.repository.AuthRepository
import com.meridian.app.domain.repository.ClockRepository
import com.meridian.app.domain.repository.PreferencesRepository
import java.time.Clock
import java.time.ZoneId

class OfficeEventHandler(
    private val auth: AuthRepository,
    private val prefs: PreferencesRepository,
    private val clockRepository: ClockRepository,
    private val notifications: NotificationHelper,
    private val clock: Clock,
) {
    /** RF03: una sola sugerencia por día, solo si todavía no fichó. */
    suspend fun onEnter() {
        val session = auth.currentSession() ?: return
        val p = prefs.current()
        if (!p.suggestClockIn) return
        val zone = ZoneId.of(p.schedule.zoneId)
        val today = clock.instant().atZone(zone).toLocalDate()
        if (prefs.lastSuggestionDate() == today.toString()) return
        if (clockRepository.eventsForDay(session.userId, today, zone).isNotEmpty()) return
        prefs.setLastSuggestionDate(today.toString())
        notifications.showClockInSuggestion()
    }

    /** Al salir de la oficina con la jornada abierta, recuerda fichar salida (con un toque de confirmación). */
    suspend fun onExit() {
        val session = auth.currentSession() ?: return
        val p = prefs.current()
        if (!p.exitReminder) return
        val zone = ZoneId.of(p.schedule.zoneId)
        val today = clock.instant().atZone(zone).toLocalDate()
        val last = clockRepository.eventsForDay(session.userId, today, zone).lastOrNull()?.type
        if (last != null && last != ClockType.SALIDA) notifications.showExitReminder()
    }
}
