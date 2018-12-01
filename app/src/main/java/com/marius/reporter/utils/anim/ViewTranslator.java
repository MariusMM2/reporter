package com.marius.reporter.utils.anim;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Point;
import android.util.Property;
import android.view.View;
import android.view.animation.*;
import com.marius.reporter.R;

public class ViewTranslator {

    public enum Direction {
        LEFT,
        RIGHT,
        UP,
        DOWN
    }

    private static int duration = -1;

    public static void moveToBehind(View mainView, View coverView) {
        moveToBehind(mainView, coverView, getDuration(mainView));
    }

    public static void moveToBehind(View mainView, View coverView, int duration) {
        final int[] mainViewPos = new int[2]; mainView.getLocationOnScreen(mainViewPos);
        final int[] coverViewPos = new int[2]; coverView.getLocationOnScreen(coverViewPos);
        final float mainViewCenterX = mainViewPos[1] + (float)mainView.getHeight()  / 2;
        final float coverViewCenterX = coverViewPos[1] + (float)coverView.getHeight()  / 2;

        if (mainViewCenterX == coverViewCenterX) return;

        ObjectAnimator hide = ObjectAnimator.ofFloat(mainView, View.TRANSLATION_Y, coverViewCenterX - mainViewCenterX);
        hide.setDuration((int)(duration*0.75));
        hide.setInterpolator(new AnticipateInterpolator());

        ObjectAnimator elevate = ViewElevator.elevate(coverView, R.dimen.fab_elevation_high, duration);
        ObjectAnimator rotate = ObjectAnimator.ofFloat(coverView, View.ROTATION, 360);
        rotate.setDuration(duration);
        rotate.setInterpolator(new AccelerateDecelerateInterpolator());

        AnimatorSet animations = new AnimatorSet();
        animations.play(hide).after(elevate);
        animations.play(hide).with(rotate);
        animations.start();
    }

    public static void moveFromBehind(View mainView, View coverView) {
        moveFromBehind(mainView, coverView, getDuration(mainView));
    }

    public static void moveFromBehind(View mainView, View coverView, int duration) {
        ObjectAnimator show = ObjectAnimator.ofFloat(mainView, View.TRANSLATION_Y, 0);
        show.setDuration(duration);
        show.setInterpolator(new OvershootInterpolator());

        ObjectAnimator elevate = ViewElevator.elevate(coverView, R.dimen.fab_elevation_low, duration);
        ObjectAnimator rotate = ObjectAnimator.ofFloat(coverView, "rotation", 0);
        rotate.setDuration(duration);
        rotate.setInterpolator(new AccelerateDecelerateInterpolator());

        AnimatorSet animations = new AnimatorSet();
        animations.play(show).before(elevate);
        animations.play(show).with(rotate);
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

    public static void slideInAndShow(View view, Direction direction) {
        slideInAndShow(view, direction, getDuration(view));
    }

    public static void slideInAndShow(View view, Direction direction, int duration) {
        Property translation = null;
        switch (direction) {
            case LEFT: case RIGHT:
                translation = View.TRANSLATION_X;
                break;
            case UP: case DOWN:
                translation = View.TRANSLATION_Y;
                break;
        }

        @SuppressWarnings("unchecked")
        ObjectAnimator slide = ObjectAnimator.ofFloat(view, translation, 0.0f);
        slide.setDuration(duration);
        slide.setInterpolator(new DecelerateInterpolator());

        ObjectAnimator fade = ObjectAnimator.ofFloat(view, View.ALPHA, 1);
        fade.setDuration(duration);

        AnimatorSet set = new AnimatorSet();
        set.play(slide).with(fade);
        set.start();
    }

    public static void slideOutAndHide(View view, Direction direction) {
        slideOutAndHide(view, direction, getDuration(view));
    }

    public static void slideOutAndHide(View view, Direction direction, int duration) {
        Point size = new Point();
        ((Activity)view.getContext()).getWindowManager().getDefaultDisplay().getSize(size);

        Property translation = null;
        float delta = 0;
        switch (direction) {
            case LEFT:
                translation = View.TRANSLATION_X;
                delta = -view.getRight();
                break;
            case RIGHT:
                translation = View.TRANSLATION_X;
                delta = size.x - view.getLeft();
                break;
            case UP:
                translation = View.TRANSLATION_Y;
                delta = -view.getBottom();
                break;
            case DOWN:
                translation = View.TRANSLATION_Y;
                delta = size.y - view.getTop();
                break;
        }

        @SuppressWarnings("unchecked")
        ObjectAnimator slide = ObjectAnimator.ofFloat(view, translation, delta);
        slide.setDuration(duration);
        slide.setInterpolator(new AccelerateInterpolator());

        ObjectAnimator fade = ObjectAnimator.ofFloat(view, View.ALPHA, 0.0f);
        fade.setDuration(duration);

        AnimatorSet set = new AnimatorSet();
        set.play(slide).with(fade);
        set.start();
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
