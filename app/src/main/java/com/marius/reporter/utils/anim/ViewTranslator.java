package com.marius.reporter.utils.anim;

import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.AnticipateInterpolator;
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
    public static void moveToBehind(View mainView, View coverView) {
        final int[] mainViewPos = new int[2]; mainView.getLocationOnScreen(mainViewPos);
        final int[] coverViewPos = new int[2]; coverView.getLocationOnScreen(coverViewPos);
        final float mainViewCenterX = mainViewPos[1] + (float)mainView.getWidth()  / 2;
        final float coverViewCenterX = coverViewPos[1] + (float)coverView.getWidth()  / 2;
        if (mainViewCenterX != coverViewCenterX) {
            mainView.animate().translationY((coverViewCenterX - mainViewCenterX))
                    .setDuration(duration)
                    .setInterpolator(new AnticipateInterpolator())
                    .start();
            ViewElevator.elevate(coverView, R.dimen.item_time_selected);
        }
    }

    public static void moveFromBehind(View mainView, View coverView) {
        mainView.animate().translationY(0)
                .setDuration(duration)
                .setInterpolator(new OvershootInterpolator())
                .start();
        ViewElevator.elevate(coverView, R.dimen.item_time_normal);
    }

    public static void moveOffscreen(View view, Direction direction) {
        moveOffscreen(view, direction, null);
    }

    public static void moveOffscreen(View view, Direction direction, Runnable endAction) {
        ViewPropertyAnimator animator = view.animate().setDuration(duration).setInterpolator(new AnticipateInterpolator())
                .withEndAction(() -> {
                    moveOffscreenReset(view, direction);
                    if (endAction != null)
                        endAction.run();
                });
        view.getHeight();
        switch (direction) {
            case LEFT:
                break;
            case RIGHT:
                animator.translationX(1000).start();
                break;
            case UP:
                break;
            case DOWN:
                break;
        }
    }

    public static void moveOffscreenReset(View view, Direction direction) {
        moveOffscreenReset(view, direction, null);
    }

    public static void moveOffscreenReset(View view, Direction direction, Runnable endAction) {
        ViewPropertyAnimator animator = view.animate().setDuration(duration).setInterpolator(new OvershootInterpolator())
                .withEndAction(endAction);
        switch (direction) {
            case LEFT: case RIGHT:
                animator.translationX(0).start();
                break;
            case UP: case DOWN:
                animator.translationY(0).start();
                break;
        }
    }
}
