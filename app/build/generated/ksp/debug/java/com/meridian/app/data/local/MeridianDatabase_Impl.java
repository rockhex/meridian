package com.meridian.app.data.local;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class MeridianDatabase_Impl extends MeridianDatabase {
  private volatile ClockEventDao _clockEventDao;

  private volatile TeamDao _teamDao;

  private volatile CalendarDao _calendarDao;

  private volatile HolidayDao _holidayDao;

  private volatile OfficePlanDao _officePlanDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `clock_events` (`id` TEXT NOT NULL, `userId` TEXT NOT NULL, `type` TEXT NOT NULL, `timestampMs` INTEGER NOT NULL, `modality` TEXT NOT NULL, `syncState` TEXT NOT NULL, `voided` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `team` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `officeName` TEXT, `officeLat` REAL, `officeLon` REAL, `officeRadius` REAL, `fixedDays` TEXT NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `team_members` (`userId` TEXT NOT NULL, `teamId` TEXT NOT NULL, `name` TEXT NOT NULL, `country` TEXT NOT NULL, `zoneId` TEXT NOT NULL, `status` TEXT NOT NULL, `busyUntilMs` INTEGER, `onsiteToday` INTEGER NOT NULL, `updatedAtMs` INTEGER NOT NULL, PRIMARY KEY(`userId`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `calendar_slots` (`id` TEXT NOT NULL, `userId` TEXT NOT NULL, `startMs` INTEGER NOT NULL, `endMs` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `holidays` (`date` TEXT NOT NULL, `name` TEXT NOT NULL, `country` TEXT NOT NULL, PRIMARY KEY(`date`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `office_plan` (`userId` TEXT NOT NULL, `date` TEXT NOT NULL, `going` INTEGER NOT NULL, `syncState` TEXT NOT NULL, PRIMARY KEY(`userId`, `date`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '6dc4d27e83e3f644cbe698c8c1a8efd0')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `clock_events`");
        db.execSQL("DROP TABLE IF EXISTS `team`");
        db.execSQL("DROP TABLE IF EXISTS `team_members`");
        db.execSQL("DROP TABLE IF EXISTS `calendar_slots`");
        db.execSQL("DROP TABLE IF EXISTS `holidays`");
        db.execSQL("DROP TABLE IF EXISTS `office_plan`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsClockEvents = new HashMap<String, TableInfo.Column>(7);
        _columnsClockEvents.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsClockEvents.put("userId", new TableInfo.Column("userId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsClockEvents.put("type", new TableInfo.Column("type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsClockEvents.put("timestampMs", new TableInfo.Column("timestampMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsClockEvents.put("modality", new TableInfo.Column("modality", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsClockEvents.put("syncState", new TableInfo.Column("syncState", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsClockEvents.put("voided", new TableInfo.Column("voided", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysClockEvents = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesClockEvents = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoClockEvents = new TableInfo("clock_events", _columnsClockEvents, _foreignKeysClockEvents, _indicesClockEvents);
        final TableInfo _existingClockEvents = TableInfo.read(db, "clock_events");
        if (!_infoClockEvents.equals(_existingClockEvents)) {
          return new RoomOpenHelper.ValidationResult(false, "clock_events(com.meridian.app.data.local.ClockEventEntity).\n"
                  + " Expected:\n" + _infoClockEvents + "\n"
                  + " Found:\n" + _existingClockEvents);
        }
        final HashMap<String, TableInfo.Column> _columnsTeam = new HashMap<String, TableInfo.Column>(7);
        _columnsTeam.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTeam.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTeam.put("officeName", new TableInfo.Column("officeName", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTeam.put("officeLat", new TableInfo.Column("officeLat", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTeam.put("officeLon", new TableInfo.Column("officeLon", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTeam.put("officeRadius", new TableInfo.Column("officeRadius", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTeam.put("fixedDays", new TableInfo.Column("fixedDays", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysTeam = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesTeam = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoTeam = new TableInfo("team", _columnsTeam, _foreignKeysTeam, _indicesTeam);
        final TableInfo _existingTeam = TableInfo.read(db, "team");
        if (!_infoTeam.equals(_existingTeam)) {
          return new RoomOpenHelper.ValidationResult(false, "team(com.meridian.app.data.local.TeamEntity).\n"
                  + " Expected:\n" + _infoTeam + "\n"
                  + " Found:\n" + _existingTeam);
        }
        final HashMap<String, TableInfo.Column> _columnsTeamMembers = new HashMap<String, TableInfo.Column>(9);
        _columnsTeamMembers.put("userId", new TableInfo.Column("userId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTeamMembers.put("teamId", new TableInfo.Column("teamId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTeamMembers.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTeamMembers.put("country", new TableInfo.Column("country", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTeamMembers.put("zoneId", new TableInfo.Column("zoneId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTeamMembers.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTeamMembers.put("busyUntilMs", new TableInfo.Column("busyUntilMs", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTeamMembers.put("onsiteToday", new TableInfo.Column("onsiteToday", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTeamMembers.put("updatedAtMs", new TableInfo.Column("updatedAtMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysTeamMembers = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesTeamMembers = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoTeamMembers = new TableInfo("team_members", _columnsTeamMembers, _foreignKeysTeamMembers, _indicesTeamMembers);
        final TableInfo _existingTeamMembers = TableInfo.read(db, "team_members");
        if (!_infoTeamMembers.equals(_existingTeamMembers)) {
          return new RoomOpenHelper.ValidationResult(false, "team_members(com.meridian.app.data.local.TeamMemberEntity).\n"
                  + " Expected:\n" + _infoTeamMembers + "\n"
                  + " Found:\n" + _existingTeamMembers);
        }
        final HashMap<String, TableInfo.Column> _columnsCalendarSlots = new HashMap<String, TableInfo.Column>(4);
        _columnsCalendarSlots.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCalendarSlots.put("userId", new TableInfo.Column("userId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCalendarSlots.put("startMs", new TableInfo.Column("startMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCalendarSlots.put("endMs", new TableInfo.Column("endMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCalendarSlots = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCalendarSlots = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoCalendarSlots = new TableInfo("calendar_slots", _columnsCalendarSlots, _foreignKeysCalendarSlots, _indicesCalendarSlots);
        final TableInfo _existingCalendarSlots = TableInfo.read(db, "calendar_slots");
        if (!_infoCalendarSlots.equals(_existingCalendarSlots)) {
          return new RoomOpenHelper.ValidationResult(false, "calendar_slots(com.meridian.app.data.local.CalendarSlotEntity).\n"
                  + " Expected:\n" + _infoCalendarSlots + "\n"
                  + " Found:\n" + _existingCalendarSlots);
        }
        final HashMap<String, TableInfo.Column> _columnsHolidays = new HashMap<String, TableInfo.Column>(3);
        _columnsHolidays.put("date", new TableInfo.Column("date", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHolidays.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHolidays.put("country", new TableInfo.Column("country", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysHolidays = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesHolidays = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoHolidays = new TableInfo("holidays", _columnsHolidays, _foreignKeysHolidays, _indicesHolidays);
        final TableInfo _existingHolidays = TableInfo.read(db, "holidays");
        if (!_infoHolidays.equals(_existingHolidays)) {
          return new RoomOpenHelper.ValidationResult(false, "holidays(com.meridian.app.data.local.HolidayEntity).\n"
                  + " Expected:\n" + _infoHolidays + "\n"
                  + " Found:\n" + _existingHolidays);
        }
        final HashMap<String, TableInfo.Column> _columnsOfficePlan = new HashMap<String, TableInfo.Column>(4);
        _columnsOfficePlan.put("userId", new TableInfo.Column("userId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOfficePlan.put("date", new TableInfo.Column("date", "TEXT", true, 2, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOfficePlan.put("going", new TableInfo.Column("going", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOfficePlan.put("syncState", new TableInfo.Column("syncState", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysOfficePlan = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesOfficePlan = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoOfficePlan = new TableInfo("office_plan", _columnsOfficePlan, _foreignKeysOfficePlan, _indicesOfficePlan);
        final TableInfo _existingOfficePlan = TableInfo.read(db, "office_plan");
        if (!_infoOfficePlan.equals(_existingOfficePlan)) {
          return new RoomOpenHelper.ValidationResult(false, "office_plan(com.meridian.app.data.local.OfficePlanEntity).\n"
                  + " Expected:\n" + _infoOfficePlan + "\n"
                  + " Found:\n" + _existingOfficePlan);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "6dc4d27e83e3f644cbe698c8c1a8efd0", "71b3945efe3b698491d7eda1927cd6e5");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "clock_events","team","team_members","calendar_slots","holidays","office_plan");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `clock_events`");
      _db.execSQL("DELETE FROM `team`");
      _db.execSQL("DELETE FROM `team_members`");
      _db.execSQL("DELETE FROM `calendar_slots`");
      _db.execSQL("DELETE FROM `holidays`");
      _db.execSQL("DELETE FROM `office_plan`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(ClockEventDao.class, ClockEventDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(TeamDao.class, TeamDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(CalendarDao.class, CalendarDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(HolidayDao.class, HolidayDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(OfficePlanDao.class, OfficePlanDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public ClockEventDao clockEventDao() {
    if (_clockEventDao != null) {
      return _clockEventDao;
    } else {
      synchronized(this) {
        if(_clockEventDao == null) {
          _clockEventDao = new ClockEventDao_Impl(this);
        }
        return _clockEventDao;
      }
    }
  }

  @Override
  public TeamDao teamDao() {
    if (_teamDao != null) {
      return _teamDao;
    } else {
      synchronized(this) {
        if(_teamDao == null) {
          _teamDao = new TeamDao_Impl(this);
        }
        return _teamDao;
      }
    }
  }

  @Override
  public CalendarDao calendarDao() {
    if (_calendarDao != null) {
      return _calendarDao;
    } else {
      synchronized(this) {
        if(_calendarDao == null) {
          _calendarDao = new CalendarDao_Impl(this);
        }
        return _calendarDao;
      }
    }
  }

  @Override
  public HolidayDao holidayDao() {
    if (_holidayDao != null) {
      return _holidayDao;
    } else {
      synchronized(this) {
        if(_holidayDao == null) {
          _holidayDao = new HolidayDao_Impl(this);
        }
        return _holidayDao;
      }
    }
  }

  @Override
  public OfficePlanDao officePlanDao() {
    if (_officePlanDao != null) {
      return _officePlanDao;
    } else {
      synchronized(this) {
        if(_officePlanDao == null) {
          _officePlanDao = new OfficePlanDao_Impl(this);
        }
        return _officePlanDao;
      }
    }
  }
}
