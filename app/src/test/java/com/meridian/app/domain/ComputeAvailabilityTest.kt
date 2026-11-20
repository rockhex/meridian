package com.meridian.app.domain

import com.meridian.app.domain.model.*
import com.meridian.app.domain.model.AvailabilityStatus.*
import com.meridian.app.domain.model.ClockType.*
import com.meridian.app.domain.usecase.ComputeAvailability
import com.meridian.app.domain.usecase.TeamRules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalTime

class ComputeAvailabilityTest {
    private val schedule = WorkSchedule(LocalTime.of(9, 0), LocalTime.of(18, 0), "UTC")
    private val compute = ComputeAvailability()
    private fun at(h: Int, m: Int = 0) = Instant.parse("2026-11-18T%02d:%02d:00Z".format(h, m))
    private fun ev(type: ClockType, t: Instant) = ClockEvent("$type$t", "u1", type, t, Modality.REMOTO, SyncState.SYNCED)

    @Test fun `sin fichar dentro del horario`() =
        assertEquals(SIN_FICHAR, compute(at(10), schedule, emptyList(), emptyList()).status)

    @Test fun `disponible tras la entrada`() =
        assertEquals(DISPONIBLE, compute(at(10), schedule, listOf(ev(ENTRADA, at(9))), emptyList()).status)

    @Test fun `en llamada informa hasta cuando`() {
        val slot = CalendarSlot("c", at(11), at(11, 30))
        val r = compute(at(11, 10), schedule, listOf(ev(ENTRADA, at(9))), listOf(slot))
        assertEquals(EN_LLAMADA, r.status)
        assertEquals(at(11, 30), r.busyUntil)
    }

    @Test fun `en descanso`() =
        assertEquals(EN_DESCANSO, compute(at(13), schedule, listOf(ev(ENTRADA, at(9)), ev(INICIO_DESCANSO, at(12, 30))), emptyList()).status)

    @Test fun `tras fichar salida o fuera de horario no se muestra actividad`() {
        assertEquals(FUERA_DE_HORARIO, compute(at(17), schedule, listOf(ev(ENTRADA, at(9)), ev(SALIDA, at(16))), emptyList()).status)
        assertEquals(FUERA_DE_HORARIO, compute(at(20), schedule, listOf(ev(ENTRADA, at(9))), emptyList()).status)
    }

    @Test fun `offline una llamada ya terminada se estima como disponible`() {
        val member = TeamMember("u2", "Lucas", "AR", "UTC", EN_LLAMADA, at(11, 30), false, at(11))
        assertEquals(DISPONIBLE, TeamRules.estimate(member, at(11, 45)).status)
        assertEquals(EN_LLAMADA, TeamRules.estimate(member, at(11, 10)).status)
    }

    @Test fun `un estado de mas de 15 minutos esta desactualizado`() {
        assertTrue(TeamRules.isStale(at(10), at(10, 16)))
        assertEquals(false, TeamRules.isStale(at(10), at(10, 10)))
    }
}
