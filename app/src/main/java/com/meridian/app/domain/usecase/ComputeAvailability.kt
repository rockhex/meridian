package com.meridian.app.domain.usecase

import com.meridian.app.domain.model.*
import com.meridian.app.domain.model.AvailabilityStatus.*
import com.meridian.app.domain.model.ClockType.INICIO_DESCANSO
import com.meridian.app.domain.model.ClockType.SALIDA
import java.time.Instant
import java.time.ZoneId

/**
 * Estado del usuario a partir de fichajes + horario + eventos de calendario (solo horario).
 * RNF04: fuera del horario o tras fichar salida => FUERA_DE_HORARIO, sin más detalle.
 */
class ComputeAvailability {
    operator fun invoke(
        now: Instant,
        schedule: WorkSchedule,
        events: List<ClockEvent>,
        slots: List<CalendarSlot>,
    ): Availability {
        val last = events.maxByOrNull { it.timestamp }?.type
        val local = now.atZone(ZoneId.of(schedule.zoneId)).toLocalTime()
        val inHours = !local.isBefore(schedule.start) && local.isBefore(schedule.end)
        if (last == SALIDA || !inHours) return Availability(FUERA_DE_HORARIO)
        return when (last) {
            null -> Availability(SIN_FICHAR)
            INICIO_DESCANSO -> Availability(EN_DESCANSO)
            else -> {
                val call = slots.filter { it.isActive(now) }.maxByOrNull { it.end }
                if (call != null) Availability(EN_LLAMADA, call.end) else Availability(DISPONIBLE)
            }
        }
    }
}
