package com.exemple.habitapp;

import androidx.annotation.DrawableRes;

public class HabitRecord {

    public final String name;
    public final String description;
    public final String category;
    public final String frequency;
    public final String time;
    public final boolean reminder;
    public final String colorName;
    public final String iconName;

    public HabitRecord(String name, String description, String category, String frequency,
                       String time, boolean reminder, String colorName, String iconName) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.frequency = frequency;
        this.time = time;
        this.reminder = reminder;
        this.colorName = colorName;
        this.iconName = iconName;
    }

    public String subtitle() {
        String schedule = time == null || time.trim().isEmpty() ? "Sem horario fixo" : time;
        return category + " | " + frequency + " | " + schedule;
    }

    @DrawableRes
    public int iconRes() {
        if ("Agua".equals(iconName)) return R.drawable.ic_nav_water;
        if ("Foco".equals(iconName)) return R.drawable.ic_nav_focus;
        if ("Movimento".equals(iconName)) return R.drawable.ic_nav_routine;
        if ("Perfil".equals(iconName)) return R.drawable.ic_nav_profile;
        if ("Historico".equals(iconName)) return R.drawable.ic_history;
        if ("Estudo".equals(iconName)) return R.drawable.ic_nav_goals;
        return R.drawable.ic_nav_goals;
    }

    public int colorRes() {
        if ("Verde".equals(colorName)) return R.color.success;
        if ("Roxo".equals(colorName)) return R.color.study;
        if ("Amarelo".equals(colorName)) return R.color.warning;
        if ("Coral".equals(colorName)) return R.color.coral;
        if ("Agua".equals(colorName)) return R.color.water;
        return R.color.primary;
    }
}
