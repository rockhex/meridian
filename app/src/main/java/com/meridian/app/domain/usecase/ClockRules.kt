package com.meridian.app.domain.usecase

import com.meridian.app.domain.model.ClockEvent
import com.meridian.app.domain.model.ClockType
import com.meridian.app.domain.model.ClockType.*
import java.time.Duration
import java.time.Instant

/** Reglas puras de la secuencia de fichajes (RF01). */
object ClockRules {
    fun allowedAfter(last: ClockType?): Set<ClockType> = when (last) {
        null -> setOf(ENTRADA)
        ENTRADA, FIN_DESCANSO -> setOf(INICIO_DESCANSO, SALIDA)
        INICIO_DESCANSO -> setOf(FIN_DESCANSO, SALIDA)
        SALIDA -> emptySet()
    }

    fun isValid(last: ClockType?, next: ClockType) = next in allowedAfter(last)

    /** Acción principal del botón de Inicio: cambia según el momento. */
    fun primaryAction(last: ClockType?): ClockType? = when (last) {
        null -> ENTRADA
        ENTRADA, FIN_DESCANSO -> INICIO_DESCANSO
        INICIO_DESCANSO -> FIN_DESCANSO
        SALIDA -> null
    }

    fun canClockOut(last: ClockType?) = SALIDA in allowedAfter(last)

    fun workedDuration(events: List<ClockEvent>, now: Instant): Duration {
        var total = Duration.ZERO
        var since: Instant? = null
        for (e in events.sortedBy { it.timestamp }) {
            when (e.type) {
                ENTRADA, FIN_DESCANSO -> since = e.timestamp
                INICIO_DESCANSO, SALIDA -> {
                    since?.let { total += Duration.between(it, e.timestamp) }
                    since = null
                }
            }
        }
        since?.let { if (now.isAfter(it)) total += Duration.between(it, now) }
        return total
    }
}
