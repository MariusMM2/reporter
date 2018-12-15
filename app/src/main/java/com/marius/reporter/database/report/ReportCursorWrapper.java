package com.marius.reporter.database.report;

import android.database.Cursor;
import android.database.CursorWrapper;
import com.marius.reporter.Report;

import java.util.UUID;

public class ReportCursorWrapper extends CursorWrapper {

    public ReportCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Report getReport() {
        String uuidString = getString(getColumnIndex(ReportDbSchema.ReportTable.Cols.UUID));
        String flyerName = getString(getColumnIndex(ReportDbSchema.ReportTable.Cols.FLYER_NAME));
        short remainingFlyers = getShort(getColumnIndex(ReportDbSchema.ReportTable.Cols.REMAINING_FLYERS));
        boolean withRemainingFlyers = getBoolean(getColumnIndex(ReportDbSchema.ReportTable.Cols.WITH_REMAINING_FLYERS));
        String gpsName = getString(getColumnIndex(ReportDbSchema.ReportTable.Cols.GPS_NAME));

        Report report = new Report(UUID.fromString(uuidString));
        report.setFlyerName(flyerName);
        report.setRemainingFlyers(remainingFlyers);
        report.setWithRemainingFlyers(withRemainingFlyers);
        report.setGPSName(gpsName);

        return report;
    }

    private boolean getBoolean(int columnIndex) {
        return !isNull(columnIndex) && getShort(columnIndex) != 0;
    }
}
