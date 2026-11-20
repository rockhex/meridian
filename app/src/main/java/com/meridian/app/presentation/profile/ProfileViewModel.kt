package com.meridian.app.presentation.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meridian.app.data.auth.GoogleIdTokenProvider
import com.meridian.app.domain.model.*
import com.meridian.app.domain.repository.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalTime

data class ProfileUiState(
    val session: Session? = null,
    val schedule: WorkSchedule? = null,
    val suggestClockIn: Boolean = true,
    val exitReminder: Boolean = true,
    val pendingAlert: Boolean = true,
    val googleStatus: GoogleAccountStatus = GoogleAccountStatus.CONNECTED,
    val office: Office? = null,
    val reconnecting: Boolean = false,
)

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModel(
    private val auth: AuthRepository,
    private val prefs: PreferencesRepository,
    private val teamRepository: TeamRepository,
    private val tokenProvider: GoogleIdTokenProvider,
    private val syncTrigger: SyncTrigger,
) : ViewModel() {

    private val reconnecting = MutableStateFlow(false)

    private val office = auth.session.filterNotNull().flatMapLatest { teamRepository.observeTeam(it.teamId) }.map { it?.office }

    val uiState: StateFlow<ProfileUiState> = combine(
        auth.session, prefs.preferences, auth.googleStatus, office, reconnecting,
    ) { s, p, g, o, r ->
        ProfileUiState(s, p.schedule, p.suggestClockIn, p.exitReminder, p.pendingSyncAlert, g, o, r)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())

    fun setSchedule(start: LocalTime, end: LocalTime, zoneId: String) {
        viewModelScope.launch { prefs.setSchedule(start, end, zoneId); syncTrigger.requestSync() }
    }
    fun setSuggestClockIn(v: Boolean) { viewModelScope.launch { prefs.setSuggestClockIn(v) } }
    fun setExitReminder(v: Boolean) { viewModelScope.launch { prefs.setExitReminder(v) } }
    fun setPendingAlert(v: Boolean) { viewModelScope.launch { prefs.setPendingSyncAlert(v) } }

    /** El permiso cambió: una sincronización registra el geofence de la oficina. */
    fun onLocationPermissionChanged() = syncTrigger.requestSync()

    fun reconnectGoogle(context: Context) {
        viewModelScope.launch {
            reconnecting.value = true
            try {
                val token = tokenProvider.requestIdToken(context)
                if (auth.reconnectGoogle(token)) syncTrigger.requestSync()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // se mantiene el aviso "Reconectar"
            } finally {
                reconnecting.value = false
            }
        }
    }

    fun signOut() { viewModelScope.launch { auth.signOut() } }
}
