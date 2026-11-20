package com.meridian.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.meridian.app.data.local.Dao.CalendarDao
import com.meridian.app.data.local.Dao.ClockEventDao
import com.meridian.app.data.local.Dao.HolidayDao
import com.meridian.app.data.local.Dao.OfficePlanDao
import com.meridian.app.data.local.Dao.TeamDao
import com.meridian.app.data.local.Entities.ClockEventEntity
import com.meridian.app.data.local.Entities.TeamEntity
import com.meridian.app.data.local.Entities.TeamMemberEntity
import com.meridian.app.data.local.Entities.CalendarSlotEntity
import com.meridian.app.data.local.Entities.HolidayEntity
import com.meridian.app.data.local.Entities.OfficePlanEntity



@Database(
    entities = [
        ClockEventEntity::class, TeamEntity::class, TeamMemberEntity::class,
        CalendarSlotEntity::class, HolidayEntity::class, OfficePlanEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class MeridianDatabase : RoomDatabase() {
    abstract fun clockEventDao(): ClockEventDao
    abstract fun teamDao(): TeamDao
    abstract fun calendarDao(): CalendarDao
    abstract fun holidayDao(): HolidayDao
    abstract fun officePlanDao(): OfficePlanDao

    companion object {
        fun create(context: Context): MeridianDatabase =
            Room.databaseBuilder(context, MeridianDatabase::class.java, "meridian.db").build()
    }
}
