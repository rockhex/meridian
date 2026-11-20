package com.meridian.app.device

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.meridian.app.di.container
import kotlinx.coroutines.launch

/** Los geofences se pierden al reiniciar: se vuelven a registrar y se retoma la cola pendiente (RNF06). */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val c = context.container
        val pending = goAsync()
        c.appScope.launch {
            try {
                c.authRepository.currentSession()?.let { s ->
                    c.teamRepository.getTeam(s.teamId)?.office?.let { c.geofencing.register(it) }
                }
                c.syncScheduler.requestSync()
            } finally {
                pending.finish()
            }
        }
    }
}
