package com.meridian.app.presentation.office

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meridian.app.domain.model.*
import com.meridian.app.domain.repository.AuthRepository
import com.meridian.app.domain.repository.ConnectivityRepository
import com.meridian.app.domain.repository.PreferencesRepository
import com.meridian.app.domain.usecase.ObserveOfficeWeekUseCase
import com.meridian.app.domain.usecase.ToggleOfficeDayUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.*
import java.time.temporal.TemporalAdjusters

data class OfficeUiState(
    val isLoading: Boolean = true,
    val week: OfficeWeek? = null,
    val weekStart: LocalDate = LocalDate.now(),
    val today: LocalDate = LocalDate.now(),
    val isOnline: Boolean = true,
)

@OptIn(ExperimentalCoroutinesApi::class)
class OfficeViewModel(
    private val auth: AuthRepository,
    private val prefs: PreferencesRepository,
    private val connectivity: ConnectivityRepository,
    private val observeOfficeWeek: ObserveOfficeWeekUseCase,
    private val toggleOfficeDay: ToggleOfficeDayUseCase,
    private val clock: Clock,
) : ViewModel() {

    /** Lun-vie de la semana actual; en fin de semana muestra la próxima. */
    private val weekInfo = prefs.preferences.map { p ->
        val today = LocalDate.now(clock.withZone(ZoneId.of(p.schedule.zoneId)))
        val start = if (today.dayOfWeek == DayOfWeek.SATURDAY || today.dayOfWeek == DayOfWeek.SUNDAY)
            today.with(TemporalAdjusters.next(DayOfWeek.MONDAY))
        else today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        today to start
    }.distinctUntilChanged()

    val uiState: StateFlow<OfficeUiState> = combine(
        auth.session.filterNotNull(), weekInfo, connectivity.isOnline,
    ) { s, w, online -> Triple(s, w, online) }
        .flatMapLatest { (s, w, online) ->
            observeOfficeWeek(s, w.second).map { week ->
                OfficeUiState(isLoading = false, week = week, weekStart = w.second, today = w.first, isOnline = online)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OfficeUiState())

    fun onToggle(day: WeekDayPlan) {
        // No se marcan feriados ni días pasados.
        if (day.holidayName != null || day.date.isBefore(uiState.value.today)) return
        viewModelScope.launch { toggleOfficeDay(day.date, !day.going) }
    }
}
