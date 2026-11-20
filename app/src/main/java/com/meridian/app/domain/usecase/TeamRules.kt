package com.meridian.app.domain.usecase

import com.meridian.app.domain.model.AvailabilityStatus.*
import com.meridian.app.domain.model.TeamMember
import com.meridian.app.domain.model.TeamSummary
import java.time.Duration
import java.time.Instant

object TeamRules {
    /** RF02: valor inicial, ajustable durante las pruebas. */
    val STALE_AFTER: Duration = Duration.ofMinutes(15)

    fun isStale(updatedAt: Instant, now: Instant) = Duration.between(updatedAt, now) > STALE_AFTER

    /** Sin conexión la app estima con los últimos datos: una llamada que ya terminó pasa a disponible. */
    fun estimate(member: TeamMember, now: Instant): TeamMember {
        val until = member.busyUntil
        return if (member.status == EN_LLAMADA && until != null && !now.isBefore(until)) {
            member.copy(status = DISPONIBLE, busyUntil = null)
        } else member
    }

    fun summarize(members: List<TeamMember>) = TeamSummary(
        total = members.size,
        available = members.count { it.status == DISPONIBLE },
        inCall = members.count { it.status == EN_LLAMADA },
        notClocked = members.count { it.status == SIN_FICHAR },
    )
}
