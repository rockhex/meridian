package com.meridian.app.presentation.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOff
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.meridian.app.R
import com.meridian.app.presentation.components.BannerKind
import com.meridian.app.presentation.components.MeridianBanner

/** Estados: normal, cargando, cuenta fuera del dominio, sin conexión. */
@Composable
fun LoginScreen(viewModel: LoginViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val action = { viewModel.onContinueWithGoogle(context) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier.size(72.dp).clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painterResource(R.drawable.ic_stat_meridian), contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(40.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text("Meridian", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(
            "Coordinación de equipos, sin monitoreo.",
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))

        when (state.error) {
            LoginError.WRONG_DOMAIN -> {
                MeridianBanner("Esa cuenta no es de la empresa. Ingresá con tu cuenta corporativa de Google.", BannerKind.ERROR)
                Spacer(Modifier.height(12.dp))
                Button(onClick = action, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text("Usar otra cuenta de Google") }
            }
            LoginError.OFFLINE -> {
                MeridianBanner("Sin conexión. Necesitás internet para iniciar sesión.", BannerKind.WARNING)
                Spacer(Modifier.height(12.dp))
                Button(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text("Continuar con Google") }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = action, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text("Reintentar") }
            }
            LoginError.GENERIC -> {
                MeridianBanner("No pudimos iniciar sesión. Probá de nuevo.", BannerKind.ERROR)
                Spacer(Modifier.height(12.dp))
                Button(onClick = action, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text("Continuar con Google") }
            }
            null -> Button(onClick = action, enabled = !state.isLoading, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                if (state.isLoading) {
                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                    Spacer(Modifier.width(8.dp))
                    Text("Conectando con Google…")
                } else Text("Continuar con Google")
            }
        }

        Spacer(Modifier.height(8.dp))
        Text("Solo cuentas del dominio de tu empresa", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(32.dp))

        InfoRow(Icons.Outlined.VerifiedUser, "Verificamos tu cuenta corporativa. Nunca guardamos tu contraseña de Google.")
        Spacer(Modifier.height(12.dp))
        InfoRow(Icons.Outlined.LocationOff, "No registramos tu ubicación continua ni tu actividad en la computadora; solo tu estado.")
    }
}

@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
