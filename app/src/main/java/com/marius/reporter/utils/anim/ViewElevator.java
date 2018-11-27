package com.marius.reporter.utils.anim;

import android.animation.ObjectAnimator;
import android.support.annotation.DimenRes;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.animation.LinearInterpolator;

public class ViewElevator {
    public static int duration = 250;

    public static ObjectAnimator elevate(@NonNull View view, @DimenRes int elevation) {
        return elevate(view, elevation, duration);
    }

    public static ObjectAnimator elevate(@NonNull View view, @DimenRes int elevation, int duration) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "elevation", view.getResources().getDimension(elevation));
        animator.setDuration(duration);
        animator.setInterpolator(new LinearInterpolator());
        return animator;
    }
}
