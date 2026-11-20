package com.meridian.app.data.local.Dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.meridian.app.data.local.OfficePlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OfficePlanDao {
    @Query("SELECT * FROM office_plan WHERE date BETWEEN :from AND :to")
    fun observeRange(from: String, to: String): Flow<List<OfficePlanEntity>>

    @Query("SELECT * FROM office_plan WHERE userId = :userId AND syncState = 'PENDING'")
    suspend fun pending(userId: String): List<OfficePlanEntity>

    @Query("UPDATE office_plan SET syncState = 'SYNCED' WHERE userId = :userId AND date = :date AND going = :going")
    suspend fun markSynced(userId: String, date: String, going: Boolean)

    @Upsert suspend fun upsert(entry: OfficePlanEntity)
    @Upsert suspend fun upsertAll(entries: List<OfficePlanEntity>)
}