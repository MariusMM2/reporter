package com.marius.reporter.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.marius.reporter.Report;
import com.marius.reporter.Time;
import com.marius.reporter.database.report.time.TimeBaseHelper;
import com.marius.reporter.database.report.time.TimeCursorWrapper;
import com.marius.reporter.database.report.time.TimeDbSchema;
import com.marius.reporter.database.report.time.TimeDbSchema.TimeTable;

class TimeRepo {
    private static TimeRepo instance;

    static TimeRepo getInstance(Context context) {
        if (instance == null) {
            instance = new TimeRepo(context);
        }
        return instance;
    }

    private SQLiteDatabase mDatabase;

    private TimeRepo(Context context) {
        mDatabase = new TimeBaseHelper(context.getApplicationContext()).getWritableDatabase();
    }

    void addTimes(Report report) {
        //TODO: improve addition and updating efficiency
        deleteTimes(report);

        for (Time time : report.getTimes()) {
            ContentValues values = getContentValues(time, report);

            mDatabase.insert(TimeTable.NAME, null, values);
        }
    }

    void getTimes(Report report) {
        String uuidString = report.getId().toString();

        try (TimeCursorWrapper cursor = queryTimes(
                TimeTable.Cols.REPORT_UUID + " = ?",
                new String[]{uuidString}
        )) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                report.add(cursor.getTime());
                cursor.moveToNext();
            }
        }
    }

    void deleteTimes(Report report) {
        String uuidString = report.getId().toString();
        mDatabase.delete(TimeTable.NAME,
                TimeTable.Cols.REPORT_UUID + " = ?",
                new String[]{uuidString});
    }

    private static ContentValues getContentValues(Time time, Report report) {
        ContentValues values = new ContentValues();
        values.put(TimeTable.Cols.REPORT_UUID, report.getId().toString());
        values.put(TimeTable.Cols.HOUR, time.getHours());
        values.put(TimeTable.Cols.MINUTE, time.getMinutes());
        values.put(TimeTable.Cols.SECOND, time.getSeconds());

        return values;
    }

    @SuppressWarnings("SameParameterValue")
    private TimeCursorWrapper queryTimes(String whereClause, String[] whereArgs) {
        @SuppressLint("Recycle") Cursor cursor = mDatabase.query(
                TimeDbSchema.TimeTable.NAME,
                null, // columns - null selects all columns
                whereClause,
                whereArgs,
                null, //groupBy
                null, //having
                null // orderBy
        );

        return new TimeCursorWrapper(cursor);
    }
}
