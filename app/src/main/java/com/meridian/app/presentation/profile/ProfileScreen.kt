package com.meridian.app.presentation.profile

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.meridian.app.domain.model.GoogleAccountStatus
import com.meridian.app.domain.model.Role
import com.meridian.app.domain.model.WorkSchedule
import com.meridian.app.presentation.components.*
import java.time.LocalTime
import java.time.ZoneId

private fun Context.hasLocationPermission(): Boolean {
    val fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val bg = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
    return fine && bg
}

/** Estados: permiso de ubicación rechazado, cuenta desconectada, token vencido. */
@Composable
fun ProfileScreen(viewModel: ProfileViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var locationGranted by remember { mutableStateOf(context.hasLocationPermission()) }
    var askedBefore by remember { mutableStateOf(false) }
    var showScheduleDialog by remember { mutableStateOf(false) }

    // Se vuelve a leer el permiso al volver desde Ajustes.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, e ->
            if (e == Lifecycle.Event.ON_RESUME) {
                val now = context.hasLocationPermission()
                if (now != locationGranted) { locationGranted = now; if (now) viewModel.onLocationPermissionChanged() }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Android 11+: la ubicación en segundo plano se pide en un segundo paso, separado.
    val backgroundLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        locationGranted = context.hasLocationPermission()
        if (locationGranted) viewModel.onLocationPermissionChanged()
    }
    val foregroundLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            backgroundLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        } else {
            locationGranted = context.hasLocationPermission()
            if (locationGranted) viewModel.onLocationPermissionChanged()
        }
    }
    fun requestLocation() {
        if (askedBefore) {
            // Ya se rechazó: el sistema no vuelve a preguntar, se lleva a Ajustes.
            context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null)))
        } else {
            askedBefore = true
            foregroundLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    val session = state.session ?: return

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Avatar(session.initials, size = 56)
            Column {
                Text(session.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    "${if (session.role == Role.LIDER) "Líder de equipo" else "Empleado/a"} · ${session.country}",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(session.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        if (!locationGranted) {
            MeridianBanner(
                "Permiso de ubicación rechazado. Podés fichar a mano y elegir la modalidad; no te sugeriremos fichar al llegar.",
                BannerKind.WARNING, actionLabel = "Activar", onAction = ::requestLocation,
            )
        }
        when (state.googleStatus) {
            GoogleAccountStatus.TOKEN_EXPIRED -> MeridianBanner(
                "Venció tu conexión con Google. El estado “en llamada” usa los últimos datos. Podés fichar igual.",
                BannerKind.ERROR, actionLabel = if (state.reconnecting) "Conectando…" else "Reconectar",
                onAction = { if (!state.reconnecting) viewModel.reconnectGoogle(context) },
            )
            GoogleAccountStatus.DISCONNECTED -> MeridianBanner(
                "Tu cuenta de Google está desconectada.", BannerKind.ERROR,
                actionLabel = "Conectar", onAction = { viewModel.reconnectGoogle(context) },
            )
            GoogleAccountStatus.CONNECTED -> Unit
        }

        SettingsCard("Jornada") {
            state.schedule?.let { s ->
                SettingRow("Horario laboral", "${s.start} – ${s.end}", onClick = { showScheduleDialog = true })
                SettingRow("Zona horaria", s.zoneId, onClick = { showScheduleDialog = true })
            }
            state.office?.let { SettingRow("Oficina", "${it.name} · radio ${it.radiusMeters.toInt()} m", onClick = null) }
        }

        SettingsCard("Notificaciones") {
            SwitchRow("Sugerir fichaje al llegar a la oficina", state.suggestClockIn && locationGranted, enabled = locationGranted, onChange = viewModel::setSuggestClockIn)
            SwitchRow("Recordatorio de salida", state.exitReminder, onChange = viewModel::setExitReminder)
            SwitchRow("Aviso de fichajes pendientes", state.pendingAlert, onChange = viewModel::setPendingAlert)
        }

        SettingsCard("Cuentas") {
            val googleText = when (state.googleStatus) {
                GoogleAccountStatus.CONNECTED -> "Conectada"
                GoogleAccountStatus.TOKEN_EXPIRED -> "Token vencido"
                GoogleAccountStatus.DISCONNECTED -> "Desconectada"
            }
            SettingRow("Google (calendario)", googleText, onClick = null)
            SettingRow("Slack", "Próximamente", onClick = null)
            SettingRow("Jira", "Próximamente", onClick = null)
        }

        Text(
            "Meridian no registra tu ubicación continua, ni captura pantalla o teclado. Del calendario solo usa el horario de tus eventos.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedButton(onClick = viewModel::signOut, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Cerrar sesión") }
    }

    if (showScheduleDialog && state.schedule != null) {
        ScheduleDialog(state.schedule!!, onDismiss = { showScheduleDialog = false }) { s, e, z ->
            viewModel.setSchedule(s, e, z); showScheduleDialog = false
        }
    }
}

@Composable
private fun SettingsCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            SectionTitle(title)
            content()
        }
    }
}

@Composable
private fun SettingRow(label: String, value: String, onClick: (() -> Unit)?) {
    val rowModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    Row(Modifier.fillMaxWidth().then(rowModifier).heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, enabled: Boolean = true, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onChange, enabled = enabled)
    }
}

private val zones = listOf(
    "America/Argentina/Buenos_Aires", "America/Montevideo", "America/Santiago",
    "America/Sao_Paulo", "America/Bogota", "Europe/Madrid",
)

/** Horario con pasos de 30 min (sin pickers experimentales) + zona horaria. */
@Composable
private fun ScheduleDialog(current: WorkSchedule, onDismiss: () -> Unit, onSave: (LocalTime, LocalTime, String) -> Unit) {
    var start by remember { mutableStateOf(current.start) }
    var end by remember { mutableStateOf(current.end) }
    var zone by remember { mutableStateOf(current.zoneId) }
    val zoneOptions = remember { (zones + current.zoneId).distinct() }
    val valid = start.isBefore(end)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Horario laboral") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Stepper("Entrada", start) { start = start.plusMinutes(it.toLong()) }
                Stepper("Salida", end) { end = end.plusMinutes(it.toLong()) }
                if (!valid) Text("La salida debe ser posterior a la entrada.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                Text("Zona horaria", style = MaterialTheme.typography.labelMedium)
                zoneOptions.forEach { z ->
                    Row(Modifier.fillMaxWidth().clickable { zone = z }.heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = zone == z, onClick = { zone = z })
                        Text(z, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { onSave(start, end, zone) }, enabled = valid && runCatching { ZoneId.of(zone) }.isSuccess) { Text("Guardar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}

@Composable
private fun Stepper(label: String, time: LocalTime, onStep: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f))
        OutlinedButton(onClick = { onStep(-30) }, modifier = Modifier.size(48.dp), contentPadding = PaddingValues(0.dp)) { Text("−") }
        Text(time.toString(), Modifier.padding(horizontal = 12.dp), fontWeight = FontWeight.SemiBold)
        OutlinedButton(onClick = { onStep(30) }, modifier = Modifier.size(48.dp), contentPadding = PaddingValues(0.dp)) { Text("+") }
    }
}
