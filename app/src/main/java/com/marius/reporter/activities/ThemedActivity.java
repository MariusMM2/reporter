package com.marius.reporter.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import com.marius.reporter.Settings;

//An Activity with a day/night switch in the menu
public abstract class ThemedActivity extends AppCompatActivity {
    @SuppressWarnings("unused")
    private static final String TAG = "ThemedActivity";

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
}
