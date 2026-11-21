package com.meridian.app.data.local.Dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.meridian.app.data.local.Entities.CalendarSlotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CalendarDao {
    @Query("SELECT * FROM calendar_slots WHERE userId = :userId AND endMs > :fromMs AND startMs < :toMs ORDER BY startMs ASC")
    fun observe(userId: String, fromMs: Long, toMs: Long): Flow<List<CalendarSlotEntity>>

    @Query("DELETE FROM calendar_slots WHERE userId = :userId")
    suspend fun deleteFor(userId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(slots: List<CalendarSlotEntity>)
}