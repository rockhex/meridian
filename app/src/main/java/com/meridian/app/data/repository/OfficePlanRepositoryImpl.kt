package com.meridian.app.data.repository

import com.meridian.app.data.local.OfficePlanDao
import com.meridian.app.data.local.OfficePlanEntity
import com.meridian.app.domain.model.PlanEntry
import com.meridian.app.domain.model.SyncState
import com.meridian.app.domain.repository.OfficePlanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class OfficePlanRepositoryImpl(private val dao: OfficePlanDao) : OfficePlanRepository {
    override fun observeRange(from: LocalDate, to: LocalDate): Flow<List<PlanEntry>> =
        dao.observeRange(from.toString(), to.toString()).map { list ->
            list.map { PlanEntry(it.userId, LocalDate.parse(it.date), it.going, it.syncState == SyncState.PENDING) }
        }

    /** Se guarda local como PENDING aunque no haya red (RF04). */
    override suspend fun setGoing(userId: String, date: LocalDate, going: Boolean) {
        dao.upsert(OfficePlanEntity(userId, date.toString(), going, SyncState.PENDING))
    }
}
