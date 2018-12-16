package com.marius.reporter.database.report.time;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.marius.reporter.database.report.time.TimeDbSchema.TimeTable;

public class TimeBaseHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "timeBase.db";

    public TimeBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TimeTable.NAME + "(" +
                "_id integer primary key autoincrement, " +
                TimeTable.Cols.REPORT_UUID + ", " +
                TimeTable.Cols.HOUR + ", " +
                TimeTable.Cols.MINUTE + ", " +
                TimeTable.Cols.SECOND +
                ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
