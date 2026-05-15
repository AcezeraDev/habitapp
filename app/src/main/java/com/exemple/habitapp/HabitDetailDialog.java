package com.exemple.habitapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

public final class HabitDetailDialog {

    private HabitDetailDialog() {
    }

    public static void show(Context context, SharedPreferences prefs, View root, HabitRecord habit, Runnable onChanged) {
        boolean done = HabitStore.isHabitDoneToday(prefs, habit.name);
        int colorRes = habit.colorRes();

        ScrollView scroll = new ScrollView(context);
        LinearLayout content = HabitUi.paddedColumn(context, 20);
        content.setBackground(HabitUi.rounded(context, R.color.surface, R.color.line, 1, 26));
        scroll.addView(content);

        LinearLayout header = new LinearLayout(context);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.addView(HabitUi.iconBox(context, habit.iconRes(), colorRes));

        LinearLayout titleBlock = new LinearLayout(context);
        titleBlock.setOrientation(LinearLayout.VERTICAL);
        titleBlock.setPadding(HabitUi.dp(context, 14), 0, 0, 0);
        TextView title = HabitUi.text(context, habit.name, 22, R.color.ink, true);
        TextView subtitle = HabitUi.text(context, habit.subtitle(), 13, R.color.muted, false);
        titleBlock.addView(title);
        titleBlock.addView(subtitle);
        header.addView(titleBlock, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        content.addView(header);

        TextView description = HabitUi.text(context, habit.description, 14, R.color.muted, false);
        description.setPadding(0, HabitUi.dp(context, 14), 0, HabitUi.dp(context, 8));
        content.addView(description);

        LinearLayout stats = new LinearLayout(context);
        stats.setOrientation(LinearLayout.HORIZONTAL);
        stats.setPadding(0, HabitUi.dp(context, 10), 0, HabitUi.dp(context, 12));
        stats.addView(metric(context, "Atual", HabitStore.getHabitStreak(prefs, habit.name) + "d", colorRes), weightParams(context, 0, 6));
        stats.addView(metric(context, "Melhor", HabitStore.getHabitBestStreak(prefs, habit.name, 90) + "d", R.color.success), weightParams(context, 6, 6));
        stats.addView(metric(context, "7 dias", HabitStore.getHabitWeekCompletionPercent(prefs, habit.name) + "%", R.color.primary), weightParams(context, 6, 0));
        content.addView(stats);

        TextView total = HabitUi.text(
                context,
                HabitStore.getHabitCompletedTotal(prefs, habit.name, 30) + " conclusoes nos ultimos 30 dias",
                13,
                R.color.muted,
                true
        );
        total.setPadding(0, 0, 0, HabitUi.dp(context, 10));
        content.addView(total);

        LinearProgressIndicator weekProgress = new LinearProgressIndicator(context);
        weekProgress.setMax(100);
        weekProgress.setIndicatorColor(ContextCompat.getColor(context, colorRes));
        weekProgress.setTrackColor(ContextCompat.getColor(context, R.color.line));
        weekProgress.setTrackThickness(HabitUi.dp(context, 8));
        weekProgress.setTrackCornerRadius(HabitUi.dp(context, 8));
        content.addView(weekProgress, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        UiAnimator.animateProgress(weekProgress, HabitStore.getHabitWeekCompletionPercent(prefs, habit.name));

        content.addView(weekDots(context, prefs, habit, colorRes));

        MaterialButton toggle = new MaterialButton(context);
        toggle.setText(done ? "Reabrir hoje" : "Marcar como concluido");
        toggle.setAllCaps(false);
        toggle.setIconResource(done ? R.drawable.ic_clock_history : R.drawable.check_circle);
        toggle.setIconTint(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.white)));
        toggle.setTextColor(ContextCompat.getColor(context, R.color.white));
        toggle.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, done ? R.color.coral : colorRes)));
        toggle.setCornerRadius(HabitUi.dp(context, 18));
        HabitUi.press(toggle);
        LinearLayout.LayoutParams toggleParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, HabitUi.dp(context, 54));
        toggleParams.setMargins(0, HabitUi.dp(context, 16), 0, 0);
        content.addView(toggle, toggleParams);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(scroll)
                .create();
        toggle.setOnClickListener(v -> {
            HabitStore.setHabitDoneToday(prefs, habit.name, !done);
            FeedbackHelper.success(context);
            if (root != null) {
                FeedbackHelper.snack(root, !done ? "Habito concluido." : "Habito reaberto.");
                if (!done) CelebrationView.burst(root);
            }
            if (onChanged != null) onChanged.run();
            dialog.dismiss();
        });
        dialog.setOnShowListener(d -> {
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            }
        });
        dialog.show();
    }

    private static LinearLayout metric(Context context, String label, String value, int colorRes) {
        LinearLayout box = HabitUi.paddedColumn(context, 12);
        box.setGravity(Gravity.CENTER);
        box.setBackground(HabitUi.rounded(context, softForColor(colorRes), colorRes, 1, 18));
        TextView valueView = HabitUi.text(context, value, 18, R.color.ink, true);
        valueView.setGravity(Gravity.CENTER);
        TextView labelView = HabitUi.text(context, label, 11, R.color.muted, false);
        labelView.setGravity(Gravity.CENTER);
        box.addView(valueView);
        box.addView(labelView);
        return box;
    }

    private static LinearLayout weekDots(Context context, SharedPreferences prefs, HabitRecord habit, int colorRes) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, HabitUi.dp(context, 14), 0, 0);
        for (int offset = -13; offset <= 0; offset++) {
            boolean done = HabitStore.wasHabitDoneOnDay(prefs, habit.name, HabitStore.dayKey(offset));
            TextView dot = new TextView(context);
            dot.setText(offset == 0 ? "H" : "");
            dot.setGravity(Gravity.CENTER);
            dot.setTextSize(9f);
            dot.setTextColor(ContextCompat.getColor(context, done ? R.color.white : R.color.muted));
            dot.setBackground(HabitUi.rounded(context, done ? colorRes : R.color.surface_soft, done ? colorRes : R.color.line, 1, 8));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, HabitUi.dp(context, 28), 1f);
            params.setMargins(HabitUi.dp(context, 2), 0, HabitUi.dp(context, 2), 0);
            row.addView(dot, params);
        }
        return row;
    }

    private static LinearLayout.LayoutParams weightParams(Context context, int startMargin, int endMargin) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        params.setMargins(HabitUi.dp(context, startMargin), 0, HabitUi.dp(context, endMargin), 0);
        return params;
    }

    private static int softForColor(int colorRes) {
        if (colorRes == R.color.water) return R.color.water_soft;
        if (colorRes == R.color.study) return R.color.study_soft;
        if (colorRes == R.color.success) return R.color.success_soft;
        if (colorRes == R.color.coral || colorRes == R.color.warning || colorRes == R.color.danger) return R.color.coral_soft;
        return R.color.primary_soft;
    }
}
