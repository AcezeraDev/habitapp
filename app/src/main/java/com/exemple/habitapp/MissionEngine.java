package com.exemple.habitapp;

import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

public final class MissionEngine {

    private MissionEngine() {
    }

    public static List<Mission> getDailyMissions(SharedPreferences prefs) {
        HabitStore.ensureToday(prefs);

        int agua = HabitStore.getAguaMl(prefs);
        int foco = prefs.getInt("estudos_concluidos_min", 0);
        int checklist = HabitStore.getChecklistConcluido(prefs, HabitStore.todayKey());
        int score = HabitStore.getTodayScore(prefs);
        int habitos = HabitStore.getCustomHabits(prefs).size();
        int habitosDone = HabitStore.getHabitosExtrasConcluidos(prefs, HabitStore.getCustomHabits(prefs), HabitStore.todayKey());

        List<Mission> missions = new ArrayList<>();
        missions.add(new Mission("Hidratacao inicial", "Beba 500 ml para acordar o painel.", HabitStore.percent(agua, 500), 40, R.id.agua));
        missions.add(new Mission("Bloco de foco", "Complete pelo menos 15 min de foco.", HabitStore.percent(foco, 15), 50, R.id.estudos));
        missions.add(new Mission("Duas vitorias", "Feche 2 itens do checklist.", HabitStore.percent(checklist, 2), 45, R.id.home));
        missions.add(new Mission("Score forte", "Chegue a 80% no score do dia.", score, 70, R.id.home));
        missions.add(new Mission("Habito extra", habitos == 0 ? "Crie um habito extra e marque hoje." : "Conclua um habito extra.", habitos == 0 ? 0 : HabitStore.percent(habitosDone, 1), 35, R.id.home));
        return missions;
    }

    public static int getCompletedCount(List<Mission> missions) {
        int total = 0;
        for (Mission mission : missions) {
            if (mission.isComplete()) total++;
        }
        return total;
    }

    public static int getAvailableXp(List<Mission> missions) {
        int total = 0;
        for (Mission mission : missions) {
            if (mission.isComplete()) total += mission.xpReward;
        }
        return total;
    }

    public static String getNextMissionTitle(List<Mission> missions) {
        Mission best = null;
        for (Mission mission : missions) {
            if (mission.isComplete()) continue;
            if (best == null || mission.progress > best.progress) {
                best = mission;
            }
        }
        return best == null ? "Todas as missoes de hoje foram fechadas." : best.title + " (" + best.progress + "%)";
    }
}
