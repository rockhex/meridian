package com.meridian.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
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

@Dao
interface TeamDao {
    @Query("SELECT * FROM team WHERE id = :id")
    fun observeTeam(id: String): Flow<TeamEntity?>

    @Query("SELECT * FROM team WHERE id = :id")
    suspend fun getTeam(id: String): TeamEntity?

    @Query("SELECT * FROM team_members WHERE teamId = :teamId ORDER BY name ASC")
    fun observeMembers(teamId: String): Flow<List<TeamMemberEntity>>

    @Upsert suspend fun upsertTeam(team: TeamEntity)
    @Upsert suspend fun upsertMembers(members: List<TeamMemberEntity>)
    @Upsert suspend fun upsertMember(member: TeamMemberEntity)

    @Query("DELETE FROM team_members WHERE teamId = :teamId")
    suspend fun deleteMembers(teamId: String)
}

@Dao
interface CalendarDao {
    @Query("SELECT * FROM calendar_slots WHERE userId = :userId AND endMs > :fromMs AND startMs < :toMs ORDER BY startMs ASC")
    fun observe(userId: String, fromMs: Long, toMs: Long): Flow<List<CalendarSlotEntity>>

    @Query("DELETE FROM calendar_slots WHERE userId = :userId")
    suspend fun deleteFor(userId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(slots: List<CalendarSlotEntity>)
}

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
