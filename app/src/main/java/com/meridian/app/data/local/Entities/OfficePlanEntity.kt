package com.meridian.app.data.local.Entities

import androidx.room.Entity
import com.meridian.app.domain.model.SyncState

@Entity(tableName = "office_plan", primaryKeys = ["userId", "date"])
data class OfficePlanEntity(
    val userId: String,
    val date: String, // ISO yyyy-MM-dd
    val going: Boolean,
    val syncState: SyncState,
)