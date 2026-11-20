package com.meridian.app.device

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.meridian.app.R
import com.meridian.app.domain.model.ClockType
import com.meridian.app.domain.model.Modality
import com.meridian.app.presentation.MainActivity

class NotificationHelper(private val context: Context) {
    private val manager = NotificationManagerCompat.from(context)

    fun createChannels() {
        manager.createNotificationChannel(
            NotificationChannelCompat.Builder(CHANNEL_CLOCK, NotificationManagerCompat.IMPORTANCE_HIGH)
                .setName("Fichaje").setDescription("Sugerencias y recordatorios de fichaje").build(),
        )
        manager.createNotificationChannel(
            NotificationChannelCompat.Builder(CHANNEL_SYNC, NotificationManagerCompat.IMPORTANCE_DEFAULT)
                .setName("Sincronización").setDescription("Avisos de fichajes pendientes de sincronizar").build(),
        )
    }

    private fun openApp(): PendingIntent = PendingIntent.getActivity(
        context, 0, Intent(context, MainActivity::class.java),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    private fun clockAction(type: ClockType, modality: Modality): PendingIntent = PendingIntent.getBroadcast(
        context, type.ordinal,
        Intent(context, ClockActionReceiver::class.java).setAction(ClockActionReceiver.ACTION_CLOCK)
            .putExtra(ClockActionReceiver.EXTRA_TYPE, type.name)
            .putExtra(ClockActionReceiver.EXTRA_MODALITY, modality.name),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    /** RF03: fichar en un toque desde la notificación, sin abrir la app (RNF02). */
    fun showClockInSuggestion() = notify(
        ID_CLOCK, CHANNEL_CLOCK, "¿Fichar entrada presencial?", "Llegaste a la oficina.",
        action = "Fichar entrada" to clockAction(ClockType.ENTRADA, Modality.PRESENCIAL),
    )

    fun showExitReminder() = notify(
        ID_CLOCK, CHANNEL_CLOCK, "¿Ya terminaste la jornada?", "Saliste de la oficina. Fichar salida cierra tu jornada.",
        action = "Fichar salida" to clockAction(ClockType.SALIDA, Modality.PRESENCIAL),
    )

    fun showConfirmation(type: ClockType, time: String, pendingSync: Boolean) = notify(
        ID_CLOCK, CHANNEL_CLOCK, "${type.confirmLabel()} $time",
        if (pendingSync) "Pendiente de sincronizar: se enviará cuando haya conexión." else "Registrado.",
        timeoutMs = 5_000,
    )

    fun showPendingSyncAlert() = notify(
        ID_SYNC, CHANNEL_SYNC, "Fichajes sin sincronizar",
        "Tus fichajes están guardados en el teléfono y se enviarán al volver la conexión.",
    )

    fun cancelClock() = manager.cancel(ID_CLOCK)

    @SuppressLint("MissingPermission")
    private fun notify(
        id: Int, channel: String, title: String, text: String,
        action: Pair<String, PendingIntent>? = null, timeoutMs: Long? = null,
    ) {
        if (!manager.areNotificationsEnabled()) return
        val builder = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_stat_meridian)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(openApp())
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // visible con la pantalla bloqueada
        action?.let { (label, intent) -> builder.addAction(0, label, intent) }
        timeoutMs?.let { builder.setTimeoutAfter(it) }
        manager.notify(id, builder.build())
    }

    private fun ClockType.confirmLabel() = when (this) {
        ClockType.ENTRADA -> "Entrada registrada"
        ClockType.INICIO_DESCANSO -> "Descanso iniciado"
        ClockType.FIN_DESCANSO -> "Descanso finalizado"
        ClockType.SALIDA -> "Salida registrada"
    }

    companion object {
        const val CHANNEL_CLOCK = "clock"
        const val CHANNEL_SYNC = "sync"
        const val ID_CLOCK = 1001
        const val ID_SYNC = 1002
    }
}
