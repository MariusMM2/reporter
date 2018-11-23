package com.marius.reporter;

import android.content.Context;
import android.content.SharedPreferences;

public class Settings {
    private static Settings instance;
    public static Settings getInstance(Context context) {
        if (instance == null) instance = new Settings(context);
        return instance;
    }

    private static class Key {
        private static final String DARK_MODE = "darkMode";
    }

    public boolean darkMode;

    private Settings(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        darkMode = prefs.getBoolean(Key.DARK_MODE, false);
    }
    public void save(Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences("settings", Context.MODE_PRIVATE).edit();
        editor.putBoolean(Key.DARK_MODE, darkMode);

        editor.apply();
    }
}
