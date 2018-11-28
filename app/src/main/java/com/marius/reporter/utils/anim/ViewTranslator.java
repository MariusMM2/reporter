package com.marius.reporter.utils.anim;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.view.View;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import com.marius.reporter.R;

public class ViewTranslator {

    public enum Direction {
        LEFT,
        RIGHT,
        UP,
        DOWN
    }

    private static int duration = -1;

    public static void moveToBehind(View mainView, View converView) {
        moveToBehind(mainView, converView, getDuration(mainView));
    }

    public static void moveToBehind(View mainView, View coverView, int duration) {
        final int[] mainViewPos = new int[2]; mainView.getLocationOnScreen(mainViewPos);
        final int[] coverViewPos = new int[2]; coverView.getLocationOnScreen(coverViewPos);
        final float mainViewCenterX = mainViewPos[1] + (float)mainView.getHeight()  / 2;
        final float coverViewCenterX = coverViewPos[1] + (float)coverView.getHeight()  / 2;
        System.out.println(mainView.getX());
        System.out.println(coverView.getX());

        if (mainViewCenterX == coverViewCenterX) return;

        ObjectAnimator hide = ObjectAnimator.ofFloat(mainView, "translationY", coverViewCenterX - mainViewCenterX);
        hide.setDuration((int)(duration*0.75));
        hide.setInterpolator(new AnticipateInterpolator());

        ObjectAnimator elevate = ViewElevator.elevate(coverView, R.dimen.view_elevated, duration);
        ObjectAnimator rotate = ObjectAnimator.ofFloat(coverView, "rotation", 360);
        rotate.setDuration(duration);
        rotate.setInterpolator(new DecelerateInterpolator());

        AnimatorSet animations = new AnimatorSet();
        animations.play(hide).after(elevate);
        animations.play(elevate).with(rotate);
        animations.start();
    }

    public static void moveFromBehind(View mainView, View coverView) {
        moveFromBehind(mainView, coverView, getDuration(mainView)/2);
    }

    public static void moveFromBehind(View mainView, View coverView, int duration) {
        ObjectAnimator show = ObjectAnimator.ofFloat(mainView, "translationY", 0);
        show.setDuration(duration);
        show.setInterpolator(new OvershootInterpolator());

        ObjectAnimator elevate = ViewElevator.elevate(coverView, R.dimen.view_normal, duration);
        ObjectAnimator rotate = ObjectAnimator.ofFloat(coverView, "rotation", 0);
        rotate.setDuration(duration);
        rotate.setInterpolator(new DecelerateInterpolator());

        AnimatorSet animations = new AnimatorSet();
        animations.play(show).before(elevate);
        animations.play(elevate).with(rotate);
        animations.start();
    }

    public static void moveOffscreen(View view, Direction direction, Runnable endAction) {
        float value = 1000;
        switch (direction) {
            case LEFT: case UP:
                value *= -1;
                break;
        }

        @SuppressLint("ObjectAnimatorBinding")
        ObjectAnimator hide  = ObjectAnimator.ofFloat(view, getDirection(direction), value);
        hide.setDuration(getDuration(view));
        hide.setInterpolator(new AnticipateInterpolator());

        AnimatorSet animations = new AnimatorSet();
        animations.play(moveOffscreenReset(view, direction))
                .after(hide);
        animations.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (endAction != null) {
                    endAction.run();
                }
            }
        });
        animations.start();
    }

    private static ObjectAnimator moveOffscreenReset(View view, Direction direction) {

        ObjectAnimator reveal = ObjectAnimator.ofFloat(view, getDirection(direction), 0);
        reveal.setDuration(duration);
        reveal.setInterpolator(new OvershootInterpolator());
        return reveal;
    }

    private static String getDirection(Direction direction) {
        String translation = "translation";
        switch(direction) {
            case LEFT: case RIGHT:
                translation += "X";
                break;
            case UP: case DOWN:
                translation += "Y";
                break;
        }
        return translation;
    }

    private static int getDuration(View context) {
        if (duration == -1) {
            duration = context.getResources().getInteger(android.R.integer.config_mediumAnimTime);
        }

        return duration;
    }
}
