package com.exemple.habitapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class EstatisticasFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_feature_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SharedPreferences prefs = requireActivity().getSharedPreferences("habit_data", Context.MODE_PRIVATE);
        HabitStore.ensureToday(prefs);
        HabitStore.saveTodaySnapshot(prefs);

        int best = 0;
        int strong = 0;
        int total = 0;
        for (int offset = 0; offset > -30; offset--) {
            int score = HabitStore.getScoreForDay(prefs, HabitStore.dayKey(offset));
            best = Math.max(best, score);
            if (score >= 80) strong++;
            total += score;
        }
        int monthlyAverage = Math.round(total / 30f);

        ((TextView) view.findViewById(R.id.txtFeatureTitle)).setText("Estatisticas");
        ((TextView) view.findViewById(R.id.txtFeatureSubtitle)).setText("Uma leitura mais avancada do seu ritmo.");
        ((TextView) view.findViewById(R.id.txtFeatureHeroTitle)).setText(monthlyAverage + "% media mensal");
        ((TextView) view.findViewById(R.id.txtFeatureHeroSubtitle)).setText(strong + " dias fortes nos ultimos 30 dias. Melhor score: " + best + "%.");
        ((LinearProgressIndicator) view.findViewById(R.id.progressFeatureHero)).setProgressCompat(monthlyAverage, true);

        LinearLayout list = view.findViewById(R.id.layoutFeatureContent);
        list.removeAllViews();
        FeatureUi.addCard(requireContext(), list, "Melhor dia da semana", bestWeekday(prefs), -1, R.color.primary);
        FeatureUi.addCard(requireContext(), list, "Habito mais consistente", habitRanking(prefs, true), -1, R.color.success);
        FeatureUi.addCard(requireContext(), list, "Habito para recuperar", habitRanking(prefs, false), -1, R.color.coral);
        FeatureUi.addCard(requireContext(), list, "Produtividade", prefs.getInt("total_foco_min_registrado", 0) + " minutos de foco registrados no total.", -1, R.color.study);
        FeatureUi.addCard(requireContext(), list, "Hidratacao", prefs.getInt("total_agua_ml_registrado", 0) + " ml de agua registrados no total.", -1, R.color.water);
        FeatureUi.addCard(requireContext(), list, "XP e nivel", XpEngine.getBaseXp(prefs) + " XP | nivel " + XpEngine.getLevel(prefs) + " | " + XpEngine.getLevelProgress(prefs) + "% ate o proximo nivel.", XpEngine.getLevelProgress(prefs), R.color.primary);
        FeatureUi.addCard(requireContext(), list, "Conquistas", AchievementEngine.getUnlockedCount(AchievementEngine.getAchievements(prefs)) + " medalhas desbloqueadas.", -1, R.color.success);

        view.findViewById(R.id.btnFeaturePrimary).setVisibility(View.GONE);
        view.findViewById(R.id.btnFeatureSecondary).setVisibility(View.GONE);
    }

    private String habitRanking(SharedPreferences prefs, boolean best) {
        List<HabitRecord> habits = HabitStore.getHabitRecords(prefs);
        if (habits.isEmpty()) {
            return best ? "Crie um habito para medir consistencia." : "Nenhum habito precisa de recuperacao ainda.";
        }

        HabitRecord selected = null;
        int selectedPercent = best ? -1 : 101;
        for (HabitRecord habit : habits) {
            int percent = HabitStore.percent(HabitStore.getHabitCompletedTotal(prefs, habit.name, 30), 30);
            if ((best && percent > selectedPercent) || (!best && percent < selectedPercent)) {
                selected = habit;
                selectedPercent = percent;
            }
        }

        if (selected == null) return "Sem dados suficientes.";
        return selected.name + " com " + selectedPercent + "% nos ultimos 30 dias.";
    }

    private String bestWeekday(SharedPreferences prefs) {
        int[] totals = new int[7];
        int[] counts = new int[7];
        Calendar calendar = Calendar.getInstance();

        for (int offset = 0; offset > -30; offset--) {
            calendar.setTimeInMillis(HabitStore.dayKey(offset) * 24L * 60L * 60L * 1000L);
            int index = calendar.get(Calendar.DAY_OF_WEEK) - 1;
            totals[index] += HabitStore.getScoreForDay(prefs, HabitStore.dayKey(offset));
            counts[index]++;
        }

        int bestIndex = 0;
        int bestAverage = -1;
        for (int i = 0; i < totals.length; i++) {
            int average = counts[i] == 0 ? 0 : Math.round(totals[i] / (float) counts[i]);
            if (average > bestAverage) {
                bestAverage = average;
                bestIndex = i;
            }
        }

        String day = new DateFormatSymbols(Locale.getDefault()).getWeekdays()[bestIndex + 1];
        return capitalize(day) + " costuma render melhor, com media de " + bestAverage + "%.";
    }

    private String capitalize(String value) {
        if (value == null || value.isEmpty()) return "";
        return value.substring(0, 1).toUpperCase(Locale.getDefault()) + value.substring(1);
    }
}
