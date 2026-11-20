package com.meridian.app.data.local.Entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "holidays")
data class HolidayEntity(
    @PrimaryKey val date: String, // ISO yyyy-MM-dd
    val name: String,
    val country: String,
)