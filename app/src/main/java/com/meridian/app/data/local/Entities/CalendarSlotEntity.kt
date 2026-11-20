package com.meridian.app.data.local.Entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calendar_slots")
data class CalendarSlotEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val startMs: Long,
    val endMs: Long,
)