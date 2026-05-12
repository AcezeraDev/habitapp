package com.exemple.habitapp;

public class Mission {

    public final String title;
    public final String subtitle;
    public final int progress;
    public final int xpReward;
    public final int destinationId;

    public Mission(String title, String subtitle, int progress, int xpReward, int destinationId) {
        this.title = title;
        this.subtitle = subtitle;
        this.progress = Math.max(0, Math.min(100, progress));
        this.xpReward = xpReward;
        this.destinationId = destinationId;
    }

    public boolean isComplete() {
        return progress >= 100;
    }
}
