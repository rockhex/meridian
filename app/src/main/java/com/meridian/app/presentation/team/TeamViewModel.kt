package com.meridian.app.presentation.team

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meridian.app.domain.model.*
import com.meridian.app.domain.repository.*
import com.meridian.app.domain.usecase.*
import com.meridian.app.presentation.components.initialsOf
import com.meridian.app.presentation.util.cityOf
import com.meridian.app.presentation.util.formatTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

enum class TeamPhase { LOADING, CONTENT, EMPTY, OFFLINE_NO_DATA }
enum class TeamFilter { TODOS, DISPONIBLES, EN_OFICINA }

data class MemberUi(
    val userId: String,
    val name: String,
    val initials: String,
    val place: String,
    val localTime: String,
    val status: AvailabilityStatus,
    val statusText: String,
    val isStale: Boolean,
    val isSelf: Boolean,
    val onsiteToday: Boolean,
)

data class TeamUiState(
    val phase: TeamPhase = TeamPhase.LOADING,
    val teamName: String = "",
    val isLeader: Boolean = false,
    val members: List<MemberUi> = emptyList(),
    val totalMembers: Int = 0,
    val summary: TeamSummary? = null,
    val filter: TeamFilter = TeamFilter.TODOS,
    val isOnline: Boolean = true,
    val lastSync: Instant? = null,
    val now: Instant = Instant.EPOCH,
    val anyStale: Boolean = false,
)

@OptIn(ExperimentalCoroutinesApi::class)
class TeamViewModel(
    private val auth: AuthRepository,
    private val prefs: PreferencesRepository,
    private val teamRepository: TeamRepository,
    private val connectivity: ConnectivityRepository,
    private val syncRepository: SyncRepository,
    private val syncTrigger: SyncTrigger,
    private val observeOwnAvailability: ObserveOwnAvailabilityUseCase,
    private val clock: Clock,
) : ViewModel() {

    private val filter = MutableStateFlow(TeamFilter.TODOS)
    private val session = auth.session.filterNotNull()

    private data class Data(val session: Session, val team: Team?, val members: List<TeamMember>, val own: Availability, val zone: ZoneId)
    private data class Env(val online: Boolean, val lastSync: Instant?, val syncing: Boolean, val now: Instant)

    private val data = session.flatMapLatest { s ->
        combine(
            teamRepository.observeTeam(s.teamId),
            teamRepository.observeMembers(s.teamId),
            observeOwnAvailability(s.userId),
            prefs.preferences,
        ) { team, members, own, p -> Data(s, team, members, own, ZoneId.of(p.schedule.zoneId)) }
    }
    private val env = combine(connectivity.isOnline, prefs.lastSync, syncRepository.isSyncing, tickerFlow(clock)) { o, l, s, n ->
        Env(o, l, s, n)
    }

    val uiState: StateFlow<TeamUiState> = combine(data, env, filter) { d, e, f ->
        val now = e.now
        // La fila propia usa el estado calculado localmente (siempre al día, también offline).
        val adjusted = d.members.map { m ->
            if (m.userId == d.session.userId) m.copy(status = d.own.status, busyUntil = d.own.busyUntil, updatedAt = now)
            else TeamRules.estimate(m, now)
        }
        val phase = when {
            adjusted.isEmpty() && e.lastSync == null ->
                if (e.syncing || e.online) TeamPhase.LOADING else TeamPhase.OFFLINE_NO_DATA
            adjusted.size <= 1 -> TeamPhase.EMPTY
            else -> TeamPhase.CONTENT
        }
        val filtered = adjusted.filter {
            when (f) {
                TeamFilter.TODOS -> true
                TeamFilter.DISPONIBLES -> it.status == AvailabilityStatus.DISPONIBLE
                TeamFilter.EN_OFICINA -> it.onsiteToday
            }
        }
        val rows = filtered.map { m ->
            val zone = ZoneId.of(m.zoneId)
            MemberUi(
                userId = m.userId, name = m.name, initials = initialsOf(m.name),
                place = "${cityOf(m.zoneId)} · ${m.country}",
                localTime = now.formatTime(zone),
                status = m.status,
                statusText = if (m.status == AvailabilityStatus.EN_LLAMADA && m.busyUntil != null)
                    "En llamada hasta ${m.busyUntil.formatTime(d.zone)}" else "",
                isStale = TeamRules.isStale(m.updatedAt, now),
                isSelf = m.userId == d.session.userId,
                onsiteToday = m.onsiteToday,
            )
        }
        TeamUiState(
            phase = phase,
            teamName = d.team?.name ?: "Tu equipo",
            isLeader = d.session.isLeader,
            members = rows,
            totalMembers = adjusted.size,
            // El líder ve estados actuales agregados, no métricas individuales.
            summary = if (d.session.isLeader) TeamRules.summarize(adjusted) else null,
            filter = f,
            isOnline = e.online,
            lastSync = e.lastSync,
            now = now,
            anyStale = rows.any { it.isStale },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TeamUiState())

    fun onFilterSelected(f: TeamFilter) { filter.value = f }
    fun retry() = syncTrigger.requestSync()
}
