package com.exemple.habitapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

public class DesafiosFragment extends Fragment {

    private SharedPreferences prefs;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_feature_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        prefs = requireActivity().getSharedPreferences("habit_data", Context.MODE_PRIVATE);
        render(view);
    }

    private void render(View view) {
        int goal = prefs.getInt("active_challenge_goal", prefs.getInt("challenge_goal_days", 30));
        int streak = HabitStore.getStreak(prefs);
        int progress = HabitStore.percent(streak, goal);

        ((TextView) view.findViewById(R.id.txtFeatureTitle)).setText("Modo desafio");
        ((TextView) view.findViewById(R.id.txtFeatureSubtitle)).setText("Escolha um ciclo de 7, 14 ou 30 dias fortes.");
        ((TextView) view.findViewById(R.id.txtFeatureHeroTitle)).setText("Desafio de " + goal + " dias");
        ((TextView) view.findViewById(R.id.txtFeatureHeroSubtitle)).setText(streak + " dias fortes na sequência atual. Troféu final em " + Math.max(0, goal - streak) + " dias.");
        ((LinearProgressIndicator) view.findViewById(R.id.progressFeatureHero)).setProgressCompat(progress, true);

        LinearLayout list = view.findViewById(R.id.layoutFeatureContent);
        list.removeAllViews();
        addChallenge(view, list, 7, "Sprint de 7 dias", "Perfeito para voltar ao ritmo sem peso.");
        addChallenge(view, list, 14, "Consistência 14", "Duas semanas de base sólida.");
        addChallenge(view, list, 30, "Projeto 30 dias", "Ciclo completo com trofeu final.");

        MaterialButton primary = view.findViewById(R.id.btnFeaturePrimary);
        primary.setText("Abrir rotina");
        primary.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateTo(R.id.rotina);
            }
        });
        view.findViewById(R.id.btnFeatureSecondary).setVisibility(View.GONE);
    }

    private void addChallenge(View root, LinearLayout list, int days, String title, String subtitle) {
        int streak = HabitStore.getStreak(prefs);
        boolean active = prefs.getInt("active_challenge_goal", 30) == days;
        FeatureUi.addCard(requireContext(), list, title + (active ? " | ativo" : ""), subtitle, HabitStore.percent(streak, days), active ? R.color.success : R.color.primary);
        list.getChildAt(list.getChildCount() - 1).setOnClickListener(v -> {
            prefs.edit().putInt("active_challenge_goal", days).putInt("challenge_goal_days", days).apply();
            FeedbackHelper.success(requireContext());
            Toast.makeText(requireContext(), "Desafio de " + days + " dias ativado.", Toast.LENGTH_SHORT).show();
            render(root);
        });
    }
}
