package com.marius.reporter.utils.anim;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.view.View;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.OvershootInterpolator;
import com.marius.reporter.R;

public class ViewTranslator {

    public enum Direction {
        LEFT,
        RIGHT,
        UP,
        DOWN
    }

    public static int duration = 500;

    public static void moveToBehind(View mainView, View converView) {
        moveToBehind(mainView, converView, duration);
    }

    public static void moveToBehind(View mainView, View coverView, int duration) {
        final int[] mainViewPos = new int[2]; mainView.getLocationOnScreen(mainViewPos);
        final int[] coverViewPos = new int[2]; coverView.getLocationOnScreen(coverViewPos);
        final float mainViewCenterX = mainViewPos[1] + (float)mainView.getWidth()  / 2;
        final float coverViewCenterX = coverViewPos[1] + (float)coverView.getWidth()  / 2;

        if (mainViewCenterX == coverViewCenterX) return;

        ObjectAnimator hide = ObjectAnimator.ofFloat(mainView, "translationY", coverViewCenterX - mainViewCenterX);
        hide.setDuration((int)(duration*0.66));
        hide.setInterpolator(new AnticipateInterpolator());

        ObjectAnimator elevate = ViewElevator.elevate(coverView, R.dimen.view_elevated, duration);
        ObjectAnimator rotate = ObjectAnimator.ofFloat(coverView, "rotation", 90);
        rotate.setDuration(duration);
        rotate.setInterpolator(new BounceInterpolator());

        AnimatorSet animations = new AnimatorSet();
        animations.play(hide).after(elevate);
        animations.play(elevate).with(rotate);
        animations.start();
    }

    public static void moveFromBehind(View mainView, View coverView) {
        moveFromBehind(mainView, coverView, duration/2);
    }

    public static void moveFromBehind(View mainView, View coverView, int duration) {
        ObjectAnimator show = ObjectAnimator.ofFloat(mainView, "translationY", 0);
        show.setDuration((int)(duration*0.66));
        show.setInterpolator(new OvershootInterpolator());

        ObjectAnimator elevate = ViewElevator.elevate(coverView, R.dimen.view_normal, duration);
        ObjectAnimator rotate = ObjectAnimator.ofFloat(coverView, "rotation", 0);
        rotate.setDuration(duration);
        rotate.setInterpolator(new BounceInterpolator());

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
        hide.setDuration(duration);
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
}
