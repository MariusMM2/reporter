package com.marius.reporter.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import com.marius.reporter.Settings;
import com.marius.reporter.fragments.SettingsFragment;

public class SettingsActivity extends ThemedActivity implements Settings.Callback {
    @SuppressWarnings("unused")
    private static final String TAG = "SettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Settings.getInstance(this).setCallback(this);

        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    @Override
    protected void onDestroy() {
        Settings.getInstance(this).removeCallback(this);
        super.onDestroy();
    }

    @Override
    public void onThemeChanged(boolean isNight) {
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.putExtras(getIntent());
        Log.v(TAG, "Refreshing Activity");

        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
