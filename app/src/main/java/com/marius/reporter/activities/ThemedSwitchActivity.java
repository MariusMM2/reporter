package com.marius.reporter.activities;

import android.annotation.SuppressLint;
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

//A template Activity with a day/night switch in the menu
@SuppressLint("Registered")
public class ThemedSwitchActivity extends AppCompatActivity {
    @SuppressWarnings("unused")
    private static final String TAG = ThemedSwitchActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(Settings.getInstance(this).darkMode ? R.style.AppNightTheme : R.style.AppDayTheme);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Settings.getInstance(this).save(this);
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
                Intent intent = new Intent(ThemedSwitchActivity.this, ThemedSwitchActivity.this.getClass());
                intent.putExtras(getCurrentState());

                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

                finish();
            }

            @Override
            public void onAnimValueChanged(float v) {

            }
        });
        dayNightSwitch.setListener(isNight -> Settings.getInstance(this).darkMode = isNight);

        return true;
    }

    protected Intent getCurrentState() {
        return getIntent();
    }
}
