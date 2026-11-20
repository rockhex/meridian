package com.meridian.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.meridian.app.domain.model.AvailabilityStatus
import com.meridian.app.domain.model.AvailabilityStatus.*
import com.meridian.app.presentation.util.agoLabel
import com.meridian.app.presentation.util.label
import java.time.Instant

data class StatusStyle(val container: Color, val content: Color, val icon: ImageVector)

fun statusStyle(status: AvailabilityStatus) = when (status) {
    DISPONIBLE -> StatusStyle(Color(0xFFDFF3E6), Color(0xFF1B5E3A), Icons.Filled.CheckCircle)
    EN_LLAMADA -> StatusStyle(Color(0xFFDCEBFA), Color(0xFF184E86), Icons.Filled.Call)
    EN_DESCANSO -> StatusStyle(Color(0xFFFFF0CC), Color(0xFF6B4500), Icons.Filled.Pause)
    FUERA_DE_HORARIO -> StatusStyle(Color(0xFFE8ECEE), Color(0xFF47535A), Icons.Filled.Bedtime)
    SIN_FICHAR -> StatusStyle(Color(0xFFEFEFEF), Color(0xFF3F3F3F), Icons.Filled.RadioButtonUnchecked)
}

/** RNF08: el estado se comunica con ícono + texto, nunca solo con color. */
@Composable
fun StatusChip(
    status: AvailabilityStatus,
    modifier: Modifier = Modifier,
    text: String = status.label(),
) {
    val style = statusStyle(status)
    Row(
        modifier = modifier.clip(RoundedCornerShape(50)).background(style.container)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(style.icon, contentDescription = null, tint = style.content, modifier = Modifier.size(16.dp))
        Text(text, color = style.content, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
    }
}

enum class BannerKind { WARNING, ERROR, INFO }

@Composable
fun MeridianBanner(
    text: String,
    kind: BannerKind,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val (bg, fg, icon) = when (kind) {
        BannerKind.WARNING -> Triple(Color(0xFFFFF4D6), Color(0xFF5B4300), Icons.Filled.WifiOff)
        BannerKind.ERROR -> Triple(MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer, Icons.Filled.Error)
        BannerKind.INFO -> Triple(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer, Icons.Filled.Info)
    }
    Row(
        modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(bg).padding(start = 12.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(18.dp))
        Text(text, color = fg, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction, modifier = Modifier.heightIn(min = 48.dp)) { Text(actionLabel, color = fg, fontWeight = FontWeight.SemiBold) }
        }
    }
}

/** Banner persistente "Sin conexión · Actualizado hace X min". */
@Composable
fun OfflineBanner(lastSync: Instant?, now: Instant, modifier: Modifier = Modifier, onRetry: (() -> Unit)? = null) {
    val suffix = if (lastSync != null) "Actualizado ${agoLabel(lastSync, now)}" else "Sin sincronizaciones todavía"
    MeridianBanner("Sin conexión · $suffix", BannerKind.WARNING, modifier, onRetry?.let { "Reintentar" }, onRetry)
}

@Composable
fun Avatar(initials: String, modifier: Modifier = Modifier, size: Int = 40) {
    Box(
        modifier = modifier.size(size.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Text(initials, color = MaterialTheme.colorScheme.onPrimaryContainer, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(text, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = modifier)
}

fun initialsOf(name: String) =
    name.split(' ').take(2).mapNotNull { it.firstOrNull() }.joinToString("").uppercase()
