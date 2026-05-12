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
        FeatureUi.addCard(requireContext(), list, "Produtividade", prefs.getInt("total_foco_min_registrado", 0) + " minutos de foco registrados no total.", -1, R.color.study);
        FeatureUi.addCard(requireContext(), list, "Hidratacao", prefs.getInt("total_agua_ml_registrado", 0) + " ml de agua registrados no total.", -1, R.color.water);
        FeatureUi.addCard(requireContext(), list, "XP e nivel", XpEngine.getBaseXp(prefs) + " XP | nivel " + XpEngine.getLevel(prefs) + " | " + XpEngine.getLevelProgress(prefs) + "% ate o proximo nivel.", XpEngine.getLevelProgress(prefs), R.color.primary);
        FeatureUi.addCard(requireContext(), list, "Conquistas", AchievementEngine.getUnlockedCount(AchievementEngine.getAchievements(prefs)) + " medalhas desbloqueadas.", -1, R.color.success);

        view.findViewById(R.id.btnFeaturePrimary).setVisibility(View.GONE);
        view.findViewById(R.id.btnFeatureSecondary).setVisibility(View.GONE);
    }
}
