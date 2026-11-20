package com.meridian.app.device

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.meridian.app.domain.model.Office
import com.meridian.app.domain.repository.OfficeGeofencing

/**
 * RNF03/RNF05: solo se registran eventos de ENTRADA y SALIDA del radio de la oficina mediante el
 * geofencing del sistema operativo. No hay seguimiento GPS en segundo plano ni ubicación continua.
 */
class GeofenceManager(private val context: Context) : OfficeGeofencing {
    private val client = LocationServices.getGeofencingClient(context)

    private fun pendingIntent(): PendingIntent {
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
            (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0)
        return PendingIntent.getBroadcast(context, 0, Intent(context, GeofenceReceiver::class.java), flags)
    }

    private fun hasPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        val background = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        return fine && background
    }

    @SuppressLint("MissingPermission")
    override fun register(office: Office) {
        // RF03: sin permiso, la app sigue con fichaje manual (la rama de la notificación no existe).
        if (!hasPermission()) return
        val geofence = Geofence.Builder()
            .setRequestId(GEOFENCE_ID)
            .setCircularRegion(office.latitude, office.longitude, office.radiusMeters)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .build()
        val request = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()
        client.addGeofences(request, pendingIntent())
    }

    override fun unregister() {
        client.removeGeofences(pendingIntent())
    }

    private companion object {
        const val GEOFENCE_ID = "meridian_office"
    }
}
