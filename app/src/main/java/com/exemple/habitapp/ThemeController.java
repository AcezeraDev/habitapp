package com.exemple.habitapp;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public final class ThemeController {

    public static final String PREF_DARK_MODE = "theme_dark_mode";

    private ThemeController() {
    }

    public static void apply(Context context) {
        AppCompatDelegate.setDefaultNightMode(isDarkMode(context)
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO);
    }

    public static boolean isDarkMode(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("habit_data", Context.MODE_PRIVATE);
        return prefs.getBoolean(PREF_DARK_MODE, false);
    }

    public static void setDarkMode(Context context, boolean enabled) {
        context.getSharedPreferences("habit_data", Context.MODE_PRIVATE)
                .edit()
                .putBoolean(PREF_DARK_MODE, enabled)
                .apply();
        apply(context);
    }
}
