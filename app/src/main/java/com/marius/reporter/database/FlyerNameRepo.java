package com.marius.reporter.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.marius.reporter.database.flyername.FlyerNameBaseHelper;
import com.marius.reporter.database.flyername.FlyerNameCursorWrapper;
import com.marius.reporter.database.flyername.FlyerNameDbSchema.FlyerNameTable;

import java.util.ArrayList;
import java.util.List;

public class FlyerNameRepo {
    private static FlyerNameRepo instance;
    public static FlyerNameRepo getInstance(Context context) {
        if (instance == null) {
            instance = new FlyerNameRepo(context);
        }
        return instance;
    }

    private Context mContext;
    private SQLiteDatabase mDatabase;

    public FlyerNameRepo(Context context) {
        mContext = context.getApplicationContext();
        mDatabase = new FlyerNameBaseHelper(mContext).getWritableDatabase();
    }

    public void addFlyerName(String flyerName) {
        ContentValues values = getContentValues(flyerName);

        try (FlyerNameCursorWrapper cursor = queryFlyerNames(
                FlyerNameTable.Cols.VALUE + " = ?",
                new String[]{flyerName})
        ) {
            if (cursor.getCount() == 0) {
                mDatabase.insert(FlyerNameTable.NAME, null, values);
            }
        }
    }

    public String[] getFlyerNames() {
        List<String> flyerNames = new ArrayList<>();

        try (FlyerNameCursorWrapper cursor = queryFlyerNames(null, null)) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                flyerNames.add(cursor.getFlyerName());
                cursor.moveToNext();
            }
        }

        return flyerNames.toArray(new String[]{});
    }

    public void updateFlyerName(String flyerName) {
        ContentValues values = getContentValues(flyerName);

        mDatabase.update(FlyerNameTable.NAME, values,
                FlyerNameTable.Cols.VALUE + " = ?",
                new String[]{flyerName});
    }

    public void deleteFlyerName(String flyerName) {
        mDatabase.delete(FlyerNameTable.NAME,
                FlyerNameTable.Cols.VALUE + " = ?",
                new String[]{flyerName});
    }

    private FlyerNameCursorWrapper queryFlyerNames(String whereClause, String[] whereArgs) {
        Cursor cursor = mDatabase.query(
                FlyerNameTable.NAME,
                null, // columns - null selects all columns
                whereClause,
                whereArgs,
                null, //groupBy
                null, //having
                null // orderBy
        );
        return new FlyerNameCursorWrapper(cursor);
    }

    private static ContentValues getContentValues(String flyerName) {
        ContentValues values = new ContentValues();
        values.put(FlyerNameTable.Cols.VALUE, flyerName);
        return values;
    }
}
