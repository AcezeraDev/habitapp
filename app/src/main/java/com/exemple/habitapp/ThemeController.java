package com.exemple.habitapp;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public final class ThemeController {

    public static final String PREF_DARK_MODE = "theme_dark_mode";
    public static final String PREF_ACCENT_THEME = "theme_accent_name";

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

    public static String getModeLabel(Context context) {
        return isDarkMode(context) ? "Escuro" : "Claro";
    }

    public static void setDarkMode(Context context, boolean enabled) {
        context.getSharedPreferences("habit_data", Context.MODE_PRIVATE)
                .edit()
                .putBoolean(PREF_DARK_MODE, enabled)
                .apply();
        apply(context);
    }

    public static String getAccentTheme(Context context) {
        return context.getSharedPreferences("habit_data", Context.MODE_PRIVATE)
                .getString(PREF_ACCENT_THEME, "Classico");
    }

    public static void setAccentTheme(Context context, String themeName) {
        context.getSharedPreferences("habit_data", Context.MODE_PRIVATE)
                .edit()
                .putString(PREF_ACCENT_THEME, themeName)
                .apply();
    }
}
