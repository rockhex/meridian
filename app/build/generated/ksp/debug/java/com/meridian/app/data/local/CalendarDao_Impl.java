package com.meridian.app.data.local;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
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
public final class CalendarDao_Impl implements CalendarDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<CalendarSlotEntity> __insertionAdapterOfCalendarSlotEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteFor;

  public CalendarDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfCalendarSlotEntity = new EntityInsertionAdapter<CalendarSlotEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `calendar_slots` (`id`,`userId`,`startMs`,`endMs`) VALUES (?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final CalendarSlotEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getUserId());
        statement.bindLong(3, entity.getStartMs());
        statement.bindLong(4, entity.getEndMs());
      }
    };
    this.__preparedStmtOfDeleteFor = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM calendar_slots WHERE userId = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertAll(final List<CalendarSlotEntity> slots,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfCalendarSlotEntity.insert(slots);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteFor(final String userId, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteFor.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, userId);
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
          __preparedStmtOfDeleteFor.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<CalendarSlotEntity>> observe(final String userId, final long fromMs,
      final long toMs) {
    final String _sql = "SELECT * FROM calendar_slots WHERE userId = ? AND endMs > ? AND startMs < ? ORDER BY startMs ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindString(_argIndex, userId);
    _argIndex = 2;
    _statement.bindLong(_argIndex, fromMs);
    _argIndex = 3;
    _statement.bindLong(_argIndex, toMs);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"calendar_slots"}, new Callable<List<CalendarSlotEntity>>() {
      @Override
      @NonNull
      public List<CalendarSlotEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfStartMs = CursorUtil.getColumnIndexOrThrow(_cursor, "startMs");
          final int _cursorIndexOfEndMs = CursorUtil.getColumnIndexOrThrow(_cursor, "endMs");
          final List<CalendarSlotEntity> _result = new ArrayList<CalendarSlotEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CalendarSlotEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final long _tmpStartMs;
            _tmpStartMs = _cursor.getLong(_cursorIndexOfStartMs);
            final long _tmpEndMs;
            _tmpEndMs = _cursor.getLong(_cursorIndexOfEndMs);
            _item = new CalendarSlotEntity(_tmpId,_tmpUserId,_tmpStartMs,_tmpEndMs);
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
}
