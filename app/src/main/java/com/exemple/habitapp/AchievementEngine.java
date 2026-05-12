package com.exemple.habitapp;

import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

public final class AchievementEngine {

    private AchievementEngine() {
    }

    public static List<Achievement> getAchievements(SharedPreferences prefs) {
        HabitStore.ensureToday(prefs);
        HabitStore.saveTodaySnapshot(prefs);

        long today = HabitStore.todayKey();
        int aguaMl = HabitStore.getAguaMl(prefs);
        int metaAguaMl = HabitStore.getMetaAguaMl(prefs);
        int focoMin = prefs.getInt("estudos_concluidos_min", 0);
        int metaFoco = prefs.getInt("meta_estudos_min", 60);
        int sessoes = prefs.getInt("sessoes_foco_concluidas", 0);
        int checklist = HabitStore.getChecklistConcluido(prefs, today);
        int score = HabitStore.getTodayScore(prefs);
        int streak = HabitStore.getStreak(prefs);
        int weeklyAverage = HabitStore.getWeeklyAverage(prefs);
        int totalAgua = prefs.getInt("total_agua_ml_registrado", 0);
        int totalFoco = prefs.getInt("total_foco_min_registrado", 0);
        List<String> habitos = HabitStore.getCustomHabits(prefs);
        int habitosDone = HabitStore.getHabitosExtrasConcluidos(prefs, habitos, today);
        boolean perfilCompleto = !TextUtils.isEmpty(prefs.getString("nome_usuario", ""))
                && !TextUtils.isEmpty(prefs.getString("email_usuario", ""))
                && !TextUtils.isEmpty(prefs.getString("objetivo_principal", ""));

        List<Achievement> list = new ArrayList<>();
        add(list, "Perfil pronto", "Nome, e-mail e objetivo preenchidos.", perfilCompleto, perfilCompleto ? 100 : 50, R.color.primary);
        add(list, "Meta de agua", aguaMl + " ml de " + metaAguaMl + " ml hoje.", aguaMl >= metaAguaMl, HabitStore.percent(aguaMl, metaAguaMl), R.color.water);
        add(list, "Foco do dia", focoMin + " min de " + metaFoco + " min.", focoMin >= metaFoco, HabitStore.percent(focoMin, metaFoco), R.color.study);
        add(list, "Checklist completo", checklist + " de 3 itens marcados.", checklist >= 3, HabitStore.percent(checklist, 3), R.color.success);
        add(list, "Dia forte", "Bata 80% de score no dia.", score >= 80, score, R.color.coral);
        add(list, "Tres sessoes", "Complete 3 blocos de foco no mesmo dia.", sessoes >= 3, HabitStore.percent(sessoes, 3), R.color.study);
        add(list, "Habitos extras", habitos.isEmpty() ? "Crie e conclua seus habitos extras." : habitosDone + " de " + habitos.size() + " concluidos.", !habitos.isEmpty() && habitosDone == habitos.size(), habitos.isEmpty() ? 0 : HabitStore.percent(habitosDone, habitos.size()), R.color.warning);
        add(list, "Sequencia 3 dias", "Mantenha 80% por 3 dias seguidos.", streak >= 3, HabitStore.percent(streak, 3), R.color.success);
        add(list, "Semana consistente", "7 dias fortes em sequencia.", streak >= 7, HabitStore.percent(streak, 7), R.color.success);
        add(list, "Media 70%", "Media semanal acima de 70%.", weeklyAverage >= 70, weeklyAverage, R.color.primary);
        add(list, "10 litros", totalAgua + " ml registrados no total.", totalAgua >= 10000, HabitStore.percent(totalAgua, 10000), R.color.water);
        add(list, "5 horas de foco", totalFoco + " min registrados no total.", totalFoco >= 300, HabitStore.percent(totalFoco, 300), R.color.study);
        add(list, "Nivel Elite", "Sequencia longa com media alta.", "Elite".equals(HabitStore.getLevelName(prefs)), Math.min(100, (streak * 5) + (weeklyAverage / 2)), R.color.sun);
        return list;
    }

    public static int getUnlockedCount(List<Achievement> achievements) {
        int total = 0;
        for (Achievement achievement : achievements) {
            if (achievement.unlocked) total++;
        }
        return total;
    }

    private static void add(List<Achievement> list, String title, String subtitle, boolean unlocked, int progress, int colorRes) {
        list.add(new Achievement(title, subtitle, unlocked, progress, colorRes));
    }
}
