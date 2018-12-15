package com.marius.reporter.database.report;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.marius.reporter.database.report.ReportDbSchema.ReportTable;

public class ReportBaseHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "reportBase.db";

    public ReportBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + ReportTable.NAME + "(" +
                "_id integer primary key autoincrement, " +
                ReportTable.Cols.UUID + ", " +
                ReportTable.Cols.FLYER_NAME + ", " +
                ReportTable.Cols.REMAINING_FLYERS + ", " +
                ReportTable.Cols.WITH_REMAINING_FLYERS + ", " +
                ReportTable.Cols.GPS_NAME +
                ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
