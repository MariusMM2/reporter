package com.marius.reporter.utils.anim;

import android.view.View;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.OvershootInterpolator;
import com.marius.reporter.R;

public class ViewTranslator {
    public static int duration = 500;
    public static void moveToBehind(View mainView, View coverView) {
        mainView.animate().translationY((float) (coverView.getTop() - mainView.getTop()))
                .setDuration(duration)
                .setInterpolator(new AnticipateInterpolator())
                .start();
        ViewElevator.elevate(coverView, R.dimen.item_time_selected);
    }

    public static void moveFromBehind(View mainView, View coverView) {
        mainView.animate().translationY(0)
                .setDuration(duration)
                .setInterpolator(new OvershootInterpolator())
                .start();
        ViewElevator.elevate(coverView, R.dimen.item_time_normal);
    }
}
