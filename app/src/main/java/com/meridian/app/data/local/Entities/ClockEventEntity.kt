package com.meridian.app.data.local.Entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.meridian.app.domain.model.ClockType
import com.meridian.app.domain.model.Modality
import com.meridian.app.domain.model.SyncState

@Entity(tableName = "clock_events")
data class ClockEventEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val type: ClockType,
    val timestampMs: Long,
    val modality: Modality,
    val syncState: SyncState,
    /** Deshecho por el usuario; se informa al servidor y luego se borra. */
    val voided: Boolean = false,
)