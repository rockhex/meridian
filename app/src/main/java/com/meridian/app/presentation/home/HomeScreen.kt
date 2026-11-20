package com.meridian.app.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.meridian.app.domain.model.*
import com.meridian.app.presentation.components.*
import com.meridian.app.presentation.util.*
import kotlinx.coroutines.launch

/**
 * Estados: sin fichar, disponible, en llamada, en descanso, fuera de horario,
 * fichaje pendiente de sincronizar (ícono + banner offline).
 */
@Composable
fun HomeScreen(viewModel: HomeViewModel, snackbarHostState: SnackbarHostState, onGoToProfile: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            launch {
                when (event) {
                    is HomeEvent.ClockRegistered -> {
                        val zone = viewModel.uiState.value.zone
                        val time = event.event.timestamp.formatTime(zone)
                        val suffix = if (event.wasOnline) "" else " · Pendiente de sincronizar"
                        val result = snackbarHostState.showSnackbar(
                            message = "${event.event.type.doneMessage()} $time$suffix",
                            actionLabel = "Deshacer",
                            duration = SnackbarDuration.Short,
                        )
                        if (result == SnackbarResult.ActionPerformed) viewModel.onUndo(event.event.id)
                    }
                    HomeEvent.InvalidSequence -> snackbarHostState.showSnackbar("Ese fichaje no corresponde en este momento.")
                }
            }
        }
    }

    if (state.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (!state.isOnline) {
            val pendingText = if (state.pendingCount > 0) " · ${state.pendingCount} sin sincronizar" else ""
            OfflineBanner(state.lastSync, state.now)
            if (pendingText.isNotEmpty()) Text(pendingText.removePrefix(" · "), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (state.googleStatus != GoogleAccountStatus.CONNECTED) {
            // RNF07: motivo + acción para reconectar, sin bloquear el fichaje.
            MeridianBanner("Tu conexión con Google venció. Podés fichar igual.", BannerKind.ERROR, actionLabel = "Reconectar", onAction = onGoToProfile)
        }

        Column {
            Text("Hola, ${state.firstName}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                state.now.atZone(state.zone).toLocalDate().formatLong(),
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        StatusCard(state)
        ClockActions(state, viewModel)
        StatsRow(state)
        EventsCard(state)
        UpcomingCard(state)
        state.nextHoliday?.let { HolidayCard(it) }
        Spacer(Modifier.height(8.dp))
    }

    if (state.showExitConfirm) ExitConfirmSheet(state, viewModel::onConfirmClockOut, viewModel::onDismissConfirm)
}

@Composable
private fun StatusCard(state: HomeUiState) {
    val style = statusStyle(state.status)
    val chipText = if (state.status == AvailabilityStatus.EN_LLAMADA && state.busyUntil != null) {
        "En llamada hasta ${state.busyUntil.formatTime(state.zone)}"
    } else state.status.label()
    val subtitle = when (state.status) {
        AvailabilityStatus.SIN_FICHAR -> "Todavía no registraste tu entrada."
        AvailabilityStatus.DISPONIBLE -> "Jornada en curso · ${state.modality.label()}"
        AvailabilityStatus.EN_LLAMADA -> "Tus compañeros te ven ocupado/a."
        AvailabilityStatus.EN_DESCANSO -> "Descanso en curso."
        AvailabilityStatus.FUERA_DE_HORARIO ->
            if (state.primaryAction == null) "Jornada cerrada. No se muestra actividad."
            else "Fuera de tu horario laboral. No se muestra actividad."
    }
    Card(colors = CardDefaults.cardColors(containerColor = style.container), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(style.icon, contentDescription = null, tint = style.content)
                Text(chipText, color = style.content, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Text(subtitle, color = style.content, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClockActions(state: HomeUiState, vm: HomeViewModel) {
    val primary = state.primaryAction ?: return // jornada cerrada: sin acciones
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (primary == ClockType.ENTRADA && !state.modalityLocked) {
            Text("Modalidad", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                Modality.entries.forEachIndexed { i, m ->
                    SegmentedButton(
                        selected = state.modality == m,
                        onClick = { vm.onModalitySelected(m) },
                        shape = SegmentedButtonDefaults.itemShape(i, Modality.entries.size),
                        modifier = Modifier.heightIn(min = 48.dp),
                    ) { Text(m.label()) }
                }
            }
        }
        if (primary == ClockType.ENTRADA) {
            // RNF02: dos toques desde la pantalla principal (modalidad + botón; el default ya sirve).
            Button(onClick = vm::onPrimaryClick, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) { Text(primary.actionLabel()) }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = vm::onPrimaryClick, modifier = Modifier.weight(1f).heightIn(min = 56.dp)) { Text(primary.actionLabel()) }
                OutlinedButton(onClick = vm::onClockOutClick, enabled = state.canClockOut, modifier = Modifier.weight(1f).heightIn(min = 56.dp)) {
                    Text(ClockType.SALIDA.actionLabel())
                }
            }
        }
    }
}

@Composable
private fun StatsRow(state: HomeUiState) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        StatTile("Hoy trabajado", state.worked.formatHm(), Modifier.weight(1f))
        StatTile("Salida estimada", state.schedule?.end?.toString() ?: "--:--", Modifier.weight(1f))
    }
}

@Composable
private fun StatTile(label: String, value: String, modifier: Modifier) {
    Card(modifier, shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun EventsCard(state: HomeUiState) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            SectionTitle("Hoy")
            if (state.events.isEmpty()) {
                Text("Todavía no hay fichajes.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            state.events.forEach { e ->
                Row(Modifier.fillMaxWidth().heightIn(min = 32.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(e.type.recordLabel(), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    Text("${e.timestamp.formatTime(state.zone)} · ${e.modality.label()}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(8.dp))
                    SyncIcon(e.syncState)
                }
            }
        }
    }
}

/** El estado de sincronización se ve con ícono y se lee con descripción (RNF08). */
@Composable
private fun SyncIcon(state: SyncState) {
    val (icon, desc) = when (state) {
        SyncState.SYNCED -> Icons.Outlined.CloudDone to "Sincronizado"
        SyncState.PENDING -> Icons.Outlined.CloudUpload to "Pendiente de sincronizar"
        SyncState.FLAGGED -> Icons.Outlined.Warning to "Para revisar: secuencia de fichajes inválida"
    }
    Icon(icon, contentDescription = desc, modifier = Modifier.size(18.dp).semantics { contentDescription = desc })
}

@Composable
private fun UpcomingCard(state: HomeUiState) {
    if (state.upcomingSlots.isEmpty()) return
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            SectionTitle("Próximos en tu calendario")
            state.upcomingSlots.forEach { s ->
                // RNF03: solo el horario; nunca el título ni el contenido del evento.
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Outlined.Event, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("${s.start.formatTime(state.zone)} – ${s.end.formatTime(state.zone)} · Ocupado/a", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun HolidayCard(holiday: Holiday) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Text(
            "Próximo feriado: ${holiday.date.formatDayLong()} · ${holiday.name}",
            Modifier.padding(14.dp), style = MaterialTheme.typography.bodySmall,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExitConfirmSheet(state: HomeUiState, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("¿Fichar salida?", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                "Vas a registrar tu salida a las ${state.now.formatTime(state.zone)} y cerrar la jornada. " +
                    "Si solo querés pausar, usá \"Iniciar descanso\".",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text("Horas trabajadas hoy: ${state.worked.formatHm()}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Button(onClick = onConfirm, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text("Fichar salida") }
            OutlinedButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text("Cancelar") }
        }
    }
}
