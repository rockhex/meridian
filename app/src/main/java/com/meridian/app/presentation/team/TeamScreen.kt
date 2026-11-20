package com.meridian.app.presentation.team

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.meridian.app.domain.model.TeamSummary
import com.meridian.app.presentation.components.*
import com.meridian.app.presentation.util.agoLabel
import com.meridian.app.presentation.util.label

/** Estados: cargando, contenido, equipo vacío, desactualizado, sin conexión sin datos previos. */
@Composable
fun TeamScreen(viewModel: TeamViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    when (state.phase) {
        TeamPhase.LOADING -> TeamSkeleton()
        TeamPhase.OFFLINE_NO_DATA -> CenteredMessage(
            icon = { Icon(Icons.Outlined.CloudOff, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant) },
            title = "Todavía no hay datos del equipo",
            text = "La primera sincronización necesita conexión. Se guardará en el teléfono para usarla sin red.",
            action = "Reintentar", onAction = viewModel::retry,
        )
        TeamPhase.EMPTY -> CenteredMessage(
            icon = { Icon(Icons.Outlined.Groups, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant) },
            title = "Todavía no hay nadie más en tu equipo",
            text = "Cuando se sumen compañeros vas a ver acá si están disponibles.",
        )
        TeamPhase.CONTENT -> TeamContent(state, viewModel)
    }
}

@Composable
private fun TeamContent(state: TeamUiState, vm: TeamViewModel) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!state.isOnline) OfflineBanner(state.lastSync, state.now, onRetry = vm::retry)
                else if (state.anyStale) MeridianBanner("Algunos estados están desactualizados.", BannerKind.WARNING, actionLabel = "Actualizar", onAction = vm::retry)
                Text(state.teamName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                state.lastSync?.let {
                    Text("Actualizado ${agoLabel(it, state.now)} · ${state.totalMembers} integrantes", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                state.summary?.let { SummaryRow(it) }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TeamFilter.entries.forEach { f ->
                        FilterChip(
                            selected = state.filter == f, onClick = { vm.onFilterSelected(f) },
                            label = { Text(when (f) { TeamFilter.TODOS -> "Todos"; TeamFilter.DISPONIBLES -> "Disponibles"; TeamFilter.EN_OFICINA -> "En la oficina hoy" }) },
                            modifier = Modifier.heightIn(min = 48.dp),
                        )
                    }
                }
            }
        }
        items(state.members, key = { it.userId }) { MemberRow(it) }
        if (state.members.isEmpty()) item {
            Text("Nadie coincide con este filtro.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SummaryRow(s: TeamSummary) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("Total" to s.total, "Disponibles" to s.available, "En llamada" to s.inCall, "Sin fichar" to s.notClocked).forEach { (label, n) ->
            Card(Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(vertical = 8.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$n", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun MemberRow(m: MemberUi) {
    val statusLabel = m.statusText.ifEmpty { m.status.label() }
    // Los estados con más de 15 min se atenúan y se rotulan (RF02).
    Card(
        Modifier.fillMaxWidth().alpha(if (m.isStale) 0.6f else 1f)
            .semantics(mergeDescendants = true) { contentDescription = "${m.name}, $statusLabel, hora local ${m.localTime}" },
        shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Avatar(m.initials)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(if (m.isSelf) "${m.name} (vos)" else m.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                Text("${m.place} · ${m.localTime}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (m.isStale) Text("Desactualizado", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (m.statusText.isNotEmpty()) StatusChip(m.status, text = m.statusText) else StatusChip(m.status)
        }
    }
}

@Composable
private fun TeamSkeleton() {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(Modifier.width(160.dp).height(28.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceVariant))
        repeat(6) {
            Box(Modifier.fillMaxWidth().height(64.dp).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.surfaceVariant))
        }
    }
}

@Composable
fun CenteredMessage(icon: @Composable () -> Unit, title: String, text: String, action: String? = null, onAction: (() -> Unit)? = null) {
    Column(Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        icon()
        Spacer(Modifier.height(12.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        if (action != null && onAction != null) {
            Spacer(Modifier.height(16.dp))
            OutlinedButton(onClick = onAction, modifier = Modifier.heightIn(min = 48.dp)) { Text(action) }
        }
    }
}
