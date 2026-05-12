package com.exemple.habitapp;

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

public class AparenciaFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_feature_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        render(view);
    }

    private void render(View view) {
        boolean darkMode = ThemeController.isDarkMode(requireContext());
        String modeLabel = ThemeController.getModeLabel(requireContext());
        String accent = ThemeController.getAccentTheme(requireContext());

        ((TextView) view.findViewById(R.id.txtFeatureTitle)).setText("Aparencia");
        ((TextView) view.findViewById(R.id.txtFeatureSubtitle)).setText("Controle o visual claro ou escuro do HabitApp.");
        ((TextView) view.findViewById(R.id.txtFeatureHeroTitle)).setText("Modo " + modeLabel);
        ((TextView) view.findViewById(R.id.txtFeatureHeroSubtitle)).setText("Tema de cor: " + accent + ". O app atualiza o visual na hora.");
        ((LinearProgressIndicator) view.findViewById(R.id.progressFeatureHero)).setProgressCompat(100, true);

        LinearLayout list = view.findViewById(R.id.layoutFeatureContent);
        list.removeAllViews();
        addModeCard(view, list, false, "Modo claro", "Visual limpo para usar durante o dia.", !darkMode, R.color.sun);
        addModeCard(view, list, true, "Modo escuro", "Visual com menos brilho para usar a noite.", darkMode, R.color.primary_dark);

        MaterialButton primary = view.findViewById(R.id.btnFeaturePrimary);
        primary.setText(darkMode ? "Usar modo claro" : "Usar modo escuro");
        primary.setOnClickListener(v -> setMode(!darkMode, view));

        MaterialButton secondary = view.findViewById(R.id.btnFeatureSecondary);
        secondary.setVisibility(View.VISIBLE);
        secondary.setText("Abrir loja de temas");
        secondary.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateTo(R.id.temas);
            }
        });
    }

    private void addModeCard(View root, LinearLayout list, boolean darkMode, String title, String subtitle, boolean active, int colorRes) {
        FeatureUi.addCard(requireContext(), list, title + (active ? " | ativo" : ""), subtitle, active ? 100 : 0, colorRes);
        list.getChildAt(list.getChildCount() - 1).setOnClickListener(v -> setMode(darkMode, root));
    }

    private void setMode(boolean darkMode, View root) {
        if (ThemeController.isDarkMode(requireContext()) == darkMode) {
            Toast.makeText(requireContext(), darkMode ? "Modo escuro ja esta ativo." : "Modo claro ja esta ativo.", Toast.LENGTH_SHORT).show();
            return;
        }

        ThemeController.setDarkMode(requireContext(), darkMode);
        FeedbackHelper.success(requireContext());
        Toast.makeText(requireContext(), darkMode ? "Modo escuro ativado." : "Modo claro ativado.", Toast.LENGTH_SHORT).show();
        render(root);
    }
}
