package com.marius.reporter.utils.anim;

import android.animation.ValueAnimator;
import android.support.annotation.DimenRes;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.animation.OvershootInterpolator;

public class ViewElevator {
    public static int duration = 1000;

    public static void elevate(@NonNull View view, @DimenRes int elevation) {
        elevate(view, elevation, duration);
    }

    public static void elevate(@NonNull View view, @DimenRes int elevation, int duration) {
        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.setInterpolator(new OvershootInterpolator());
        valueAnimator.setFloatValues(view.getElevation(), view.getResources().getDimension(elevation));
        valueAnimator.setDuration(duration);
        valueAnimator.addUpdateListener(animation -> view.setElevation((float) animation.getAnimatedValue()));
        valueAnimator.start();
    }
}
