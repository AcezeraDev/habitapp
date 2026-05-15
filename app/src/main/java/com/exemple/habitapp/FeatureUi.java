package com.exemple.habitapp;

import android.content.Context;
import android.graphics.Typeface;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;

public final class FeatureUi {

    private FeatureUi() {
    }

    public static void addCard(Context context, LinearLayout parent, String title, String subtitle, int progress, int colorRes) {
        MaterialCardView card = new MaterialCardView(context);
        card.setCardBackgroundColor(ContextCompat.getColor(context, softForColor(colorRes)));
        card.setRadius(dp(context, 22));
        card.setCardElevation(dp(context, 5));
        card.setStrokeWidth(dp(context, 1));
        card.setStrokeColor(ContextCompat.getColor(context, colorRes));
        HabitUi.press(card);

        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(context, 20), dp(context, 18), dp(context, 20), dp(context, 18));

        TextView titleView = new TextView(context);
        titleView.setText(title);
        titleView.setTextColor(ContextCompat.getColor(context, R.color.ink));
        titleView.setTextSize(16f);
        titleView.setTypeface(titleView.getTypeface(), Typeface.BOLD);
        content.addView(titleView);

        TextView subtitleView = new TextView(context);
        subtitleView.setText(subtitle);
        subtitleView.setTextColor(ContextCompat.getColor(context, R.color.muted));
        subtitleView.setTextSize(13f);
        subtitleView.setPadding(0, dp(context, 6), 0, progress >= 0 ? dp(context, 12) : 0);
        content.addView(subtitleView);

        if (progress >= 0) {
            LinearProgressIndicator bar = new LinearProgressIndicator(context);
            bar.setMax(100);
            bar.setProgressCompat(0, false);
            bar.setIndicatorColor(ContextCompat.getColor(context, colorRes));
            bar.setTrackColor(ContextCompat.getColor(context, R.color.line));
            bar.setTrackThickness(dp(context, 8));
            bar.setTrackCornerRadius(dp(context, 8));
            content.addView(bar, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            UiAnimator.animateProgress(bar, progress);
        }

        card.addView(content);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, dp(context, 10));
        parent.addView(card, params);
        UiAnimator.enterDelayed(card, Math.min(parent.getChildCount(), 8) * 35L);
    }

    public static TextView addText(Context context, LinearLayout parent, String text, int textSize, boolean bold) {
        TextView view = new TextView(context);
        view.setText(text);
        view.setTextColor(ContextCompat.getColor(context, R.color.ink));
        view.setTextSize(textSize);
        if (bold) view.setTypeface(view.getTypeface(), Typeface.BOLD);
        view.setPadding(0, dp(context, 6), 0, dp(context, 6));
        parent.addView(view);
        return view;
    }

    public static int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    private static int softForColor(int colorRes) {
        if (colorRes == R.color.water) return R.color.water_soft;
        if (colorRes == R.color.study) return R.color.study_soft;
        if (colorRes == R.color.success) return R.color.success_soft;
        if (colorRes == R.color.coral || colorRes == R.color.warning) return R.color.coral_soft;
        return R.color.primary_soft;
    }
}
