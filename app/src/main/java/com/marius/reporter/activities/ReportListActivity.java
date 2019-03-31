package com.marius.reporter.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;
import android.view.MenuItem;
import android.widget.Toast;
import com.marius.reporter.R;
import com.marius.reporter.fragments.ReportFragment;
import com.marius.reporter.fragments.ReportListFragment;
import com.marius.reporter.models.Report;

import java.util.UUID;

public class ReportListActivity extends SingleFragmentActivity implements ReportListFragment.Callbacks, ReportFragment.Callbacks {
    @SuppressWarnings("unused")
    private static final String TAG = "ReportListActivity";

    private static UUID mQueuedReportId;

    @Override
    protected Fragment createFragment() {
        return new ReportListFragment();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean switchPref = sharedPref.getBoolean(SettingsActivity.KEY_PREF_EXAMPLE_SWITCH, false);
        Toast.makeText(this, Boolean.toString(switchPref), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onReportSelected(Report report) {
        if (!hasReportQueued()) {
            mQueuedReportId = report.getId();
            Intent intent = ReportPagerActivity.newIntent(this, report.getId());
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
        }
    }

    @Override
    public boolean hasReportQueued() {
        return mQueuedReportId != null;
    }

    @Override
    public void onReportUpdated(Report report) {
        ReportListFragment listFragment = (ReportListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);
        assert listFragment != null;
        listFragment.updateUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mQueuedReportId = null;
    }

    public void openSettings(MenuItem item) {
        Intent i = new Intent(this, SettingsActivity.class);
        startActivity(i);
    }
}
