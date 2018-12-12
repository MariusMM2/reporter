package com.marius.reporter.database.flyername;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.marius.reporter.database.flyername.FlyerNameDbSchema.FlyerNameTable;

public class FlyerNameBaseHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "flyerNameBase.db";

    public FlyerNameBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + FlyerNameTable.NAME + "(" +
                "_id integer primary key autoincrement, " +
                FlyerNameTable.Cols.VALUE +
                ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
