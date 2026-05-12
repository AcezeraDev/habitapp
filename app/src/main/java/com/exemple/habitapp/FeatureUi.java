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
        card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.surface));
        card.setRadius(dp(context, 8));
        card.setCardElevation(0);
        card.setStrokeWidth(dp(context, 1));
        card.setStrokeColor(ContextCompat.getColor(context, R.color.line));

        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(context, 16), dp(context, 14), dp(context, 16), dp(context, 14));

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
        subtitleView.setPadding(0, dp(context, 5), 0, progress >= 0 ? dp(context, 10) : 0);
        content.addView(subtitleView);

        if (progress >= 0) {
            LinearProgressIndicator bar = new LinearProgressIndicator(context);
            bar.setMax(100);
            bar.setProgressCompat(progress, false);
            bar.setIndicatorColor(ContextCompat.getColor(context, colorRes));
            bar.setTrackColor(ContextCompat.getColor(context, R.color.line));
            bar.setTrackThickness(dp(context, 6));
            bar.setTrackCornerRadius(dp(context, 8));
            content.addView(bar, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        card.addView(content);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, dp(context, 10));
        parent.addView(card, params);
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
}
