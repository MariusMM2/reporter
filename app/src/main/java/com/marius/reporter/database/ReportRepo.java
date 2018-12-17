package com.marius.reporter.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.marius.reporter.Report;
import com.marius.reporter.database.report.ReportBaseHelper;
import com.marius.reporter.database.report.ReportCursorWrapper;
import com.marius.reporter.database.report.ReportDbSchema.ReportTable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ReportRepo {
    private static ReportRepo instance;
    public static ReportRepo getInstance(Context context) {
        if (instance == null) {
            instance = new ReportRepo(context);
        }
        return instance;
    }

    private Context mContext;
    private SQLiteDatabase mDatabase;
    private TimeRepo mTimeRepo;

    public ReportRepo(Context context) {
        mContext = context.getApplicationContext();
        mDatabase = new ReportBaseHelper(mContext).getWritableDatabase();
        mTimeRepo = TimeRepo.getInstance(context);
    }

    public void addReport(Report report) {
        ContentValues values = getContentValues(report);

        mDatabase.insert(ReportTable.NAME, null, values);

        mTimeRepo.addTimes(report);
    }

    public List<Report> getReports() {
        List<Report> reports = new ArrayList<>();

        ReportCursorWrapper cursor = queryReports(null, null);

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                reports.add(cursor.getReport());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }

        for (Report report : reports) {
            mTimeRepo.getTimes(report);
        }

        return reports;
    }

    public Report getReport(UUID id) {
        ReportCursorWrapper cursor = queryReports(
                ReportTable.Cols.UUID + " = ?",
                new String[]{id.toString()}
        );

        try {
            if (cursor.getCount() == 0) {
                return null;
            }

            cursor.moveToFirst();
            Report report = cursor.getReport();
            mTimeRepo.getTimes(report);
            return report;
        } finally {
            cursor.close();
        }
    }

    public void updateReport(Report report) {
        mTimeRepo.addTimes(report);

        String uuidString = report.getId().toString();
        ContentValues values = getContentValues(report);

        mDatabase.update(ReportTable.NAME, values,
                ReportTable.Cols.UUID + " = ?",
                new String[]{uuidString});
    }

    public void deleteReport(UUID id) {
        mTimeRepo.deleteTimes(getReport(id));

        String uuidString = id.toString();
        mDatabase.delete(ReportTable.NAME,
                ReportTable.Cols.UUID + " = ?",
                new String[]{uuidString});
    }

    private static ContentValues getContentValues(Report report) {
        ContentValues values = new ContentValues();
        values.put(ReportTable.Cols.UUID, report.getId().toString());
        values.put(ReportTable.Cols.FLYER_NAME, report.getFlyerName());
        values.put(ReportTable.Cols.REMAINING_FLYERS, report.getRemainingFlyers());
        values.put(ReportTable.Cols.WITH_REMAINING_FLYERS, report.isWithRemainingFlyers());
        values.put(ReportTable.Cols.GPS_NAME, report.getGPSName());

        return values;
    }

    private ReportCursorWrapper queryReports(String whereClause, String[] whereArgs) {
        Cursor cursor = mDatabase.query(
                ReportTable.NAME,
                null, // columns - null selects all columns
                whereClause,
                whereArgs,
                null, //groupBy
                null, //having
                null // orderBy
        );

        return new ReportCursorWrapper(cursor);
    }
}
