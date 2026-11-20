package com.meridian.app.data.local.Dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.meridian.app.data.local.TeamEntity
import com.meridian.app.data.local.TeamMemberEntity
import kotlinx.coroutines.flow.Flow

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