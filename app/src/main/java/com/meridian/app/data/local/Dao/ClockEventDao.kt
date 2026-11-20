package com.meridian.app.data.local.Dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.meridian.app.data.local.ClockEventEntity
import com.meridian.app.domain.model.SyncState
import kotlinx.coroutines.flow.Flow

@Dao
interface ClockEventDao {
    @Query("SELECT * FROM clock_events WHERE userId = :userId AND timestampMs >= :fromMs AND timestampMs < :toMs AND voided = 0 ORDER BY timestampMs ASC")
    fun observeBetween(userId: String, fromMs: Long, toMs: Long): Flow<List<ClockEventEntity>>

    @Query("SELECT * FROM clock_events WHERE userId = :userId AND timestampMs >= :fromMs AND timestampMs < :toMs AND voided = 0 ORDER BY timestampMs ASC")
    suspend fun getBetween(userId: String, fromMs: Long, toMs: Long): List<ClockEventEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(event: ClockEventEntity): Long

    @Query("SELECT * FROM clock_events WHERE syncState = 'PENDING' ORDER BY timestampMs ASC")
    suspend fun pending(): List<ClockEventEntity>

    @Query("SELECT COUNT(*) FROM clock_events WHERE syncState = 'PENDING' AND voided = 0")
    fun observePendingCount(): Flow<Int>

    // "AND voided = 0": si el usuario deshizo mientras se enviaba, no pisamos el PENDING del void.
    @Query("UPDATE clock_events SET syncState = :state WHERE id = :id AND voided = 0")
    suspend fun setState(id: String, state: SyncState)

    @Query("UPDATE clock_events SET voided = 1, syncState = 'PENDING' WHERE id = :id")
    suspend fun markVoided(id: String)

    @Query("DELETE FROM clock_events WHERE id = :id")
    suspend fun delete(id: String)
}
