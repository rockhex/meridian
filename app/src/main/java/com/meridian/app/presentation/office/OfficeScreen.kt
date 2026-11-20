package com.meridian.app.presentation.office

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.selection.toggleable
import com.meridian.app.domain.model.WeekDayPlan
import com.meridian.app.presentation.components.BannerKind
import com.meridian.app.presentation.components.MeridianBanner
import com.meridian.app.presentation.team.CenteredMessage
import com.meridian.app.presentation.util.formatDayShort
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

/** Estados: sin oficina asignada, sin planificación, contenido, cambio pendiente de sincronizar. */
@Composable
fun OfficeScreen(viewModel: OfficeViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val week = state.week

    if (state.isLoading || week == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    if (week.office == null) {
        CenteredMessage(
            icon = { Icon(Icons.Outlined.Apartment, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant) },
            title = "No tenés una oficina asignada",
            text = "Cuando tu equipo defina una oficina, vas a poder marcar tus días presenciales acá.",
        )
        return
    }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Oficina esta semana", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(
                    "${state.weekStart.dayOfMonth}–${state.weekStart.plusDays(4).dayOfMonth} de " +
                        state.weekStart.plusDays(4).month.getDisplayName(TextStyle.FULL, Locale.forLanguageTag("es-AR")) +
                        " · ${week.office.name}",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (week.hasPending) {
                    MeridianBanner(
                        if (state.isOnline) "Sincronizando tus cambios…" else "Sin conexión. Tus cambios se van a sincronizar al reconectar.",
                        BannerKind.WARNING,
                    )
                }
                if (week.fixedDays.isNotEmpty()) {
                    val names = week.fixedDays.joinToString(" y ") { it.displayName() }
                    MeridianBanner("Tu equipo definió $names como días fijos.", BannerKind.INFO)
                }
                if (!week.hasAnyPlan) {
                    MeridianBanner("Todavía nadie planificó esta semana. Marcá los días que vas a ir.", BannerKind.INFO)
                }
            }
        }
        items(week.days, key = { it.date.toString() }) { day ->
            DayRow(day, past = day.date.isBefore(state.today), isToday = day.date == state.today) { viewModel.onToggle(day) }
        }
    }
}

private fun DayOfWeek.displayName() = getDisplayName(TextStyle.FULL, Locale.forLanguageTag("es-AR"))

@Composable
private fun DayRow(day: WeekDayPlan, past: Boolean, isToday: Boolean, onToggle: () -> Unit) {
    val enabled = day.holidayName == null && !past
    val who = when {
        day.holidayName != null -> "Feriado · ${day.holidayName}"
        day.goingCount == 0 -> "Nadie planificó este día"
        else -> "${day.goingCount} ${if (day.goingCount == 1) "va" else "van"}" +
            if (day.attendees.isNotEmpty()) " · ${day.attendees.take(3).joinToString(", ") { it.substringBefore(' ') }}" +
                if (day.attendees.size > 3) " y ${day.attendees.size - 3} más" else "" else ""
    }
    val border = if (isToday) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(14.dp)) else Modifier
    Card(Modifier.fillMaxWidth().then(border), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(
            Modifier.fillMaxWidth()
                .toggleable(value = day.going, enabled = enabled, role = Role.Checkbox, onValueChange = { onToggle() })
                .semantics { contentDescription = "${day.date.formatDayShort()}. $who. ${if (day.going) "Vas a ir" else "No vas a ir"}" }
                .heightIn(min = 64.dp).padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(day.date.formatDayShort(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    if (day.isFixed && day.holidayName == null) Tag("Fijo")
                    if (isToday) Tag("Hoy")
                    if (day.pendingSync) Icon(Icons.Outlined.CloudUpload, "Pendiente de sincronizar", Modifier.size(16.dp))
                }
                Text(who, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Checkbox(checked = day.going, onCheckedChange = null, enabled = enabled)
            Text("Voy", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun Tag(text: String) {
    Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.primaryContainer) {
        Text(text, Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}
