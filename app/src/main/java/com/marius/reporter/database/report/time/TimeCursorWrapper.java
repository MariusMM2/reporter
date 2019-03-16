package com.marius.reporter.database.report.time;

import android.database.Cursor;
import android.database.CursorWrapper;
import com.marius.reporter.models.Time;

public class TimeCursorWrapper extends CursorWrapper {

    public TimeCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Time getTime() {
        short hours = getShort(getColumnIndex(TimeDbSchema.TimeTable.Cols.HOUR));
        short minutes = getShort(getColumnIndex(TimeDbSchema.TimeTable.Cols.MINUTE));
        short seconds = getShort(getColumnIndex(TimeDbSchema.TimeTable.Cols.SECOND));

        Time time = new Time();
        time.setHours(hours);
        time.setMinutes(minutes);
        time.setSeconds(seconds);

        return time;
    }
}
