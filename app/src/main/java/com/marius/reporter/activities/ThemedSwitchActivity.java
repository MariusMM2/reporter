package com.marius.reporter.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import com.mahfa.dnswitch.DayNightSwitch;
import com.mahfa.dnswitch.DayNightSwitchAnimListener;
import com.marius.reporter.R;
import com.marius.reporter.Settings;

//An Activity with a day/night switch in the menu
public abstract class ThemedSwitchActivity extends AppCompatActivity {
    @SuppressWarnings("unused")
    private static final String TAG = ThemedSwitchActivity.class.getSimpleName();

    private boolean nightMode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nightMode = Settings.getInstance(this).nightMode;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Settings.getInstance(this).save(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nightMode != Settings.getInstance(this).nightMode) {
            recreate();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.daynight_menu, menu);

        DayNightSwitch dayNightSwitch = menu.findItem(R.id.day_night_switch).getActionView().findViewById(R.id.switch_item);
        dayNightSwitch.setIsNight(Settings.getInstance(this).nightMode);
        dayNightSwitch.setAnimListener(new DayNightSwitchAnimListener() {
            @Override
            public void onAnimStart() {

            }

            @Override
            public void onAnimEnd() {
                Intent intent = new Intent(ThemedSwitchActivity.this, ThemedSwitchActivity.this.getClass());
                intent.putExtras(getIntent());

                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

                finish();
            }

            @Override
            public void onAnimValueChanged(float v) {

            }
        });
        dayNightSwitch.setListener(isNight -> {
            Settings.getInstance(this).nightMode = isNight;
            Settings.getInstance(this).refreshTheme();
        });

        return true;
    }
}
