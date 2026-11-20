package com.meridian.app.data.repository

import com.meridian.app.data.local.TeamDao
import com.meridian.app.data.remote.TeamRealtimeSource
import com.meridian.app.domain.repository.AuthRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

/** Mientras la app está en primer plano, aplica a la caché local los cambios de estado por WebSocket. */
class RealtimeCoordinator(
    private val auth: AuthRepository,
    private val source: TeamRealtimeSource,
    private val teamDao: TeamDao,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun run() {
        auth.session.filterNotNull()
            .distinctUntilChanged { a, b -> a.teamId == b.teamId }
            .flatMapLatest { s -> source.updates(s.teamId).map { s.teamId to it } }
            .collect { (teamId, dto) -> teamDao.upsertMember(dto.toEntity(teamId)) }
    }
}
