package com.marius.reporter.activities;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;
import com.mahfa.dnswitch.DayNightSwitch;
import com.mahfa.dnswitch.DayNightSwitchAnimListener;
import com.marius.reporter.R;
import com.marius.reporter.Settings;
import com.marius.reporter.fragments.ReportFragment;

public class ReportActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new ReportFragment();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.daynight_menu, menu);

        DayNightSwitch dayNightSwitch = menu.findItem(R.id.day_night_switch).getActionView().findViewById(R.id.switch_item);
        dayNightSwitch.setIsNight(Settings.getInstance(this).darkMode);
        dayNightSwitch.setAnimListener(new DayNightSwitchAnimListener() {
            @Override
            public void onAnimStart() {

            }

            @Override
            public void onAnimEnd() {
                Fragment frag = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                assert frag != null;
                frag.onStop();
                Intent intent = new Intent(frag.getActivity(), ReportActivity.class);

                startActivity(intent);
                finish();
            }

            @Override
            public void onAnimValueChanged(float v) {

            }
        });
        dayNightSwitch.setListener(isNight -> Settings.getInstance(this).darkMode = isNight);

        return true;
    }
}
