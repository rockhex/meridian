package com.meridian.app.data.local;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
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
import com.meridian.app.domain.model.SyncState;
import java.lang.Class;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
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
public final class OfficePlanDao_Impl implements OfficePlanDao {
  private final RoomDatabase __db;

  private final SharedSQLiteStatement __preparedStmtOfMarkSynced;

  private final EntityUpsertionAdapter<OfficePlanEntity> __upsertionAdapterOfOfficePlanEntity;

  public OfficePlanDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__preparedStmtOfMarkSynced = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE office_plan SET syncState = 'SYNCED' WHERE userId = ? AND date = ? AND going = ?";
        return _query;
      }
    };
    this.__upsertionAdapterOfOfficePlanEntity = new EntityUpsertionAdapter<OfficePlanEntity>(new EntityInsertionAdapter<OfficePlanEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT INTO `office_plan` (`userId`,`date`,`going`,`syncState`) VALUES (?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final OfficePlanEntity entity) {
        statement.bindString(1, entity.getUserId());
        statement.bindString(2, entity.getDate());
        final int _tmp = entity.getGoing() ? 1 : 0;
        statement.bindLong(3, _tmp);
        statement.bindString(4, __SyncState_enumToString(entity.getSyncState()));
      }
    }, new EntityDeletionOrUpdateAdapter<OfficePlanEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE `office_plan` SET `userId` = ?,`date` = ?,`going` = ?,`syncState` = ? WHERE `userId` = ? AND `date` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final OfficePlanEntity entity) {
        statement.bindString(1, entity.getUserId());
        statement.bindString(2, entity.getDate());
        final int _tmp = entity.getGoing() ? 1 : 0;
        statement.bindLong(3, _tmp);
        statement.bindString(4, __SyncState_enumToString(entity.getSyncState()));
        statement.bindString(5, entity.getUserId());
        statement.bindString(6, entity.getDate());
      }
    });
  }

  @Override
  public Object markSynced(final String userId, final String date, final boolean going,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfMarkSynced.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, userId);
        _argIndex = 2;
        _stmt.bindString(_argIndex, date);
        _argIndex = 3;
        final int _tmp = going ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp);
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
          __preparedStmtOfMarkSynced.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object upsert(final OfficePlanEntity entry, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __upsertionAdapterOfOfficePlanEntity.upsert(entry);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object upsertAll(final List<OfficePlanEntity> entries,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __upsertionAdapterOfOfficePlanEntity.upsert(entries);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<OfficePlanEntity>> observeRange(final String from, final String to) {
    final String _sql = "SELECT * FROM office_plan WHERE date BETWEEN ? AND ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, from);
    _argIndex = 2;
    _statement.bindString(_argIndex, to);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"office_plan"}, new Callable<List<OfficePlanEntity>>() {
      @Override
      @NonNull
      public List<OfficePlanEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfGoing = CursorUtil.getColumnIndexOrThrow(_cursor, "going");
          final int _cursorIndexOfSyncState = CursorUtil.getColumnIndexOrThrow(_cursor, "syncState");
          final List<OfficePlanEntity> _result = new ArrayList<OfficePlanEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final OfficePlanEntity _item;
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final String _tmpDate;
            _tmpDate = _cursor.getString(_cursorIndexOfDate);
            final boolean _tmpGoing;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfGoing);
            _tmpGoing = _tmp != 0;
            final SyncState _tmpSyncState;
            _tmpSyncState = __SyncState_stringToEnum(_cursor.getString(_cursorIndexOfSyncState));
            _item = new OfficePlanEntity(_tmpUserId,_tmpDate,_tmpGoing,_tmpSyncState);
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
  public Object pending(final String userId,
      final Continuation<? super List<OfficePlanEntity>> $completion) {
    final String _sql = "SELECT * FROM office_plan WHERE userId = ? AND syncState = 'PENDING'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, userId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<OfficePlanEntity>>() {
      @Override
      @NonNull
      public List<OfficePlanEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfGoing = CursorUtil.getColumnIndexOrThrow(_cursor, "going");
          final int _cursorIndexOfSyncState = CursorUtil.getColumnIndexOrThrow(_cursor, "syncState");
          final List<OfficePlanEntity> _result = new ArrayList<OfficePlanEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final OfficePlanEntity _item;
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final String _tmpDate;
            _tmpDate = _cursor.getString(_cursorIndexOfDate);
            final boolean _tmpGoing;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfGoing);
            _tmpGoing = _tmp != 0;
            final SyncState _tmpSyncState;
            _tmpSyncState = __SyncState_stringToEnum(_cursor.getString(_cursorIndexOfSyncState));
            _item = new OfficePlanEntity(_tmpUserId,_tmpDate,_tmpGoing,_tmpSyncState);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }

  private String __SyncState_enumToString(@NonNull final SyncState _value) {
    switch (_value) {
      case PENDING: return "PENDING";
      case SYNCED: return "SYNCED";
      case FLAGGED: return "FLAGGED";
      default: throw new IllegalArgumentException("Can't convert enum to string, unknown enum value: " + _value);
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
