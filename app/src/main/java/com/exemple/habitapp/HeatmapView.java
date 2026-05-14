package com.exemple.habitapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class HeatmapView extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int[] scores = new int[35];

    public HeatmapView(Context context) {
        super(context);
    }

    public HeatmapView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public HeatmapView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setScores(int[] values) {
        if (values == null) return;
        scores = values;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int columns = 7;
        int rows = 5;
        float gap = dp(7);
        float cell = Math.min((getWidth() - gap * (columns - 1)) / columns, (getHeight() - gap * (rows - 1)) / rows);
        float startX = (getWidth() - (cell * columns + gap * (columns - 1))) / 2f;
        float startY = (getHeight() - (cell * rows + gap * (rows - 1))) / 2f;

        for (int i = 0; i < rows * columns && i < scores.length; i++) {
            int score = Math.max(0, Math.min(100, scores[i]));
            int row = i / columns;
            int column = i % columns;
            paint.setColor(colorFor(score));
            canvas.drawRoundRect(
                    startX + column * (cell + gap),
                    startY + row * (cell + gap),
                    startX + column * (cell + gap) + cell,
                    startY + row * (cell + gap) + cell,
                    dp(9),
                    dp(9),
                    paint
            );
        }
    }

    private int colorFor(int score) {
        if (score >= 85) return ContextCompat.getColor(getContext(), R.color.success);
        if (score >= 60) return ContextCompat.getColor(getContext(), R.color.primary);
        if (score >= 35) return ContextCompat.getColor(getContext(), R.color.warning);
        if (score > 0) return ContextCompat.getColor(getContext(), R.color.coral);
        return ContextCompat.getColor(getContext(), R.color.surface_soft);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
