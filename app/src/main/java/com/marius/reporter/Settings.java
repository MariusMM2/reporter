package com.marius.reporter;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Map;

public class Settings {
    private static final String TAG = Settings.class.getSimpleName();
    private static Settings instance;

    public synchronized static Settings getInstance(Context context) {
        if (instance == null) {
            instance = new Settings(context);
        }
        return instance;
    }

    private static class Key {
        private static final String DARK_MODE = "darkMode";
        private static final String GPS_NAME = "gpsName";
    }

    public boolean darkMode;
    public String gpsName;

    private Settings(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        darkMode = prefs.getBoolean(Key.DARK_MODE, false);
        gpsName  = prefs.getString(Key.GPS_NAME, "");

        logPrefs(prefs, "Loaded");
    }

    public synchronized void save(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(Key.DARK_MODE, darkMode);
        editor.putString(Key.GPS_NAME, gpsName);

        editor.apply();

        logPrefs(prefs, "Saved");
    }

    private void logPrefs(SharedPreferences prefs, String s) {
        Map<String, ?> prefsAll = prefs.getAll();
        for (String key : prefsAll.keySet()) {
            Log.i(TAG, String.format(s + " preference: %s = %s", key, prefsAll.get(key)));
        }
    }
}
