package com.marius.reporter;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

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
    private List<Report> mReports;

    public ReportRepo(Context context) {
        mContext = context.getApplicationContext();
        mDatabase = null;
        mReports = new ArrayList<>();
        mReports.add(Report.dummy());
        mReports.add(Report.dummy());
        mReports.add(Report.dummy());
    }

    public void addReport(Report report) {
        mReports.add(report);
    }

    public List<Report> getReports() {
        return mReports;
    }

    public Report getReport(UUID id) {
        for (Report report : mReports) {
            if (report.getId().equals(id)) {
                return report;
            }
        }
        return null;
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean updateReport(Report report) {
        final int index = mReports.indexOf(getReport(report.getId()));
        if (index == -1) {
            return false;
        } else {
            mReports.set(index, report);
            return true;
        }
    }

    public Report deleteReport(int index) {
        return mReports.remove(index);
    }

    public void addReport(int index, Report report) {
        mReports.add(index, report);
    }
}
