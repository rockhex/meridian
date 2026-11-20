package com.meridian.app.data.local;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.EntityUpsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.meridian.app.domain.model.AvailabilityStatus;
import java.lang.Class;
import java.lang.Double;
import java.lang.Exception;
import java.lang.Float;
import java.lang.IllegalArgumentException;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class TeamDao_Impl implements TeamDao {
  private final RoomDatabase __db;

  private final SharedSQLiteStatement __preparedStmtOfDeleteMembers;

  private final EntityUpsertionAdapter<TeamEntity> __upsertionAdapterOfTeamEntity;

  private final EntityUpsertionAdapter<TeamMemberEntity> __upsertionAdapterOfTeamMemberEntity;

  public TeamDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__preparedStmtOfDeleteMembers = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM team_members WHERE teamId = ?";
        return _query;
      }
    };
    this.__upsertionAdapterOfTeamEntity = new EntityUpsertionAdapter<TeamEntity>(new EntityInsertionAdapter<TeamEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT INTO `team` (`id`,`name`,`officeName`,`officeLat`,`officeLon`,`officeRadius`,`fixedDays`) VALUES (?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TeamEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getName());
        if (entity.getOfficeName() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getOfficeName());
        }
        if (entity.getOfficeLat() == null) {
          statement.bindNull(4);
        } else {
          statement.bindDouble(4, entity.getOfficeLat());
        }
        if (entity.getOfficeLon() == null) {
          statement.bindNull(5);
        } else {
          statement.bindDouble(5, entity.getOfficeLon());
        }
        if (entity.getOfficeRadius() == null) {
          statement.bindNull(6);
        } else {
          statement.bindDouble(6, entity.getOfficeRadius());
        }
        statement.bindString(7, entity.getFixedDays());
      }
    }, new EntityDeletionOrUpdateAdapter<TeamEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE `team` SET `id` = ?,`name` = ?,`officeName` = ?,`officeLat` = ?,`officeLon` = ?,`officeRadius` = ?,`fixedDays` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TeamEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getName());
        if (entity.getOfficeName() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getOfficeName());
        }
        if (entity.getOfficeLat() == null) {
          statement.bindNull(4);
        } else {
          statement.bindDouble(4, entity.getOfficeLat());
        }
        if (entity.getOfficeLon() == null) {
          statement.bindNull(5);
        } else {
          statement.bindDouble(5, entity.getOfficeLon());
        }
        if (entity.getOfficeRadius() == null) {
          statement.bindNull(6);
        } else {
          statement.bindDouble(6, entity.getOfficeRadius());
        }
        statement.bindString(7, entity.getFixedDays());
        statement.bindString(8, entity.getId());
      }
    });
    this.__upsertionAdapterOfTeamMemberEntity = new EntityUpsertionAdapter<TeamMemberEntity>(new EntityInsertionAdapter<TeamMemberEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT INTO `team_members` (`userId`,`teamId`,`name`,`country`,`zoneId`,`status`,`busyUntilMs`,`onsiteToday`,`updatedAtMs`) VALUES (?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TeamMemberEntity entity) {
        statement.bindString(1, entity.getUserId());
        statement.bindString(2, entity.getTeamId());
        statement.bindString(3, entity.getName());
        statement.bindString(4, entity.getCountry());
        statement.bindString(5, entity.getZoneId());
        statement.bindString(6, __AvailabilityStatus_enumToString(entity.getStatus()));
        if (entity.getBusyUntilMs() == null) {
          statement.bindNull(7);
        } else {
          statement.bindLong(7, entity.getBusyUntilMs());
        }
        final int _tmp = entity.getOnsiteToday() ? 1 : 0;
        statement.bindLong(8, _tmp);
        statement.bindLong(9, entity.getUpdatedAtMs());
      }
    }, new EntityDeletionOrUpdateAdapter<TeamMemberEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE `team_members` SET `userId` = ?,`teamId` = ?,`name` = ?,`country` = ?,`zoneId` = ?,`status` = ?,`busyUntilMs` = ?,`onsiteToday` = ?,`updatedAtMs` = ? WHERE `userId` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TeamMemberEntity entity) {
        statement.bindString(1, entity.getUserId());
        statement.bindString(2, entity.getTeamId());
        statement.bindString(3, entity.getName());
        statement.bindString(4, entity.getCountry());
        statement.bindString(5, entity.getZoneId());
        statement.bindString(6, __AvailabilityStatus_enumToString(entity.getStatus()));
        if (entity.getBusyUntilMs() == null) {
          statement.bindNull(7);
        } else {
          statement.bindLong(7, entity.getBusyUntilMs());
        }
        final int _tmp = entity.getOnsiteToday() ? 1 : 0;
        statement.bindLong(8, _tmp);
        statement.bindLong(9, entity.getUpdatedAtMs());
        statement.bindString(10, entity.getUserId());
      }
    });
  }

  @Override
  public Object deleteMembers(final String teamId, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteMembers.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, teamId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteMembers.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object upsertTeam(final TeamEntity team, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __upsertionAdapterOfTeamEntity.upsert(team);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object upsertMembers(final List<TeamMemberEntity> members,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __upsertionAdapterOfTeamMemberEntity.upsert(members);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object upsertMember(final TeamMemberEntity member,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __upsertionAdapterOfTeamMemberEntity.upsert(member);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<TeamEntity> observeTeam(final String id) {
    final String _sql = "SELECT * FROM team WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"team"}, new Callable<TeamEntity>() {
      @Override
      @Nullable
      public TeamEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfOfficeName = CursorUtil.getColumnIndexOrThrow(_cursor, "officeName");
          final int _cursorIndexOfOfficeLat = CursorUtil.getColumnIndexOrThrow(_cursor, "officeLat");
          final int _cursorIndexOfOfficeLon = CursorUtil.getColumnIndexOrThrow(_cursor, "officeLon");
          final int _cursorIndexOfOfficeRadius = CursorUtil.getColumnIndexOrThrow(_cursor, "officeRadius");
          final int _cursorIndexOfFixedDays = CursorUtil.getColumnIndexOrThrow(_cursor, "fixedDays");
          final TeamEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpOfficeName;
            if (_cursor.isNull(_cursorIndexOfOfficeName)) {
              _tmpOfficeName = null;
            } else {
              _tmpOfficeName = _cursor.getString(_cursorIndexOfOfficeName);
            }
            final Double _tmpOfficeLat;
            if (_cursor.isNull(_cursorIndexOfOfficeLat)) {
              _tmpOfficeLat = null;
            } else {
              _tmpOfficeLat = _cursor.getDouble(_cursorIndexOfOfficeLat);
            }
            final Double _tmpOfficeLon;
            if (_cursor.isNull(_cursorIndexOfOfficeLon)) {
              _tmpOfficeLon = null;
            } else {
              _tmpOfficeLon = _cursor.getDouble(_cursorIndexOfOfficeLon);
            }
            final Float _tmpOfficeRadius;
            if (_cursor.isNull(_cursorIndexOfOfficeRadius)) {
              _tmpOfficeRadius = null;
            } else {
              _tmpOfficeRadius = _cursor.getFloat(_cursorIndexOfOfficeRadius);
            }
            final String _tmpFixedDays;
            _tmpFixedDays = _cursor.getString(_cursorIndexOfFixedDays);
            _result = new TeamEntity(_tmpId,_tmpName,_tmpOfficeName,_tmpOfficeLat,_tmpOfficeLon,_tmpOfficeRadius,_tmpFixedDays);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getTeam(final String id, final Continuation<? super TeamEntity> $completion) {
    final String _sql = "SELECT * FROM team WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<TeamEntity>() {
      @Override
      @Nullable
      public TeamEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfOfficeName = CursorUtil.getColumnIndexOrThrow(_cursor, "officeName");
          final int _cursorIndexOfOfficeLat = CursorUtil.getColumnIndexOrThrow(_cursor, "officeLat");
          final int _cursorIndexOfOfficeLon = CursorUtil.getColumnIndexOrThrow(_cursor, "officeLon");
          final int _cursorIndexOfOfficeRadius = CursorUtil.getColumnIndexOrThrow(_cursor, "officeRadius");
          final int _cursorIndexOfFixedDays = CursorUtil.getColumnIndexOrThrow(_cursor, "fixedDays");
          final TeamEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpOfficeName;
            if (_cursor.isNull(_cursorIndexOfOfficeName)) {
              _tmpOfficeName = null;
            } else {
              _tmpOfficeName = _cursor.getString(_cursorIndexOfOfficeName);
            }
            final Double _tmpOfficeLat;
            if (_cursor.isNull(_cursorIndexOfOfficeLat)) {
              _tmpOfficeLat = null;
            } else {
              _tmpOfficeLat = _cursor.getDouble(_cursorIndexOfOfficeLat);
            }
            final Double _tmpOfficeLon;
            if (_cursor.isNull(_cursorIndexOfOfficeLon)) {
              _tmpOfficeLon = null;
            } else {
              _tmpOfficeLon = _cursor.getDouble(_cursorIndexOfOfficeLon);
            }
            final Float _tmpOfficeRadius;
            if (_cursor.isNull(_cursorIndexOfOfficeRadius)) {
              _tmpOfficeRadius = null;
            } else {
              _tmpOfficeRadius = _cursor.getFloat(_cursorIndexOfOfficeRadius);
            }
            final String _tmpFixedDays;
            _tmpFixedDays = _cursor.getString(_cursorIndexOfFixedDays);
            _result = new TeamEntity(_tmpId,_tmpName,_tmpOfficeName,_tmpOfficeLat,_tmpOfficeLon,_tmpOfficeRadius,_tmpFixedDays);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<TeamMemberEntity>> observeMembers(final String teamId) {
    final String _sql = "SELECT * FROM team_members WHERE teamId = ? ORDER BY name ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, teamId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"team_members"}, new Callable<List<TeamMemberEntity>>() {
      @Override
      @NonNull
      public List<TeamMemberEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfTeamId = CursorUtil.getColumnIndexOrThrow(_cursor, "teamId");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfCountry = CursorUtil.getColumnIndexOrThrow(_cursor, "country");
          final int _cursorIndexOfZoneId = CursorUtil.getColumnIndexOrThrow(_cursor, "zoneId");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfBusyUntilMs = CursorUtil.getColumnIndexOrThrow(_cursor, "busyUntilMs");
          final int _cursorIndexOfOnsiteToday = CursorUtil.getColumnIndexOrThrow(_cursor, "onsiteToday");
          final int _cursorIndexOfUpdatedAtMs = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAtMs");
          final List<TeamMemberEntity> _result = new ArrayList<TeamMemberEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TeamMemberEntity _item;
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final String _tmpTeamId;
            _tmpTeamId = _cursor.getString(_cursorIndexOfTeamId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpCountry;
            _tmpCountry = _cursor.getString(_cursorIndexOfCountry);
            final String _tmpZoneId;
            _tmpZoneId = _cursor.getString(_cursorIndexOfZoneId);
            final AvailabilityStatus _tmpStatus;
            _tmpStatus = __AvailabilityStatus_stringToEnum(_cursor.getString(_cursorIndexOfStatus));
            final Long _tmpBusyUntilMs;
            if (_cursor.isNull(_cursorIndexOfBusyUntilMs)) {
              _tmpBusyUntilMs = null;
            } else {
              _tmpBusyUntilMs = _cursor.getLong(_cursorIndexOfBusyUntilMs);
            }
            final boolean _tmpOnsiteToday;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfOnsiteToday);
            _tmpOnsiteToday = _tmp != 0;
            final long _tmpUpdatedAtMs;
            _tmpUpdatedAtMs = _cursor.getLong(_cursorIndexOfUpdatedAtMs);
            _item = new TeamMemberEntity(_tmpUserId,_tmpTeamId,_tmpName,_tmpCountry,_tmpZoneId,_tmpStatus,_tmpBusyUntilMs,_tmpOnsiteToday,_tmpUpdatedAtMs);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }

  private String __AvailabilityStatus_enumToString(@NonNull final AvailabilityStatus _value) {
    switch (_value) {
      case SIN_FICHAR: return "SIN_FICHAR";
      case DISPONIBLE: return "DISPONIBLE";
      case EN_LLAMADA: return "EN_LLAMADA";
      case EN_DESCANSO: return "EN_DESCANSO";
      case FUERA_DE_HORARIO: return "FUERA_DE_HORARIO";
      default: throw new IllegalArgumentException("Can't convert enum to string, unknown enum value: " + _value);
    }
  }

  private AvailabilityStatus __AvailabilityStatus_stringToEnum(@NonNull final String _value) {
    switch (_value) {
      case "SIN_FICHAR": return AvailabilityStatus.SIN_FICHAR;
      case "DISPONIBLE": return AvailabilityStatus.DISPONIBLE;
      case "EN_LLAMADA": return AvailabilityStatus.EN_LLAMADA;
      case "EN_DESCANSO": return AvailabilityStatus.EN_DESCANSO;
      case "FUERA_DE_HORARIO": return AvailabilityStatus.FUERA_DE_HORARIO;
      default: throw new IllegalArgumentException("Can't convert value to enum, unknown value: " + _value);
    }
  }
}
