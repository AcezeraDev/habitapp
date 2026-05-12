package com.exemple.habitapp;

public class Achievement {

    public final String title;
    public final String subtitle;
    public final boolean unlocked;
    public final int progress;
    public final int colorRes;

    public Achievement(String title, String subtitle, boolean unlocked, int progress, int colorRes) {
        this.title = title;
        this.subtitle = subtitle;
        this.unlocked = unlocked;
        this.progress = Math.max(0, Math.min(100, progress));
        this.colorRes = colorRes;
    }
}
