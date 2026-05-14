package com.exemple.habitapp;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CelebrationView extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final List<Particle> particles = new ArrayList<>();
    private float progress;

    public CelebrationView(Context context) {
        super(context);
        setWillNotDraw(false);
    }

    public static void burst(View anchor) {
        if (anchor == null || anchor.getContext() == null) return;
        View root = anchor.getRootView();
        if (!(root instanceof ViewGroup)) return;

        ViewGroup parent = (ViewGroup) root;
        CelebrationView overlay = new CelebrationView(anchor.getContext());
        parent.addView(overlay, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        overlay.start();
    }

    private void start() {
        buildParticles();
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(980);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            progress = (float) animation.getAnimatedValue();
            invalidate();
        });
        animator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                if (getParent() instanceof ViewGroup) {
                    ((ViewGroup) getParent()).removeView(CelebrationView.this);
                }
            }
        });
        animator.start();
    }

    private void buildParticles() {
        Random random = new Random();
        int[] colors = new int[]{
                ContextCompat.getColor(getContext(), R.color.primary),
                ContextCompat.getColor(getContext(), R.color.success),
                ContextCompat.getColor(getContext(), R.color.coral),
                ContextCompat.getColor(getContext(), R.color.study),
                ContextCompat.getColor(getContext(), R.color.warning)
        };
        particles.clear();
        for (int i = 0; i < 38; i++) {
            float angle = (float) (Math.PI * 2 * random.nextFloat());
            float speed = dp(70 + random.nextInt(140));
            Particle particle = new Particle();
            particle.direction = new PointF((float) Math.cos(angle) * speed, (float) Math.sin(angle) * speed);
            particle.size = dp(4 + random.nextInt(5));
            particle.color = colors[i % colors.length];
            particle.delay = random.nextFloat() * 0.18f;
            particles.add(particle);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        for (Particle particle : particles) {
            float local = Math.max(0f, Math.min(1f, (progress - particle.delay) / (1f - particle.delay)));
            if (local <= 0f) continue;
            paint.setColor(particle.color);
            paint.setAlpha((int) (255 * (1f - local)));
            float x = centerX + particle.direction.x * local;
            float y = centerY + particle.direction.y * local + dp(42) * local * local;
            canvas.drawCircle(x, y, particle.size * (1f - local * 0.35f), paint);
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private static class Particle {
        PointF direction;
        float size;
        int color;
        float delay;
    }
}
