package com.marius.reporter.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.marius.reporter.database.report.time.TimeBaseHelper;
import com.marius.reporter.database.report.time.TimeCursorWrapper;
import com.marius.reporter.database.report.time.TimeDbSchema;
import com.marius.reporter.database.report.time.TimeDbSchema.TimeTable;
import com.marius.reporter.models.Report;
import com.marius.reporter.models.Time;

import java.util.Arrays;

class TimeRepo {
    private static final String TAG = TimeRepo.class.getSimpleName();

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
        String[] whereArgs = {uuidString};
        Log.d(TAG, String.format("whereArgs: %s", Arrays.toString(whereArgs)));

        int result = mDatabase.delete(TimeTable.NAME,
                TimeTable.Cols.REPORT_UUID + " = ?",
                whereArgs);
        if (result != 0)
            Log.i(TAG, String.format("Deleted %d Times for %s", result, report.toString()));
        else
            Log.w(TAG, String.format("No Time records found for %s", report.toString()));
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
