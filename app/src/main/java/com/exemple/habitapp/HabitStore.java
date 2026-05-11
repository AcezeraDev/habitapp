package com.exemple.habitapp;

import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

public final class HabitStore {

    public static final String CUSTOM_HABITS_KEY = "custom_habits";

    private HabitStore() {
    }

    public static void ensureToday(SharedPreferences prefs) {
        long today = todayKey();
        long lastActiveDay = prefs.getLong("last_active_day", -1);

        if (lastActiveDay == -1) {
            prefs.edit().putLong("last_active_day", today).apply();
            saveTodaySnapshot(prefs);
            return;
        }

        if (lastActiveDay != today) {
            saveSnapshotForDay(prefs, lastActiveDay);

            prefs.edit()
                    .putFloat("agua_litros", 0f)
                    .putInt("estudos_concluidos_min", 0)
                    .putInt("sessoes_foco_concluidas", 0)
                    .putLong("last_active_day", today)
                    .apply();

            saveTodaySnapshot(prefs);
        }
    }

    public static void saveTodaySnapshot(SharedPreferences prefs) {
        saveSnapshotForDay(prefs, todayKey());
    }

    public static void saveSnapshotForDay(SharedPreferences prefs, long day) {
        int aguaMl = getAguaMl(prefs);
        int metaAguaMl = getMetaAguaMl(prefs);
        int estudos = prefs.getInt("estudos_concluidos_min", 0);
        int metaEstudos = prefs.getInt("meta_estudos_min", 60);
        int checklist = getChecklistConcluido(prefs, day);
        List<String> habitos = getCustomHabits(prefs);
        int habitosConcluidos = getHabitosExtrasConcluidos(prefs, habitos, day);

        int aguaPercent = percent(aguaMl, metaAguaMl);
        int estudoPercent = percent(estudos, metaEstudos);
        int checklistPercent = percent(checklist, 3);
        int habitosPercent = habitos.isEmpty() ? 100 : percent(habitosConcluidos, habitos.size());
        int score = habitos.isEmpty()
                ? (aguaPercent + estudoPercent + checklistPercent) / 3
                : (aguaPercent + estudoPercent + checklistPercent + habitosPercent) / 4;

        prefs.edit()
                .putInt("score_day_" + day, score)
                .putInt("agua_ml_day_" + day, aguaMl)
                .putInt("estudos_min_day_" + day, estudos)
                .putInt("checklist_day_" + day, checklist)
                .putInt("habitos_done_day_" + day, habitosConcluidos)
                .apply();
    }

    public static int getTodayScore(SharedPreferences prefs) {
        return getScoreForDay(prefs, todayKey());
    }

    public static int getScoreForDay(SharedPreferences prefs, long day) {
        if (day == todayKey()) {
            int aguaPercent = percent(getAguaMl(prefs), getMetaAguaMl(prefs));
            int estudoPercent = percent(prefs.getInt("estudos_concluidos_min", 0), prefs.getInt("meta_estudos_min", 60));
            int checklistPercent = percent(getChecklistConcluido(prefs, day), 3);
            List<String> habitos = getCustomHabits(prefs);
            int habitosPercent = habitos.isEmpty() ? 100 : percent(getHabitosExtrasConcluidos(prefs, habitos, day), habitos.size());

            return habitos.isEmpty()
                    ? (aguaPercent + estudoPercent + checklistPercent) / 3
                    : (aguaPercent + estudoPercent + checklistPercent + habitosPercent) / 4;
        }

        return prefs.getInt("score_day_" + day, 0);
    }

    public static int[] getWeekScores(SharedPreferences prefs) {
        int[] scores = new int[7];
        long today = todayKey();

        for (int i = 0; i < 7; i++) {
            long day = today - (6 - i);
            scores[i] = getScoreForDay(prefs, day);
        }

        return scores;
    }

    public static int getWeeklyAverage(SharedPreferences prefs) {
        int[] scores = getWeekScores(prefs);
        int total = 0;

        for (int score : scores) {
            total += score;
        }

        return Math.round(total / 7f);
    }

    public static int getStreak(SharedPreferences prefs) {
        int streak = 0;
        long today = todayKey();

        for (int i = 0; i < 90; i++) {
            if (getScoreForDay(prefs, today - i) >= 80) {
                streak++;
            } else {
                break;
            }
        }

        return streak;
    }

    public static String getLevelName(SharedPreferences prefs) {
        int streak = getStreak(prefs);
        int weeklyAverage = getWeeklyAverage(prefs);

        if (streak >= 14 && weeklyAverage >= 85) return "Elite";
        if (streak >= 7 && weeklyAverage >= 75) return "Avancado";
        if (streak >= 3 || weeklyAverage >= 60) return "Consistente";
        return "Inicial";
    }

    public static int getAguaMl(SharedPreferences prefs) {
        return (int) Math.round(prefs.getFloat("agua_litros", 0f) * 1000);
    }

    public static int getMetaAguaMl(SharedPreferences prefs) {
        return (int) Math.round(prefs.getFloat("meta_litros", 2.0f) * 1000);
    }

    public static int getChecklistConcluido(SharedPreferences prefs, long day) {
        int total = 0;
        if (prefs.getBoolean("check_planejamento_" + day, false)) total++;
        if (prefs.getBoolean("check_treino_" + day, false)) total++;
        if (prefs.getBoolean("check_sono_" + day, false)) total++;
        return total;
    }

    public static List<String> getCustomHabits(SharedPreferences prefs) {
        String salvos = prefs.getString(CUSTOM_HABITS_KEY, "");
        List<String> habitos = new ArrayList<>();
        if (TextUtils.isEmpty(salvos)) return habitos;

        String[] partes = salvos.split("\\|");
        for (String parte : partes) {
            if (!TextUtils.isEmpty(parte.trim())) {
                habitos.add(parte.trim());
            }
        }
        return habitos;
    }

    public static void saveCustomHabits(SharedPreferences prefs, List<String> habitos) {
        prefs.edit().putString(CUSTOM_HABITS_KEY, TextUtils.join("|", habitos)).apply();
    }

    public static int getHabitosExtrasConcluidos(SharedPreferences prefs, List<String> habitos, long day) {
        int total = 0;
        for (String habito : habitos) {
            if (prefs.getBoolean(getHabitoKey(habito, day), false)) total++;
        }
        return total;
    }

    public static String getHabitoKey(String habito, long day) {
        return "custom_habit_" + day + "_" + habito.hashCode();
    }

    public static int percent(double atual, double meta) {
        if (meta <= 0) return 0;
        return (int) Math.max(0, Math.min(100, Math.round((atual / meta) * 100)));
    }

    public static long todayKey() {
        return System.currentTimeMillis() / (1000L * 60 * 60 * 24);
    }
}
