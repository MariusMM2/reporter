package com.marius.reporter.activities;

import android.content.Intent;
import android.support.v4.app.Fragment;
import com.marius.reporter.R;
import com.marius.reporter.Report;
import com.marius.reporter.fragments.ReportFragment;
import com.marius.reporter.fragments.ReportListFragment;

import java.util.UUID;

import static com.marius.reporter.activities.ReportPagerActivity.EXTRA_REPORT_ID;

public class ReportListActivity extends SingleFragmentActivity implements ReportListFragment.Callbacks, ReportFragment.Callbacks {
    @SuppressWarnings("unused")
    private static final String TAG = ReportListActivity.class.getSimpleName();

    private UUID mLastSelectedReportId;
    @Override
    protected Fragment createFragment() {
        return new ReportListFragment();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_masterdetail;
    }

    @Override
    protected Intent getCurrentState() {
        return super.getCurrentState().putExtra(EXTRA_REPORT_ID, mLastSelectedReportId);
    }

    @Override
    public void onReportSelected(Report report) {
        if (!report.getId().equals(mLastSelectedReportId)) {
            mLastSelectedReportId = report.getId();

            if (findViewById(R.id.detail_fragment_container) == null) {
                Intent intent = ReportPagerActivity.newIntent(this, report.getId());
                startActivity(intent);
            } else {
                Fragment newDetail = ReportFragment.newInstance(report.getId());

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.detail_fragment_container, newDetail)
                        .commit();
            }
        }
        themeTransitionDone();
    }

    @Override
    public void onReportDeleted(Report report) {
        if (report.getId().equals(mLastSelectedReportId)) {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.detail_fragment_container);
            if (fragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .remove(fragment)
                        .commit();
            }
        }
    }

    @Override
    public void onReportUpdated(Report report) {
        ReportListFragment listFragment = (ReportListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);
        listFragment.updateUI();
    }
}
