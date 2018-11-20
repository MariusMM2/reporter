package com.marius.reporter;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.marius.reporter.activities.SingleFragmentActivity;
import com.marius.reporter.fragments.ReportFragment;

public class ReportActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new ReportFragment();
    }
}
