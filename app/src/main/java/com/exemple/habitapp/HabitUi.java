package com.exemple.habitapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public final class HabitUi {

    private HabitUi() {
    }

    public static MaterialCardView surfaceCard(Context context) {
        MaterialCardView card = new MaterialCardView(context);
        card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.surface));
        card.setRadius(dp(context, 28));
        card.setCardElevation(dp(context, 6));
        card.setStrokeWidth(dp(context, 1));
        card.setStrokeColor(ContextCompat.getColor(context, R.color.line));
        card.setUseCompatPadding(true);
        card.setPreventCornerOverlap(true);
        press(card);
        return card;
    }

    public static LinearLayout paddedColumn(Context context, int paddingDp) {
        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        int padding = dp(context, paddingDp);
        content.setPadding(padding, padding, padding, padding);
        return content;
    }

    public static TextView text(Context context, String value, int sizeSp, int colorRes, boolean bold) {
        TextView view = new TextView(context);
        view.setText(value);
        view.setTextColor(ContextCompat.getColor(context, colorRes));
        view.setTextSize(sizeSp);
        view.setIncludeFontPadding(true);
        if (bold) view.setTypeface(view.getTypeface(), Typeface.BOLD);
        return view;
    }

    public static TextView badge(Context context, String value, int colorRes) {
        TextView view = text(context, value, 12, colorRes, true);
        view.setGravity(Gravity.CENTER);
        view.setPadding(dp(context, 11), dp(context, 7), dp(context, 11), dp(context, 7));
        view.setBackground(rounded(context, softForColor(colorRes), colorRes, 1, 20));
        return view;
    }

    public static ImageView iconBox(Context context, int iconRes, int colorRes) {
        ImageView icon = new ImageView(context);
        icon.setImageResource(iconRes);
        icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        icon.setImageTintList(isPremiumIcon(iconRes) ? null : ColorStateList.valueOf(ContextCompat.getColor(context, colorRes)));
        icon.setPadding(dp(context, 10), dp(context, 10), dp(context, 10), dp(context, 10));
        icon.setBackground(rounded(context, softForColor(colorRes), colorRes, 1, 20));
        icon.setContentDescription(null);
        icon.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        icon.setLayoutParams(new LinearLayout.LayoutParams(dp(context, 52), dp(context, 52)));
        return icon;
    }

    public static boolean isPremiumIcon(int iconRes) {
        return iconRes == R.drawable.ic_premium_book
                || iconRes == R.drawable.ic_premium_drop
                || iconRes == R.drawable.ic_premium_dumbbell
                || iconRes == R.drawable.ic_premium_equalizer
                || iconRes == R.drawable.ic_premium_fire
                || iconRes == R.drawable.ic_premium_moon
                || iconRes == R.drawable.ic_premium_music
                || iconRes == R.drawable.ic_premium_sleep
                || iconRes == R.drawable.ic_premium_sun
                || iconRes == R.drawable.ic_premium_trophy
                || iconRes == R.drawable.ic_premium_video;
    }

    public static GradientDrawable rounded(Context context, int fillRes, int strokeRes, int strokeDp, int radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(ContextCompat.getColor(context, fillRes));
        drawable.setCornerRadius(dp(context, radiusDp));
        if (strokeDp > 0) {
            drawable.setStroke(dp(context, strokeDp), ContextCompat.getColor(context, strokeRes));
        }
        return drawable;
    }

    public static void addWithBottomMargin(LinearLayout parent, View child, int bottomDp) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, dp(parent.getContext(), bottomDp));
        parent.addView(child, params);
    }

    public static void installPressFeedback(View root) {
        if (root instanceof MaterialButton) {
            press(root);
        }

        if (!(root instanceof ViewGroup)) return;

        ViewGroup group = (ViewGroup) root;
        for (int i = 0; i < group.getChildCount(); i++) {
            installPressFeedback(group.getChildAt(i));
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    public static void press(View view) {
        view.setOnTouchListener((v, event) -> {
            switch (event.getActionMasked()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    v.animate().scaleX(0.965f).scaleY(0.965f).setDuration(80).start();
                    break;
                case android.view.MotionEvent.ACTION_CANCEL:
                    v.animate().scaleX(1f).scaleY(1f).setDuration(120).start();
                    break;
                case android.view.MotionEvent.ACTION_UP:
                    v.animate()
                            .scaleX(1.025f)
                            .scaleY(1.025f)
                            .setDuration(90)
                            .withEndAction(() -> v.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(130)
                                    .start())
                            .start();
                    break;
                default:
                    break;
            }
            return false;
        });
    }

    public static void tooltip(View view, String label) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            view.setTooltipText(label);
        }
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
