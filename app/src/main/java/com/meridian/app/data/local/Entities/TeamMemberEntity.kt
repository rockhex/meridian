package com.meridian.app.data.local.Entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.meridian.app.domain.model.AvailabilityStatus

@Entity(tableName = "team_members")
data class TeamMemberEntity(
    @PrimaryKey val userId: String,
    val teamId: String,
    val name: String,
    val country: String,
    val zoneId: String,
    val status: AvailabilityStatus,
    val busyUntilMs: Long?,
    val onsiteToday: Boolean,
    val updatedAtMs: Long,
)