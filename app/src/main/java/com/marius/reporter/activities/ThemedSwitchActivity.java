package com.marius.reporter.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
                Fragment frag = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                assert frag != null;
                frag.onStop();
                Intent intent = new Intent(frag.getActivity(), ThemedSwitchActivity.this.getClass());

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