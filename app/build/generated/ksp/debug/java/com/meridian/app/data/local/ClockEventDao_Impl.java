package com.meridian.app.data.local;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.meridian.app.domain.model.ClockType;
import com.meridian.app.domain.model.Modality;
import com.meridian.app.domain.model.SyncState;
import java.lang.Class;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.lang.Integer;
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
public final class ClockEventDao_Impl implements ClockEventDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ClockEventEntity> __insertionAdapterOfClockEventEntity;

  private final SharedSQLiteStatement __preparedStmtOfSetState;

  private final SharedSQLiteStatement __preparedStmtOfMarkVoided;

  private final SharedSQLiteStatement __preparedStmtOfDelete;

  public ClockEventDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfClockEventEntity = new EntityInsertionAdapter<ClockEventEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR IGNORE INTO `clock_events` (`id`,`userId`,`type`,`timestampMs`,`modality`,`syncState`,`voided`) VALUES (?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ClockEventEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getUserId());
        statement.bindString(3, __ClockType_enumToString(entity.getType()));
        statement.bindLong(4, entity.getTimestampMs());
        statement.bindString(5, __Modality_enumToString(entity.getModality()));
        statement.bindString(6, __SyncState_enumToString(entity.getSyncState()));
        final int _tmp = entity.getVoided() ? 1 : 0;
        statement.bindLong(7, _tmp);
      }
    };
    this.__preparedStmtOfSetState = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE clock_events SET syncState = ? WHERE id = ? AND voided = 0";
        return _query;
      }
    };
    this.__preparedStmtOfMarkVoided = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE clock_events SET voided = 1, syncState = 'PENDING' WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDelete = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM clock_events WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final ClockEventEntity event, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfClockEventEntity.insertAndReturnId(event);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object setState(final String id, final SyncState state,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfSetState.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, __SyncState_enumToString(state));
        _argIndex = 2;
        _stmt.bindString(_argIndex, id);
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
          __preparedStmtOfSetState.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object markVoided(final String id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfMarkVoided.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, id);
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
          __preparedStmtOfMarkVoided.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final String id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDelete.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, id);
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
          __preparedStmtOfDelete.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ClockEventEntity>> observeBetween(final String userId, final long fromMs,
      final long toMs) {
    final String _sql = "SELECT * FROM clock_events WHERE userId = ? AND timestampMs >= ? AND timestampMs < ? AND voided = 0 ORDER BY timestampMs ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindString(_argIndex, userId);
    _argIndex = 2;
    _statement.bindLong(_argIndex, fromMs);
    _argIndex = 3;
    _statement.bindLong(_argIndex, toMs);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"clock_events"}, new Callable<List<ClockEventEntity>>() {
      @Override
      @NonNull
      public List<ClockEventEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfTimestampMs = CursorUtil.getColumnIndexOrThrow(_cursor, "timestampMs");
          final int _cursorIndexOfModality = CursorUtil.getColumnIndexOrThrow(_cursor, "modality");
          final int _cursorIndexOfSyncState = CursorUtil.getColumnIndexOrThrow(_cursor, "syncState");
          final int _cursorIndexOfVoided = CursorUtil.getColumnIndexOrThrow(_cursor, "voided");
          final List<ClockEventEntity> _result = new ArrayList<ClockEventEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ClockEventEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final ClockType _tmpType;
            _tmpType = __ClockType_stringToEnum(_cursor.getString(_cursorIndexOfType));
            final long _tmpTimestampMs;
            _tmpTimestampMs = _cursor.getLong(_cursorIndexOfTimestampMs);
            final Modality _tmpModality;
            _tmpModality = __Modality_stringToEnum(_cursor.getString(_cursorIndexOfModality));
            final SyncState _tmpSyncState;
            _tmpSyncState = __SyncState_stringToEnum(_cursor.getString(_cursorIndexOfSyncState));
            final boolean _tmpVoided;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfVoided);
            _tmpVoided = _tmp != 0;
            _item = new ClockEventEntity(_tmpId,_tmpUserId,_tmpType,_tmpTimestampMs,_tmpModality,_tmpSyncState,_tmpVoided);
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

  @Override
  public Object getBetween(final String userId, final long fromMs, final long toMs,
      final Continuation<? super List<ClockEventEntity>> $completion) {
    final String _sql = "SELECT * FROM clock_events WHERE userId = ? AND timestampMs >= ? AND timestampMs < ? AND voided = 0 ORDER BY timestampMs ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindString(_argIndex, userId);
    _argIndex = 2;
    _statement.bindLong(_argIndex, fromMs);
    _argIndex = 3;
    _statement.bindLong(_argIndex, toMs);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ClockEventEntity>>() {
      @Override
      @NonNull
      public List<ClockEventEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfTimestampMs = CursorUtil.getColumnIndexOrThrow(_cursor, "timestampMs");
          final int _cursorIndexOfModality = CursorUtil.getColumnIndexOrThrow(_cursor, "modality");
          final int _cursorIndexOfSyncState = CursorUtil.getColumnIndexOrThrow(_cursor, "syncState");
          final int _cursorIndexOfVoided = CursorUtil.getColumnIndexOrThrow(_cursor, "voided");
          final List<ClockEventEntity> _result = new ArrayList<ClockEventEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ClockEventEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final ClockType _tmpType;
            _tmpType = __ClockType_stringToEnum(_cursor.getString(_cursorIndexOfType));
            final long _tmpTimestampMs;
            _tmpTimestampMs = _cursor.getLong(_cursorIndexOfTimestampMs);
            final Modality _tmpModality;
            _tmpModality = __Modality_stringToEnum(_cursor.getString(_cursorIndexOfModality));
            final SyncState _tmpSyncState;
            _tmpSyncState = __SyncState_stringToEnum(_cursor.getString(_cursorIndexOfSyncState));
            final boolean _tmpVoided;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfVoided);
            _tmpVoided = _tmp != 0;
            _item = new ClockEventEntity(_tmpId,_tmpUserId,_tmpType,_tmpTimestampMs,_tmpModality,_tmpSyncState,_tmpVoided);
            _result.add(_item);
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
  public Object pending(final Continuation<? super List<ClockEventEntity>> $completion) {
    final String _sql = "SELECT * FROM clock_events WHERE syncState = 'PENDING' ORDER BY timestampMs ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ClockEventEntity>>() {
      @Override
      @NonNull
      public List<ClockEventEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfTimestampMs = CursorUtil.getColumnIndexOrThrow(_cursor, "timestampMs");
          final int _cursorIndexOfModality = CursorUtil.getColumnIndexOrThrow(_cursor, "modality");
          final int _cursorIndexOfSyncState = CursorUtil.getColumnIndexOrThrow(_cursor, "syncState");
          final int _cursorIndexOfVoided = CursorUtil.getColumnIndexOrThrow(_cursor, "voided");
          final List<ClockEventEntity> _result = new ArrayList<ClockEventEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ClockEventEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final ClockType _tmpType;
            _tmpType = __ClockType_stringToEnum(_cursor.getString(_cursorIndexOfType));
            final long _tmpTimestampMs;
            _tmpTimestampMs = _cursor.getLong(_cursorIndexOfTimestampMs);
            final Modality _tmpModality;
            _tmpModality = __Modality_stringToEnum(_cursor.getString(_cursorIndexOfModality));
            final SyncState _tmpSyncState;
            _tmpSyncState = __SyncState_stringToEnum(_cursor.getString(_cursorIndexOfSyncState));
            final boolean _tmpVoided;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfVoided);
            _tmpVoided = _tmp != 0;
            _item = new ClockEventEntity(_tmpId,_tmpUserId,_tmpType,_tmpTimestampMs,_tmpModality,_tmpSyncState,_tmpVoided);
            _result.add(_item);
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
  public Flow<Integer> observePendingCount() {
    final String _sql = "SELECT COUNT(*) FROM clock_events WHERE syncState = 'PENDING' AND voided = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"clock_events"}, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
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

  private String __ClockType_enumToString(@NonNull final ClockType _value) {
    switch (_value) {
      case ENTRADA: return "ENTRADA";
      case INICIO_DESCANSO: return "INICIO_DESCANSO";
      case FIN_DESCANSO: return "FIN_DESCANSO";
      case SALIDA: return "SALIDA";
      default: throw new IllegalArgumentException("Can't convert enum to string, unknown enum value: " + _value);
    }
  }

  private String __Modality_enumToString(@NonNull final Modality _value) {
    switch (_value) {
      case PRESENCIAL: return "PRESENCIAL";
      case REMOTO: return "REMOTO";
      default: throw new IllegalArgumentException("Can't convert enum to string, unknown enum value: " + _value);
    }
  }

  private String __SyncState_enumToString(@NonNull final SyncState _value) {
    switch (_value) {
      case PENDING: return "PENDING";
      case SYNCED: return "SYNCED";
      case FLAGGED: return "FLAGGED";
      default: throw new IllegalArgumentException("Can't convert enum to string, unknown enum value: " + _value);
    }
  }

  private ClockType __ClockType_stringToEnum(@NonNull final String _value) {
    switch (_value) {
      case "ENTRADA": return ClockType.ENTRADA;
      case "INICIO_DESCANSO": return ClockType.INICIO_DESCANSO;
      case "FIN_DESCANSO": return ClockType.FIN_DESCANSO;
      case "SALIDA": return ClockType.SALIDA;
      default: throw new IllegalArgumentException("Can't convert value to enum, unknown value: " + _value);
    }
  }

  private Modality __Modality_stringToEnum(@NonNull final String _value) {
    switch (_value) {
      case "PRESENCIAL": return Modality.PRESENCIAL;
      case "REMOTO": return Modality.REMOTO;
      default: throw new IllegalArgumentException("Can't convert value to enum, unknown value: " + _value);
    }
  }

  private SyncState __SyncState_stringToEnum(@NonNull final String _value) {
    switch (_value) {
      case "PENDING": return SyncState.PENDING;
      case "SYNCED": return SyncState.SYNCED;
      case "FLAGGED": return SyncState.FLAGGED;
      default: throw new IllegalArgumentException("Can't convert value to enum, unknown value: " + _value);
    }
  }
}
