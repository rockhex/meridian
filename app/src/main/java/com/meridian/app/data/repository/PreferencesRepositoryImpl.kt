package com.meridian.app.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.meridian.app.data.local.Keys
import com.meridian.app.domain.model.UserPreferences
import com.meridian.app.domain.model.WorkSchedule
import com.meridian.app.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

class PreferencesRepositoryImpl(private val dataStore: DataStore<Preferences>) : PreferencesRepository {

    override val preferences: Flow<UserPreferences> = dataStore.data.map { p ->
        UserPreferences(
            schedule = WorkSchedule(
                start = LocalTime.of(0, 0).plusMinutes((p[Keys.START_MIN] ?: DEFAULT_START).toLong()),
                end = LocalTime.of(0, 0).plusMinutes((p[Keys.END_MIN] ?: DEFAULT_END).toLong()),
                zoneId = p[Keys.ZONE] ?: ZoneId.systemDefault().id,
            ),
            suggestClockIn = p[Keys.SUGGEST_CLOCK_IN] ?: true,
            exitReminder = p[Keys.EXIT_REMINDER] ?: true,
            pendingSyncAlert = p[Keys.PENDING_ALERT] ?: true,
        )
    }.distinctUntilChanged()

    override val lastSync: Flow<Instant?> =
        dataStore.data.map { p -> p[Keys.LAST_SYNC]?.let(Instant::ofEpochMilli) }.distinctUntilChanged()

    override suspend fun current() = preferences.first()

    override suspend fun setSchedule(start: LocalTime, end: LocalTime, zoneId: String) {
        dataStore.edit {
            it[Keys.START_MIN] = start.hour * 60 + start.minute
            it[Keys.END_MIN] = end.hour * 60 + end.minute
            it[Keys.ZONE] = zoneId
        }
    }

    override suspend fun setSuggestClockIn(enabled: Boolean) { dataStore.edit { it[Keys.SUGGEST_CLOCK_IN] = enabled } }
    override suspend fun setExitReminder(enabled: Boolean) { dataStore.edit { it[Keys.EXIT_REMINDER] = enabled } }
    override suspend fun setPendingSyncAlert(enabled: Boolean) { dataStore.edit { it[Keys.PENDING_ALERT] = enabled } }
    override suspend fun setLastSync(instant: Instant) { dataStore.edit { it[Keys.LAST_SYNC] = instant.toEpochMilli() } }
    override suspend fun lastSuggestionDate(): String? = dataStore.data.first()[Keys.LAST_SUGGESTION]
    override suspend fun setLastSuggestionDate(date: String) { dataStore.edit { it[Keys.LAST_SUGGESTION] = date } }

    private companion object {
        const val DEFAULT_START = 9 * 60
        const val DEFAULT_END = 18 * 60
    }
}
