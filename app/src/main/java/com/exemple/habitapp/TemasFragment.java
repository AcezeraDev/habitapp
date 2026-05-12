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

public class TemasFragment extends Fragment {

    private static final String[] NAMES = {"Classico", "Oceano", "Foco Neon", "Floresta", "Solar"};
    private static final int[] REQUIRED_LEVELS = {1, 2, 3, 5, 8};
    private static final int[] COLORS = {R.color.primary, R.color.water, R.color.study, R.color.success, R.color.sun};

    private SharedPreferences prefs;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_feature_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        prefs = requireActivity().getSharedPreferences("habit_data", Context.MODE_PRIVATE);
        int level = XpEngine.getLevel(prefs);
        String selected = ThemeController.getAccentTheme(requireContext());

        ((TextView) view.findViewById(R.id.txtFeatureTitle)).setText("Loja de temas");
        ((TextView) view.findViewById(R.id.txtFeatureSubtitle)).setText("Desbloqueie estilos conforme ganha XP.");
        ((TextView) view.findViewById(R.id.txtFeatureHeroTitle)).setText("Tema: " + selected);
        ((TextView) view.findViewById(R.id.txtFeatureHeroSubtitle)).setText("Nivel " + level + ". Novos temas liberam conforme seu uso cresce.");
        ((LinearProgressIndicator) view.findViewById(R.id.progressFeatureHero)).setProgressCompat(XpEngine.getLevelProgress(prefs), true);

        LinearLayout list = view.findViewById(R.id.layoutFeatureContent);
        list.removeAllViews();
        for (int i = 0; i < NAMES.length; i++) {
            boolean unlocked = level >= REQUIRED_LEVELS[i];
            String status = unlocked ? "Liberado" : "Desbloqueia no nivel " + REQUIRED_LEVELS[i];
            FeatureUi.addCard(requireContext(), list, NAMES[i] + (NAMES[i].equals(selected) ? " | ativo" : ""), status, unlocked ? 100 : XpEngine.getLevelProgress(prefs), COLORS[i]);
            final int index = i;
            list.getChildAt(list.getChildCount() - 1).setOnClickListener(v -> selectTheme(index, unlocked, view));
        }

        MaterialButton primary = view.findViewById(R.id.btnFeaturePrimary);
        primary.setText("Voltar para configuracoes");
        primary.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateTo(R.id.configuracoes);
            }
        });
        view.findViewById(R.id.btnFeatureSecondary).setVisibility(View.GONE);
    }

    private void selectTheme(int index, boolean unlocked, View root) {
        if (!unlocked) {
            Toast.makeText(requireContext(), "Tema bloqueado. Ganhe mais XP.", Toast.LENGTH_SHORT).show();
            return;
        }
        ThemeController.setAccentTheme(requireContext(), NAMES[index]);
        FeedbackHelper.success(requireContext());
        Toast.makeText(requireContext(), "Tema " + NAMES[index] + " selecionado.", Toast.LENGTH_SHORT).show();
        onViewCreated(root, null);
    }
}
