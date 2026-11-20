package com.meridian.app.device

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.meridian.app.di.container
import kotlinx.coroutines.launch

class GeofenceReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val event = GeofencingEvent.fromIntent(intent) ?: return
        if (event.hasError()) return
        val container = context.container
        val pending = goAsync()
        container.appScope.launch {
            try {
                when (event.geofenceTransition) {
                    Geofence.GEOFENCE_TRANSITION_ENTER -> container.officeEventHandler.onEnter()
                    Geofence.GEOFENCE_TRANSITION_EXIT -> container.officeEventHandler.onExit()
                }
            } finally {
                pending.finish()
            }
        }
    }
}
