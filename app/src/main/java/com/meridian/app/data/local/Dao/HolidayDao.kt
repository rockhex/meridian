package com.meridian.app.data.local.Dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.meridian.app.data.local.Entities.HolidayEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HolidayDao {
    @Query("SELECT * FROM holidays WHERE date >= :from ORDER BY date ASC LIMIT 1")
    fun observeNext(from: String): Flow<HolidayEntity?>

    @Query("SELECT * FROM holidays WHERE date BETWEEN :from AND :to")
    fun observeRange(from: String, to: String): Flow<List<HolidayEntity>>

    @Query("SELECT COUNT(*) FROM holidays WHERE date LIKE :yearPrefix")
    suspend fun countForYear(yearPrefix: String): Int

    @Upsert suspend fun upsertAll(items: List<HolidayEntity>)
}