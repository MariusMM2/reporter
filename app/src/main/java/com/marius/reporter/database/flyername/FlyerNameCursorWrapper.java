package com.marius.reporter.database.flyername;

import android.database.Cursor;
import android.database.CursorWrapper;
import com.marius.reporter.database.flyername.FlyerNameDbSchema.FlyerNameTable;

public class FlyerNameCursorWrapper extends CursorWrapper {

    public FlyerNameCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public String getFlyerName() {
        return getString(getColumnIndex(FlyerNameTable.Cols.VALUE));
    }
}
