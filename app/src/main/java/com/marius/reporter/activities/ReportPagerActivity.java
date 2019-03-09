package com.marius.reporter.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import com.marius.reporter.R;
import com.marius.reporter.Report;
import com.marius.reporter.database.ReportRepo;
import com.marius.reporter.fragments.ReportFragment;

import java.util.List;
import java.util.UUID;

public class ReportPagerActivity extends ThemedSwitchActivity implements ReportFragment.Callbacks {
    public static final String EXTRA_REPORT_ID = "com.marius.reporter.activities.report_id";

    private ViewPager mViewPager;
    private List<Report> mReports;

    public static Intent newIntent(Context packageContext, UUID reportId) {
        Intent intent = new Intent(packageContext, ReportPagerActivity.class);
        intent.putExtra(EXTRA_REPORT_ID, reportId.toString());
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_report_pager);

        UUID reportId = UUID.fromString(getIntent().getStringExtra(EXTRA_REPORT_ID));

        mViewPager = findViewById(R.id.report_view_pager);
        mReports = ReportRepo.getInstance(this).getReports();

        mViewPager.setAdapter(new FragmentStatePagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int i) {
                Report report = mReports.get(i);
                return ReportFragment.newInstance(report.getId());
            }

            @Override
            public int getCount() {
                return mReports.size();
            }
        });

        for (int i = 0; i < mReports.size(); i++) {
            if (mReports.get(i).getId().equals(reportId)) {
                mViewPager.setCurrentItem(i);
                break;
            }
        }
    }

    @Override
    protected Intent getCurrentState() {
        return new Intent().putExtra(EXTRA_REPORT_ID, mReports.get(mViewPager.getCurrentItem()).getId().toString());
    }

    @Override
    public void onReportUpdated(Report report) {

    }
}
