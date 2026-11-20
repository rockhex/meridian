package com.meridian.app.data.remote

import com.meridian.app.domain.model.AvailabilityStatus
import com.meridian.app.domain.model.AvailabilityStatus.*
import kotlinx.coroutines.delay
import java.time.Clock
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

/**
 * Backend simulado en memoria para correr la app sin servidor (BuildConfig.USE_FAKE_BACKEND).
 * Imita el contrato real: valida dominio, deduplica fichajes por UUID y conserva los planes enviados.
 */
class FakeRemoteDataSource(private val clock: Clock) : RemoteDataSource {

    private val zone = ZoneId.of("America/Montevideo")
    private val seen = mutableSetOf<String>()
    private val plans = mutableListOf<PlanDto>()

    private fun monday(): LocalDate =
        LocalDate.now(clock.withZone(zone)).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    init {
        val mon = monday()
        fun p(user: String, offset: Long, going: Boolean = true) =
            plans.add(PlanDto(user, mon.plusDays(offset).toString(), going))
        p("u-lucas", 1); p("u-lucas", 2); p("u-lucas", 3)
        p("u-fede", 1); p("u-fede", 3)
        p("u-ana", 1, false); p("u-ana", 3); p("u-ana", 4)
        p("u-diego", 2)
    }

    override suspend fun signIn(idToken: String): SessionDto {
        delay(900)
        val email = idToken.removePrefix("demo:")
        if (!email.endsWith("@meridian.example")) throw WrongDomainException()
        return SessionDto(
            token = "demo-token", userId = "u-carla", name = "Carla Ruiz", email = email,
            role = "LIDER", teamId = "t-backend", country = "Uruguay", zoneId = zone.id,
        )
    }

    override suspend fun linkGoogle(idToken: String) { delay(500) }

    override suspend fun getTeam(teamId: String): TeamDto {
        delay(400)
        val now = clock.instant()
        fun m(id: String, name: String, country: String, tz: String, status: AvailabilityStatus,
              busyMin: Long? = null, onsite: Boolean = false) = MemberDto(
            id, name, country, tz, status.name,
            busyMin?.let { now.plus(Duration.ofMinutes(it)).toString() }, onsite, now.toString(),
        )
        return TeamDto(
            id = teamId,
            name = "Equipo Backend",
            office = OfficeDto("Oficina Montevideo", -34.9058, -56.1913, 150f),
            fixedDays = listOf("TUESDAY", "THURSDAY"),
            members = listOf(
                m("u-lucas", "Lucas Martínez", "Argentina", "America/Argentina/Buenos_Aires", EN_LLAMADA, 28),
                m("u-carla", "Carla Ruiz", "Uruguay", "America/Montevideo", DISPONIBLE, onsite = true),
                m("u-fede", "Federico Díaz", "Uruguay", "America/Montevideo", EN_DESCANSO, onsite = true),
                m("u-ana", "Ana López", "Chile", "America/Santiago", DISPONIBLE),
                m("u-sofia", "Sofía Núñez", "España", "Europe/Madrid", FUERA_DE_HORARIO),
                m("u-diego", "Diego Fernández", "Uruguay", "America/Montevideo", SIN_FICHAR),
            ),
        )
    }

    override suspend fun pushClockEvent(event: ClockEventDto): ClockPushResponse {
        delay(200)
        return ClockPushResponse(if (seen.add(event.id)) "ACCEPTED" else "DUPLICATE")
    }

    override suspend fun voidClockEvent(id: String) { delay(100); seen.remove(id) }

    override suspend fun getCalendar(date: String): List<CalendarSlotDto> {
        val day = LocalDate.parse(date)
        fun slot(id: String, h1: Int, m1: Int, h2: Int, m2: Int) = CalendarSlotDto(
            id, day.atTime(h1, m1).atZone(zone).toInstant().toString(),
            day.atTime(h2, m2).atZone(zone).toInstant().toString(),
        )
        return listOf(slot("c1", 11, 0, 11, 30), slot("c2", 15, 0, 15, 30))
    }

    override suspend fun getPlans(from: String, to: String): List<PlanDto> = plans.toList()

    override suspend fun pushPlan(plan: PlanDto) {
        delay(150)
        plans.removeAll { it.userId == plan.userId && it.date == plan.date }
        plans.add(plan)
    }

    override suspend fun getHolidays(year: Int): List<HolidayDto> = listOf(
        HolidayDto("$year-01-01", "Año Nuevo"),
        HolidayDto("$year-05-01", "Día de los Trabajadores"),
        HolidayDto("$year-08-25", "Declaratoria de la Independencia"),
        HolidayDto("$year-11-02", "Día de los Difuntos"),
        HolidayDto("$year-12-25", "Navidad"),
    )
}
