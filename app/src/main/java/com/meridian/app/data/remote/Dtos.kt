package com.meridian.app.data.remote

import java.io.IOException

data class SignInRequest(val idToken: String)
data class LinkGoogleRequest(val idToken: String)

data class SessionDto(
    val token: String,
    val userId: String,
    val name: String,
    val email: String,
    val role: String,
    val teamId: String,
    val country: String,
    val zoneId: String,
)

data class OfficeDto(val name: String, val latitude: Double, val longitude: Double, val radiusMeters: Float)

data class MemberDto(
    val userId: String,
    val name: String,
    val country: String,
    val zoneId: String,
    val status: String,
    val busyUntil: String?,
    val onsiteToday: Boolean,
    val updatedAt: String,
)

data class TeamDto(
    val id: String,
    val name: String,
    val office: OfficeDto?,
    val fixedDays: List<String>,
    val members: List<MemberDto>,
)

data class ClockEventDto(val id: String, val type: String, val timestamp: String, val modality: String)

/** status: ACCEPTED | DUPLICATE | FLAGGED (secuencia inválida, guardado para revisión). */
data class ClockPushResponse(val status: String)

data class PlanDto(val userId: String, val date: String, val going: Boolean)
data class CalendarSlotDto(val id: String, val start: String, val end: String)
data class HolidayDto(val date: String, val name: String)

class UnauthorizedException : IOException("Sesión vencida")
class WrongDomainException : IOException("Cuenta fuera del dominio")
class GoogleTokenExpiredException : IOException("Token de Google vencido o revocado")
