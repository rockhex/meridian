package com.meridian.app.domain

import com.meridian.app.domain.model.*
import com.meridian.app.domain.model.ClockType.*
import com.meridian.app.domain.usecase.ClockRules
import org.junit.Assert.*
import org.junit.Test
import java.time.Duration
import java.time.Instant

class ClockRulesTest {
    private fun ev(type: ClockType, iso: String) =
        ClockEvent(iso + type, "u1", type, Instant.parse(iso), Modality.REMOTO, SyncState.PENDING)

    @Test fun `la secuencia valida es entrada, descanso, fin de descanso, salida`() {
        assertTrue(ClockRules.isValid(null, ENTRADA))
        assertTrue(ClockRules.isValid(ENTRADA, INICIO_DESCANSO))
        assertTrue(ClockRules.isValid(INICIO_DESCANSO, FIN_DESCANSO))
        assertTrue(ClockRules.isValid(FIN_DESCANSO, SALIDA))
    }

    @Test fun `rechaza fichajes fuera de secuencia`() {
        assertFalse(ClockRules.isValid(null, SALIDA))
        assertFalse(ClockRules.isValid(ENTRADA, ENTRADA))
        assertFalse(ClockRules.isValid(ENTRADA, FIN_DESCANSO))
        assertFalse(ClockRules.isValid(SALIDA, ENTRADA))
    }

    @Test fun `la accion principal cambia segun el momento`() {
        assertEquals(ENTRADA, ClockRules.primaryAction(null))
        assertEquals(INICIO_DESCANSO, ClockRules.primaryAction(ENTRADA))
        assertEquals(FIN_DESCANSO, ClockRules.primaryAction(INICIO_DESCANSO))
        assertNull(ClockRules.primaryAction(SALIDA))
    }

    @Test fun `horas trabajadas descuentan el descanso`() {
        val events = listOf(
            ev(ENTRADA, "2026-11-18T09:00:00Z"),
            ev(INICIO_DESCANSO, "2026-11-18T13:00:00Z"),
            ev(FIN_DESCANSO, "2026-11-18T14:00:00Z"),
        )
        val worked = ClockRules.workedDuration(events, Instant.parse("2026-11-18T16:30:00Z"))
        assertEquals(Duration.ofHours(6).plusMinutes(30), worked) // 4h + 2h30 en curso
    }
}
