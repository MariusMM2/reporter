package com.marius.reporter.activities;

import android.support.v4.app.Fragment;
import com.marius.reporter.fragments.ReportFragment;

public class ReportActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new ReportFragment();
    }
}
