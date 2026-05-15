package com.exemple.habitapp;

import android.content.SharedPreferences;

import java.util.List;

public final class XpEngine {

    private static final String CLAIM_DAY = "xp_claim_day";
    private static final String CLAIMED_XP = "xp_claimed_total";

    private XpEngine() {
    }

    public static int getBaseXp(SharedPreferences prefs) {
        int totalAgua = prefs.getInt("total_agua_ml_registrado", 0) / 50;
        int totalFoco = prefs.getInt("total_foco_min_registrado", 0) * 2;
        int streak = HabitStore.getStreak(prefs) * 120;
        int weekly = HabitStore.getWeeklyAverage(prefs) * 4;
        int achievements = AchievementEngine.getUnlockedCount(AchievementEngine.getAchievements(prefs)) * 90;
        return Math.max(0, totalAgua + totalFoco + streak + weekly + achievements + prefs.getInt(CLAIMED_XP, 0));
    }

    public static int getLevel(SharedPreferences prefs) {
        return Math.max(1, (getBaseXp(prefs) / 500) + 1);
    }

    public static int getLevelProgress(SharedPreferences prefs) {
        return HabitStore.percent(getBaseXp(prefs) % 500, 500);
    }

    public static int getXpToNextLevel(SharedPreferences prefs) {
        int remainder = getBaseXp(prefs) % 500;
        return remainder == 0 ? 500 : 500 - remainder;
    }

    public static int getStreakBonus(SharedPreferences prefs) {
        return HabitStore.getStreak(prefs) * 120;
    }

    public static boolean hasClaimedToday(SharedPreferences prefs) {
        return prefs.getLong(CLAIM_DAY, -1) == HabitStore.todayKey();
    }

    public static int claimDailyMissions(SharedPreferences prefs) {
        if (hasClaimedToday(prefs)) return 0;
        List<Mission> missions = MissionEngine.getDailyMissions(prefs);
        int xp = MissionEngine.getAvailableXp(missions);
        if (xp <= 0) return 0;
        prefs.edit()
                .putLong(CLAIM_DAY, HabitStore.todayKey())
                .putInt(CLAIMED_XP, prefs.getInt(CLAIMED_XP, 0) + xp)
                .apply();
        return xp;
    }
}
