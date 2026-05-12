package com.exemple.habitapp;

import android.animation.ValueAnimator;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.progressindicator.BaseProgressIndicator;

public final class UiAnimator {

    private UiAnimator() {
    }

    public static void animatePercentText(TextView textView, int target) {
        ValueAnimator animator = ValueAnimator.ofInt(0, target);
        animator.setDuration(520);
        animator.addUpdateListener(animation -> textView.setText(animation.getAnimatedValue() + "%"));
        animator.start();
    }

    public static void animateProgress(BaseProgressIndicator<?> indicator, int target) {
        int safeTarget = Math.max(0, Math.min(indicator.getMax(), target));
        ValueAnimator animator = ValueAnimator.ofInt(indicator.getProgress(), safeTarget);
        animator.setDuration(420);
        animator.addUpdateListener(animation -> indicator.setProgressCompat((Integer) animation.getAnimatedValue(), false));
        animator.start();
    }

    public static void enter(View view) {
        view.setAlpha(0f);
        view.setTranslationY(16f);
        view.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(260)
                .start();
    }

    public static void pulse(View view) {
        view.animate()
                .scaleX(1.04f)
                .scaleY(1.04f)
                .setDuration(90)
                .withEndAction(() -> view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(120)
                        .start())
                .start();
    }
}
