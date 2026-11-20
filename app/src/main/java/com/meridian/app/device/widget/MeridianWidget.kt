package com.meridian.app.device.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Button
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.GlanceTheme
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.meridian.app.di.container
import com.meridian.app.domain.model.ClockType
import com.meridian.app.domain.model.Modality
import com.meridian.app.domain.usecase.ClockRules
import java.time.ZoneId

data class WidgetState(val statusText: String, val action: ClockType?, val actionLabel: String)

/** Widget de fichaje (RNF02: un toque). Solo expone la acción principal; "Fichar salida" pide confirmación en la app. */
class MeridianWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val state = loadState(context)
        provideContent { GlanceTheme { WidgetContent(state) } }
    }

    private suspend fun loadState(context: Context): WidgetState {
        val c = context.container
        val session = c.authRepository.currentSession() ?: return WidgetState("Iniciá sesión en Meridian", null, "")
        val zone = ZoneId.of(c.preferencesRepository.current().schedule.zoneId)
        val today = c.clock.instant().atZone(zone).toLocalDate()
        val last = c.clockRepository.eventsForDay(session.userId, today, zone).lastOrNull()?.type
        val action = ClockRules.primaryAction(last)
        return WidgetState(
            statusText = when (last) {
                null -> "Sin fichar"
                ClockType.INICIO_DESCANSO -> "En descanso"
                ClockType.SALIDA -> "Jornada cerrada"
                else -> "Jornada en curso"
            },
            action = action,
            actionLabel = when (action) {
                ClockType.ENTRADA -> "Fichar entrada"
                ClockType.INICIO_DESCANSO -> "Iniciar descanso"
                ClockType.FIN_DESCANSO -> "Finalizar descanso"
                else -> ""
            },
        )
    }
}

@Composable
private fun WidgetContent(state: WidgetState) {
    Column(
        modifier = GlanceModifier.fillMaxSize().background(GlanceTheme.colors.widgetBackground).padding(12.dp),
        verticalAlignment = Alignment.Vertical.CenterVertically,
    ) {
        Text("Meridian", style = TextStyle(fontWeight = FontWeight.Bold, color = GlanceTheme.colors.primary))
        Text(state.statusText, style = TextStyle(color = GlanceTheme.colors.onSurface))
        if (state.action != null) {
            Spacer(GlanceModifier.height(8.dp))
            Button(
                text = state.actionLabel,
                onClick = actionRunCallback<WidgetClockAction>(
                    actionParametersOf(WidgetClockAction.TYPE_KEY to state.action.name),
                ),
                modifier = GlanceModifier.fillMaxWidth(),
            )
        }
    }
}

class WidgetClockAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val type = parameters[TYPE_KEY]?.let { runCatching { ClockType.valueOf(it) }.getOrNull() } ?: return
        context.container.registerClockEvent(type, Modality.REMOTO)
        MeridianWidget().updateAll(context)
    }

    companion object {
        val TYPE_KEY = ActionParameters.Key<String>("clock_type")
    }
}

class MeridianWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MeridianWidget()
}
