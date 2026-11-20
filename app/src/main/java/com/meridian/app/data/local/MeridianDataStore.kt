package com.meridian.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.meridianDataStore: DataStore<Preferences> by preferencesDataStore(name = "meridian")

object Keys {
    // Sesión (el token NO va acá: se guarda cifrado con Android Keystore, RNF09)
    val USER_ID = stringPreferencesKey("user_id")
    val NAME = stringPreferencesKey("name")
    val EMAIL = stringPreferencesKey("email")
    val ROLE = stringPreferencesKey("role")
    val TEAM_ID = stringPreferencesKey("team_id")
    val COUNTRY = stringPreferencesKey("country")
    val GOOGLE_STATUS = stringPreferencesKey("google_status")

    // Preferencias
    val START_MIN = intPreferencesKey("work_start_min")
    val END_MIN = intPreferencesKey("work_end_min")
    val ZONE = stringPreferencesKey("zone_id")
    val SUGGEST_CLOCK_IN = booleanPreferencesKey("suggest_clock_in")
    val EXIT_REMINDER = booleanPreferencesKey("exit_reminder")
    val PENDING_ALERT = booleanPreferencesKey("pending_alert")

    // Estado interno
    val LAST_SYNC = longPreferencesKey("last_sync_ms")
    val LAST_SUGGESTION = stringPreferencesKey("last_suggestion_date")
}
