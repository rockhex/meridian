package com.meridian.app.data.local.Entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "team")
data class TeamEntity(
    @PrimaryKey val id: String,
    val name: String,
    val officeName: String?,
    val officeLat: Double?,
    val officeLon: Double?,
    val officeRadius: Float?,
    /** DayOfWeek separados por coma. */
    val fixedDays: String,
)