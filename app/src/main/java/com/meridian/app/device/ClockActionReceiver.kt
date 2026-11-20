package com.meridian.app.device

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.meridian.app.device.widget.MeridianWidget
import com.meridian.app.di.container
import com.meridian.app.domain.model.ClockType
import com.meridian.app.domain.model.Modality
import com.meridian.app.domain.usecase.RegisterResult
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** Fichaje desde la notificación: un toque, sin abrir la app (RNF02). Funciona offline (RNF01). */
class ClockActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_CLOCK) return
        val type = intent.getStringExtra(EXTRA_TYPE)?.let { runCatching { ClockType.valueOf(it) }.getOrNull() } ?: return
        val modality = intent.getStringExtra(EXTRA_MODALITY)?.let { runCatching { Modality.valueOf(it) }.getOrNull() }
            ?: Modality.REMOTO
        val container = context.container
        val pending = goAsync()
        container.appScope.launch {
            try {
                val result = container.registerClockEvent(type, modality)
                if (result is RegisterResult.Registered) {
                    val zone = ZoneId.of(container.preferencesRepository.current().schedule.zoneId)
                    val time = DateTimeFormatter.ofPattern("HH:mm").format(result.event.timestamp.atZone(zone))
                    container.notifications.showConfirmation(type, time, pendingSync = true)
                } else {
                    container.notifications.cancelClock()
                }
                MeridianWidget().updateAll(context)
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val ACTION_CLOCK = "com.meridian.app.ACTION_CLOCK"
        const val EXTRA_TYPE = "type"
        const val EXTRA_MODALITY = "modality"
    }
}
