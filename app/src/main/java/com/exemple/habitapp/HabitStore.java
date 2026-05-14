package com.exemple.habitapp;

import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public final class HabitStore {

    public static final String CUSTOM_HABITS_KEY = "custom_habits";
    private static final long DAY_MILLIS = 1000L * 60L * 60L * 24L;
    private static final String META_PREFIX = "habit_meta_";

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

        if (lastActiveDay != today && lastActiveDay == legacyUtcTodayKey()) {
            migrateLegacyCurrentDay(prefs, lastActiveDay, today);
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

        for (int i = 0; i < 7; i++) {
            long day = dayKey(i - 6);
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

        for (int i = 0; i < 90; i++) {
            if (getScoreForDay(prefs, dayKey(-i)) >= 80) {
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
        if (streak >= 7 && weeklyAverage >= 75) return "Avançado";
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

    public static List<HabitRecord> getHabitRecords(SharedPreferences prefs) {
        List<String> names = getCustomHabits(prefs);
        List<HabitRecord> records = new ArrayList<>();
        for (String name : names) {
            records.add(getHabitRecord(prefs, name));
        }
        return records;
    }

    public static HabitRecord getHabitRecord(SharedPreferences prefs, String habitName) {
        String name = sanitizeHabitName(habitName);
        String category = prefs.getString(getHabitMetaKey(name, "category"), inferCategory(name));
        String frequency = prefs.getString(getHabitMetaKey(name, "frequency"), "Diario");
        String description = prefs.getString(getHabitMetaKey(name, "description"), defaultDescription(name, category));
        String time = prefs.getString(getHabitMetaKey(name, "time"), "");
        boolean reminder = prefs.getBoolean(getHabitMetaKey(name, "reminder"), false);
        String color = prefs.getString(getHabitMetaKey(name, "color"), defaultColorForCategory(category));
        String icon = prefs.getString(getHabitMetaKey(name, "icon"), defaultIconForCategory(category));
        return new HabitRecord(name, description, category, frequency, time, reminder, color, icon);
    }

    public static void saveHabitRecord(SharedPreferences prefs, String oldName, HabitRecord record) {
        String newName = sanitizeHabitName(record.name);
        List<String> habits = getCustomHabits(prefs);
        String oldSafeName = sanitizeHabitName(oldName);

        if (!TextUtils.isEmpty(oldSafeName) && habits.contains(oldSafeName)) {
            int index = habits.indexOf(oldSafeName);
            habits.set(index, newName);
        } else if (!habits.contains(newName)) {
            habits.add(newName);
        }

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(CUSTOM_HABITS_KEY, TextUtils.join("|", habits));
        putHabitMeta(editor, newName, "description", record.description);
        putHabitMeta(editor, newName, "category", record.category);
        putHabitMeta(editor, newName, "frequency", record.frequency);
        putHabitMeta(editor, newName, "time", record.time);
        editor.putBoolean(getHabitMetaKey(newName, "reminder"), record.reminder);
        putHabitMeta(editor, newName, "color", record.colorName);
        putHabitMeta(editor, newName, "icon", record.iconName);

        if (!TextUtils.isEmpty(oldSafeName) && !oldSafeName.equals(newName)) {
            boolean doneToday = prefs.getBoolean(getHabitoKey(oldSafeName, todayKey()), false);
            editor.remove(getHabitoKey(oldSafeName, todayKey()));
            editor.putBoolean(getHabitoKey(newName, todayKey()), doneToday);
            removeHabitMeta(editor, oldSafeName);
        }

        editor.apply();
        saveTodaySnapshot(prefs);
    }

    public static void removeHabitRecord(SharedPreferences prefs, String habitName) {
        String name = sanitizeHabitName(habitName);
        List<String> habits = getCustomHabits(prefs);
        habits.remove(name);

        SharedPreferences.Editor editor = prefs.edit()
                .putString(CUSTOM_HABITS_KEY, TextUtils.join("|", habits))
                .remove(getHabitoKey(name, todayKey()));
        removeHabitMeta(editor, name);
        editor.apply();
        saveTodaySnapshot(prefs);
    }

    public static boolean isHabitDoneToday(SharedPreferences prefs, String habitName) {
        return prefs.getBoolean(getHabitoKey(sanitizeHabitName(habitName), todayKey()), false);
    }

    public static void setHabitDoneToday(SharedPreferences prefs, String habitName, boolean done) {
        prefs.edit().putBoolean(getHabitoKey(sanitizeHabitName(habitName), todayKey()), done).apply();
        saveTodaySnapshot(prefs);
    }

    public static int getHabitStreak(SharedPreferences prefs, String habitName) {
        String name = sanitizeHabitName(habitName);
        int streak = 0;
        for (int i = 0; i < 90; i++) {
            if (prefs.getBoolean(getHabitoKey(name, dayKey(-i)), false)) {
                streak++;
            } else {
                break;
            }
        }
        return streak;
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

    public static String sanitizeHabitName(String habitName) {
        if (habitName == null) return "";
        return habitName.trim().replace("\n", " ").replace("|", "/");
    }

    private static String getHabitMetaKey(String habitName, String field) {
        return META_PREFIX + field + "_" + habitName.hashCode();
    }

    private static void putHabitMeta(SharedPreferences.Editor editor, String habitName, String field, String value) {
        editor.putString(getHabitMetaKey(habitName, field), value == null ? "" : value.trim());
    }

    private static void removeHabitMeta(SharedPreferences.Editor editor, String habitName) {
        editor.remove(getHabitMetaKey(habitName, "description"));
        editor.remove(getHabitMetaKey(habitName, "category"));
        editor.remove(getHabitMetaKey(habitName, "frequency"));
        editor.remove(getHabitMetaKey(habitName, "time"));
        editor.remove(getHabitMetaKey(habitName, "reminder"));
        editor.remove(getHabitMetaKey(habitName, "color"));
        editor.remove(getHabitMetaKey(habitName, "icon"));
    }

    private static String inferCategory(String name) {
        String value = name == null ? "" : name.toLowerCase();
        if (value.contains("agua") || value.contains("beber") || value.contains("hidrata")) return "Saude";
        if (value.contains("estud") || value.contains("foco") || value.contains("ler")) return "Foco";
        if (value.contains("trein") || value.contains("caminh") || value.contains("corpo")) return "Movimento";
        if (value.contains("sono") || value.contains("dorm")) return "Sono";
        return "Rotina";
    }

    private static String defaultDescription(String name, String category) {
        return "Pequena acao de " + category.toLowerCase() + " para manter consistencia hoje.";
    }

    private static String defaultColorForCategory(String category) {
        if ("Saude".equals(category)) return "Agua";
        if ("Foco".equals(category)) return "Roxo";
        if ("Movimento".equals(category)) return "Coral";
        if ("Sono".equals(category)) return "Verde";
        return "Azul";
    }

    private static String defaultIconForCategory(String category) {
        if ("Saude".equals(category)) return "Agua";
        if ("Foco".equals(category)) return "Foco";
        if ("Movimento".equals(category)) return "Movimento";
        if ("Sono".equals(category)) return "Historico";
        return "Estudo";
    }

    private static void migrateLegacyCurrentDay(SharedPreferences prefs, long legacyDay, long today) {
        SharedPreferences.Editor editor = prefs.edit()
                .putLong("last_active_day", today);

        copyBoolean(editor, prefs, "check_planejamento_", legacyDay, today);
        copyBoolean(editor, prefs, "check_treino_", legacyDay, today);
        copyBoolean(editor, prefs, "check_sono_", legacyDay, today);
        copyBoolean(editor, prefs, "rotina_bloco_manha_", legacyDay, today);
        copyBoolean(editor, prefs, "rotina_bloco_alimentacao_", legacyDay, today);
        copyBoolean(editor, prefs, "rotina_bloco_treino_", legacyDay, today);
        copyBoolean(editor, prefs, "rotina_bloco_sono_", legacyDay, today);
        copyInt(editor, prefs, "mood_", legacyDay, today);
        copyInt(editor, prefs, "energy_", legacyDay, today);
        copyString(editor, prefs, "agua_log_", legacyDay, today);
        copyString(editor, prefs, "focus_log_", legacyDay, today);

        for (String habito : getCustomHabits(prefs)) {
            String oldKey = getHabitoKey(habito, legacyDay);
            if (prefs.contains(oldKey)) {
                editor.putBoolean(getHabitoKey(habito, today), prefs.getBoolean(oldKey, false));
            }
        }

        if (prefs.getLong("daily_setup_day", -1) == legacyDay) {
            editor.putLong("daily_setup_day", today);
        }
        if (prefs.getLong("ultimo_dia_foto", -1) == legacyDay) {
            editor.putLong("ultimo_dia_foto", today);
        }

        editor.apply();
    }

    private static void copyBoolean(SharedPreferences.Editor editor, SharedPreferences prefs, String prefix, long oldDay, long newDay) {
        String oldKey = prefix + oldDay;
        if (prefs.contains(oldKey)) {
            editor.putBoolean(prefix + newDay, prefs.getBoolean(oldKey, false));
        }
    }

    private static void copyInt(SharedPreferences.Editor editor, SharedPreferences prefs, String prefix, long oldDay, long newDay) {
        String oldKey = prefix + oldDay;
        if (prefs.contains(oldKey)) {
            editor.putInt(prefix + newDay, prefs.getInt(oldKey, -1));
        }
    }

    private static void copyString(SharedPreferences.Editor editor, SharedPreferences prefs, String prefix, long oldDay, long newDay) {
        String oldKey = prefix + oldDay;
        if (prefs.contains(oldKey)) {
            editor.putString(prefix + newDay, prefs.getString(oldKey, ""));
        }
    }

    public static int percent(double atual, double meta) {
        if (meta <= 0) return 0;
        return (int) Math.max(0, Math.min(100, Math.round((atual / meta) * 100)));
    }

    public static long todayKey() {
        return dayKey(0);
    }

    public static long dayKey(int daysFromToday) {
        Calendar calendar = Calendar.getInstance();
        if (daysFromToday != 0) {
            calendar.add(Calendar.DAY_OF_YEAR, daysFromToday);
        }

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis() / DAY_MILLIS;
    }

    private static long legacyUtcTodayKey() {
        return System.currentTimeMillis() / DAY_MILLIS;
    }
}
