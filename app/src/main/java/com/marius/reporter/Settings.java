package com.marius.reporter;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.preference.PreferenceManager;

public class Settings {
    @SuppressWarnings("unused")
    private static final String TAG = "Settings";

    private static Settings instance;

    private static class Key {
        private static String NIGHT_MODE, GPS_NAME;
    }

    public synchronized static Settings getInstance(Context context) {
        if (instance == null) {
            instance = new Settings(context);
            Key.NIGHT_MODE = context.getString(R.string.pref_key_night_mode);
            Key.GPS_NAME = context.getString(R.string.pref_key_gps_name);
        }
        return instance;
    }

    private Callback mCallback;
    private SharedPreferences mPrefs;

    private Settings(Context context) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void refreshTheme() {
        boolean nightMode = isNightMode();

        AppCompatDelegate.setDefaultNightMode(nightMode ?
                AppCompatDelegate.MODE_NIGHT_YES :
                AppCompatDelegate.MODE_NIGHT_NO);

        if (mCallback != null) mCallback.onThemeChanged(nightMode);
    }

    public boolean isNightMode() {
        return mPrefs.getBoolean(Key.NIGHT_MODE, false);
    }

    public String getGpsName() {
        return mPrefs.getString(Key.GPS_NAME, "");
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public void removeCallback(Callback callback) {
        if (mCallback == callback) {
            mCallback = null;
        }
    }

    public interface Callback {
        void onThemeChanged(boolean isNight);
    }
}
