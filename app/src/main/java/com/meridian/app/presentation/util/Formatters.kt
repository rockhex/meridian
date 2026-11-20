package com.meridian.app.presentation.util

import com.meridian.app.domain.model.AvailabilityStatus
import com.meridian.app.domain.model.ClockType
import com.meridian.app.domain.model.Modality
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val ES: Locale = Locale.forLanguageTag("es-AR")
private val HHMM: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

fun Instant.formatTime(zone: ZoneId): String = HHMM.format(atZone(zone))

fun Duration.formatHm(): String {
    val h = toHours()
    val m = toMinutes() % 60
    return if (h > 0) "${h}h ${m}m" else "${m}m"
}

fun LocalDate.formatLong(): String =
    DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", ES).format(this).replaceFirstChar { it.uppercase() }

fun LocalDate.formatDayShort(): String =
    DateTimeFormatter.ofPattern("EEE d", ES).format(this).replace(".", "").replaceFirstChar { it.uppercase() }

fun LocalDate.formatDayLong(): String =
    DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", ES).format(this)

fun agoLabel(from: Instant, now: Instant): String {
    val m = Duration.between(from, now).toMinutes().coerceAtLeast(0)
    return when {
        m < 1 -> "hace un momento"
        m < 60 -> "hace $m min"
        else -> "hace ${m / 60} h"
    }
}

fun cityOf(zoneId: String): String = zoneId.substringAfterLast('/').replace('_', ' ')

fun AvailabilityStatus.label(): String = when (this) {
    AvailabilityStatus.SIN_FICHAR -> "Sin fichar"
    AvailabilityStatus.DISPONIBLE -> "Disponible"
    AvailabilityStatus.EN_LLAMADA -> "En llamada"
    AvailabilityStatus.EN_DESCANSO -> "En descanso"
    AvailabilityStatus.FUERA_DE_HORARIO -> "Fuera de horario"
}

fun Modality.label(): String = if (this == Modality.PRESENCIAL) "Presencial" else "Remoto"

fun ClockType.actionLabel(): String = when (this) {
    ClockType.ENTRADA -> "Fichar entrada"
    ClockType.INICIO_DESCANSO -> "Iniciar descanso"
    ClockType.FIN_DESCANSO -> "Finalizar descanso"
    ClockType.SALIDA -> "Fichar salida"
}

fun ClockType.recordLabel(): String = when (this) {
    ClockType.ENTRADA -> "Entrada"
    ClockType.INICIO_DESCANSO -> "Inicio de descanso"
    ClockType.FIN_DESCANSO -> "Fin de descanso"
    ClockType.SALIDA -> "Salida"
}

fun ClockType.doneMessage(): String = when (this) {
    ClockType.ENTRADA -> "Entrada registrada"
    ClockType.INICIO_DESCANSO -> "Descanso iniciado"
    ClockType.FIN_DESCANSO -> "Descanso finalizado"
    ClockType.SALIDA -> "Salida registrada"
}
