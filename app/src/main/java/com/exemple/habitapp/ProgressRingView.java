package com.exemple.habitapp;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class ProgressRingView extends View {

    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF bounds = new RectF();
    private float progress;

    public ProgressRingView(Context context) {
        super(context);
        init();
    }

    public ProgressRingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ProgressRingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        int stroke = dp(8);
        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeWidth(stroke);
        trackPaint.setStrokeCap(Paint.Cap.ROUND);
        trackPaint.setColor(ContextCompat.getColor(getContext(), R.color.line));

        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(stroke);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        progressPaint.setColor(ContextCompat.getColor(getContext(), R.color.primary));
    }

    public void setProgressAnimated(int targetProgress) {
        int safeTarget = Math.max(0, Math.min(100, targetProgress));
        ValueAnimator animator = ValueAnimator.ofFloat(progress, safeTarget);
        animator.setDuration(560);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            progress = (float) animation.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float inset = dp(10);
        bounds.set(inset, inset, getWidth() - inset, getHeight() - inset);
        canvas.drawArc(bounds, 0, 360, false, trackPaint);
        canvas.drawArc(bounds, -90, 360f * (progress / 100f), false, progressPaint);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
