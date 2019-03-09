package com.marius.reporter.activities;

import android.content.Intent;
import android.support.v4.app.Fragment;
import com.marius.reporter.R;
import com.marius.reporter.Report;
import com.marius.reporter.fragments.ReportFragment;
import com.marius.reporter.fragments.ReportListFragment;

public class ReportListActivity extends SingleFragmentActivity implements ReportListFragment.Callbacks, ReportFragment.Callbacks {
    @SuppressWarnings("unused")
    private static final String TAG = ReportListActivity.class.getSimpleName();

    @Override
    protected Fragment createFragment() {
        return new ReportListFragment();
    }

    @Override
    public void onReportSelected(Report report) {
        Intent intent = ReportPagerActivity.newIntent(this, report.getId());
        startActivity(intent);
    }

    @Override
    public void onReportUpdated(Report report) {
        ReportListFragment listFragment = (ReportListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);
        listFragment.updateUI();
    }
}
