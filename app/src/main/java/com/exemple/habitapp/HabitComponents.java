package com.exemple.habitapp;

import android.content.Context;
import android.content.res.ColorStateList;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;

public final class HabitComponents {

    private HabitComponents() {
    }

    public static MaterialCardView habitCard(
            Context context,
            HabitRecord habit,
            boolean done,
            int streak,
            View.OnClickListener doneClick,
            View.OnClickListener editClick,
            View.OnClickListener deleteClick
    ) {
        return habitCard(context, habit, done, streak, doneClick, editClick, deleteClick, null);
    }

    public static MaterialCardView habitCard(
            Context context,
            HabitRecord habit,
            boolean done,
            int streak,
            View.OnClickListener doneClick,
            View.OnClickListener editClick,
            View.OnClickListener deleteClick,
            View.OnClickListener detailClick
    ) {
        int colorRes = habit.colorRes();
        MaterialCardView card = HabitUi.surfaceCard(context);
        card.setRadius(HabitUi.dp(context, 28));
        card.setCardBackgroundColor(ContextCompat.getColor(context, done ? R.color.success_soft : softForColor(colorRes)));
        card.setStrokeColor(ContextCompat.getColor(context, done ? R.color.success : colorRes));
        card.setCardElevation(HabitUi.dp(context, done ? 8 : 5));
        if (detailClick != null) {
            card.setOnClickListener(detailClick);
        }

        LinearLayout content = HabitUi.paddedColumn(context, 16);
        content.addView(habitHeader(context, habit, streak, colorRes));

        if (!TextUtils.isEmpty(habit.description)) {
            TextView description = HabitUi.text(context, habit.description, 14, R.color.muted, false);
            description.setPadding(0, HabitUi.dp(context, 12), 0, HabitUi.dp(context, 8));
            content.addView(description);
        }

        LinearProgressIndicator progress = new LinearProgressIndicator(context);
        progress.setMax(100);
        progress.setTrackThickness(HabitUi.dp(context, 8));
        progress.setTrackCornerRadius(HabitUi.dp(context, 8));
        progress.setTrackColor(ContextCompat.getColor(context, R.color.line));
        progress.setIndicatorColor(ContextCompat.getColor(context, done ? R.color.success : colorRes));
        content.addView(progress, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        UiAnimator.animateProgress(progress, done ? 100 : 18);

        LinearLayout actions = new LinearLayout(context);
        actions.setGravity(Gravity.CENTER_VERTICAL);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setPadding(0, HabitUi.dp(context, 14), 0, 0);

        MaterialButton doneButton = animatedCheckButton(context, done, colorRes);
        doneButton.setOnClickListener(doneClick);
        actions.addView(doneButton, new LinearLayout.LayoutParams(0, HabitUi.dp(context, 50), 1f));

        if (detailClick != null) {
            MaterialButton detailButton = iconButton(context, R.drawable.ic_clock_history, colorRes, "Detalhes do habito");
            detailButton.setOnClickListener(detailClick);
            actions.addView(detailButton);
        }

        MaterialButton editButton = iconButton(context, R.drawable.ic_edit, R.color.primary, "Editar habito");
        editButton.setOnClickListener(editClick);
        actions.addView(editButton);

        MaterialButton deleteButton = iconButton(context, R.drawable.ic_delete, R.color.danger, "Excluir habito");
        deleteButton.setOnClickListener(deleteClick);
        actions.addView(deleteButton);

        content.addView(actions);
        card.addView(content);
        return card;
    }

    public static LinearLayout sectionTitle(Context context, String title, String subtitle, int iconRes, int colorRes) {
        LinearLayout row = new LinearLayout(context);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, HabitUi.dp(context, 4), 0, HabitUi.dp(context, 8));
        row.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        row.addView(HabitUi.iconBox(context, iconRes, colorRes));

        LinearLayout texts = new LinearLayout(context);
        texts.setOrientation(LinearLayout.VERTICAL);
        texts.setPadding(HabitUi.dp(context, 12), 0, 0, 0);
        texts.addView(HabitUi.text(context, title, 17, R.color.ink, true));
        texts.addView(HabitUi.text(context, subtitle, 13, R.color.muted, false));
        row.addView(texts, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        return row;
    }

    public static MaterialCardView statsCard(Context context, String title, String value, int colorRes) {
        MaterialCardView card = HabitUi.surfaceCard(context);
        card.setCardBackgroundColor(ContextCompat.getColor(context, softForColor(colorRes)));
        card.setStrokeColor(ContextCompat.getColor(context, colorRes));
        card.setRadius(HabitUi.dp(context, 22));

        LinearLayout content = HabitUi.paddedColumn(context, 14);
        content.setGravity(Gravity.CENTER);
        TextView valueView = HabitUi.text(context, value, 22, R.color.ink, true);
        valueView.setGravity(Gravity.CENTER);
        TextView titleView = HabitUi.text(context, title, 12, R.color.muted, false);
        titleView.setGravity(Gravity.CENTER);
        content.addView(valueView);
        content.addView(titleView);
        card.addView(content);
        return card;
    }

    public static LinearLayout emptyState(Context context, String title, String subtitle, int iconRes) {
        LinearLayout content = HabitUi.paddedColumn(context, 24);
        content.setGravity(Gravity.CENTER);
        content.setBackground(HabitUi.rounded(context, R.color.surface_tint, R.color.line, 1, 28));
        content.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        ImageView icon = HabitUi.iconBox(context, iconRes, R.color.primary);
        content.addView(icon);

        TextView titleView = HabitUi.text(context, title, 18, R.color.ink, true);
        titleView.setGravity(Gravity.CENTER);
        titleView.setPadding(0, HabitUi.dp(context, 14), 0, 0);
        content.addView(titleView);

        TextView subtitleView = HabitUi.text(context, subtitle, 14, R.color.muted, false);
        subtitleView.setGravity(Gravity.CENTER);
        subtitleView.setPadding(0, HabitUi.dp(context, 6), 0, 0);
        content.addView(subtitleView);
        return content;
    }

    public static MaterialButton animatedCheckButton(Context context, boolean done, int colorRes) {
        MaterialButton button = new MaterialButton(context);
        button.setText(done ? "Feito" : "Concluir");
        button.setAllCaps(false);
        button.setIconResource(done ? R.drawable.check_circle : R.drawable.ic_nav_goals);
        button.setIconTint(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.white)));
        button.setTextColor(ContextCompat.getColor(context, R.color.white));
        button.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, done ? R.color.success : colorRes)));
        button.setCornerRadius(HabitUi.dp(context, 22));
        HabitUi.press(button);
        return button;
    }

    public static MaterialButton iconButton(Context context, int iconRes, int tintRes, String label) {
        MaterialButton button = new MaterialButton(context, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
        button.setText("");
        button.setContentDescription(label);
        button.setTooltipText(label);
        button.setIconResource(iconRes);
        button.setIconTint(ColorStateList.valueOf(ContextCompat.getColor(context, tintRes)));
        button.setIconPadding(0);
        button.setMinWidth(0);
        button.setMinimumWidth(0);
        button.setInsetTop(0);
        button.setInsetBottom(0);
        button.setStrokeWidth(HabitUi.dp(context, 1));
        button.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(context, tintRes)));
        button.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, softForColor(tintRes))));
        button.setCornerRadius(HabitUi.dp(context, 22));
        HabitUi.press(button);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(HabitUi.dp(context, 50), HabitUi.dp(context, 50));
        params.setMargins(HabitUi.dp(context, 8), 0, 0, 0);
        button.setLayoutParams(params);
        return button;
    }

    public static TextView streakBadge(Context context, String value, int colorRes) {
        return HabitUi.badge(context, value, colorRes);
    }

    private static LinearLayout habitHeader(Context context, HabitRecord habit, int streak, int colorRes) {
        LinearLayout header = new LinearLayout(context);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.addView(HabitUi.iconBox(context, habit.iconRes(), colorRes));

        LinearLayout texts = new LinearLayout(context);
        texts.setOrientation(LinearLayout.VERTICAL);
        texts.setPadding(HabitUi.dp(context, 12), 0, HabitUi.dp(context, 8), 0);
        TextView title = HabitUi.text(context, habit.name, 17, R.color.ink, true);
        title.setMaxLines(1);
        title.setEllipsize(TextUtils.TruncateAt.END);
        texts.addView(title);
        texts.addView(HabitUi.text(context, habit.subtitle(), 13, R.color.muted, false));
        header.addView(texts, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        header.addView(streakBadge(context, streak + "d streak", colorRes));
        return header;
    }

    private static int softForColor(int colorRes) {
        if (colorRes == R.color.water) return R.color.water_soft;
        if (colorRes == R.color.study) return R.color.study_soft;
        if (colorRes == R.color.success) return R.color.success_soft;
        if (colorRes == R.color.coral || colorRes == R.color.warning || colorRes == R.color.danger) return R.color.coral_soft;
        return R.color.primary_soft;
    }
}
