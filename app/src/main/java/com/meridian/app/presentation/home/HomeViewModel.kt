package com.meridian.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meridian.app.domain.model.*
import com.meridian.app.domain.repository.*
import com.meridian.app.domain.usecase.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.*

data class HomeUiState(
    val isLoading: Boolean = true,
    val firstName: String = "",
    val zone: ZoneId = ZoneId.systemDefault(),
    val now: Instant = Instant.EPOCH,
    val schedule: WorkSchedule? = null,
    val status: AvailabilityStatus = AvailabilityStatus.SIN_FICHAR,
    val busyUntil: Instant? = null,
    val modality: Modality = Modality.REMOTO,
    val modalityLocked: Boolean = false,
    val primaryAction: ClockType? = null,
    val canClockOut: Boolean = false,
    val events: List<ClockEvent> = emptyList(),
    val worked: Duration = Duration.ZERO,
    val upcomingSlots: List<CalendarSlot> = emptyList(),
    val nextHoliday: Holiday? = null,
    val isOnline: Boolean = true,
    val lastSync: Instant? = null,
    val pendingCount: Int = 0,
    val showExitConfirm: Boolean = false,
    val googleStatus: GoogleAccountStatus = GoogleAccountStatus.CONNECTED,
)

sealed interface HomeEvent {
    data class ClockRegistered(val event: ClockEvent, val wasOnline: Boolean) : HomeEvent
    data object InvalidSequence : HomeEvent
}

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val auth: AuthRepository,
    private val prefs: PreferencesRepository,
    private val clockRepository: ClockRepository,
    private val teamRepository: TeamRepository,
    private val connectivity: ConnectivityRepository,
    private val registerClockEvent: RegisterClockEventUseCase,
    private val undoClockEvent: UndoClockEventUseCase,
    private val computeAvailability: ComputeAvailability,
    private val clock: Clock,
) : ViewModel() {

    private data class DayKey(val userId: String, val zoneId: String, val date: LocalDate)
    private data class Core(val session: Session, val prefs: UserPreferences, val events: List<ClockEvent>, val now: Instant, val slots: List<CalendarSlot>)
    private data class Env(val online: Boolean, val lastSync: Instant?, val pending: Int, val holiday: Holiday?, val google: GoogleAccountStatus)

    private val sessionFlow = auth.session.filterNotNull()

    private val dayKey = combine(sessionFlow, prefs.preferences, tickerFlow(clock)) { s, p, now ->
        DayKey(s.userId, p.schedule.zoneId, now.atZone(ZoneId.of(p.schedule.zoneId)).toLocalDate())
    }.distinctUntilChanged()

    private val dayEvents = dayKey.flatMapLatest { k ->
        clockRepository.observeDay(k.userId, k.date, ZoneId.of(k.zoneId))
    }
    private val slots = dayKey.flatMapLatest { k ->
        val z = ZoneId.of(k.zoneId)
        teamRepository.observeSlots(k.userId, k.date.atStartOfDay(z).toInstant(), k.date.plusDays(1).atStartOfDay(z).toInstant())
    }
    private val holiday = dayKey.flatMapLatest { teamRepository.observeNextHoliday(it.date) }

    private val chosenModality = MutableStateFlow<Modality?>(null)
    private val showConfirm = MutableStateFlow(false)

    private val core = combine(sessionFlow, prefs.preferences, dayEvents, tickerFlow(clock), slots) { s, p, e, n, sl ->
        Core(s, p, e, n, sl)
    }
    private val env = combine(
        connectivity.isOnline, prefs.lastSync, clockRepository.observePendingCount(), holiday, auth.googleStatus,
    ) { online, last, pending, hol, google -> Env(online, last, pending, hol, google) }

    val uiState: StateFlow<HomeUiState> = combine(core, env, chosenModality, showConfirm) { c, e, chosen, confirm ->
        val lastType = c.events.lastOrNull()?.type
        val entry = c.events.firstOrNull { it.type == ClockType.ENTRADA }
        val availability = computeAvailability(c.now, c.prefs.schedule, c.events, c.slots)
        HomeUiState(
            isLoading = false,
            firstName = c.session.firstName,
            zone = ZoneId.of(c.prefs.schedule.zoneId),
            now = c.now,
            schedule = c.prefs.schedule,
            status = availability.status,
            busyUntil = availability.busyUntil,
            modality = entry?.modality ?: chosen ?: Modality.REMOTO,
            modalityLocked = entry != null,
            primaryAction = ClockRules.primaryAction(lastType),
            canClockOut = ClockRules.canClockOut(lastType),
            events = c.events,
            worked = ClockRules.workedDuration(c.events, c.now),
            upcomingSlots = c.slots.filter { it.end.isAfter(c.now) }.take(3),
            nextHoliday = e.holiday,
            isOnline = e.online,
            lastSync = e.lastSync,
            pendingCount = e.pending,
            showExitConfirm = confirm,
            googleStatus = e.google,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    private val _events = MutableSharedFlow<HomeEvent>(extraBufferCapacity = 4)
    val events: SharedFlow<HomeEvent> = _events.asSharedFlow()

    fun onModalitySelected(m: Modality) { chosenModality.value = m }

    fun onPrimaryClick() {
        val s = uiState.value
        s.primaryAction?.let { clock(it, s.modality) }
    }

    /** Fichar salida suma un toque de confirmación (RNF02). */
    fun onClockOutClick() { showConfirm.value = true }
    fun onDismissConfirm() { showConfirm.value = false }
    fun onConfirmClockOut() {
        showConfirm.value = false
        clock(ClockType.SALIDA, uiState.value.modality)
    }

    fun onUndo(eventId: String) { viewModelScope.launch { undoClockEvent(eventId) } }

    private fun clock(type: ClockType, modality: Modality) {
        viewModelScope.launch {
            when (val r = registerClockEvent(type, modality)) {
                is RegisterResult.Registered -> _events.tryEmit(HomeEvent.ClockRegistered(r.event, uiState.value.isOnline))
                else -> _events.tryEmit(HomeEvent.InvalidSequence)
            }
        }
    }
}
