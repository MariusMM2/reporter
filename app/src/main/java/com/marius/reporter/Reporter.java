package com.marius.reporter;

import android.app.Application;

public class Reporter extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Settings.getInstance(this).refreshTheme();
    }
}
