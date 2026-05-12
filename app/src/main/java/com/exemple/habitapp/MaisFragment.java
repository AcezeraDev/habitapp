package com.exemple.habitapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class MaisFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mais, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences prefs = requireActivity().getSharedPreferences("habit_data", Context.MODE_PRIVATE);
        HabitStore.ensureToday(prefs);

        TextView resumo = view.findViewById(R.id.txtMaisResumo);
        resumo.setText("Score hoje " + HabitStore.getTodayScore(prefs) + "% • sequência " + HabitStore.getStreak(prefs) + " dias • nível " + HabitStore.getLevelName(prefs));

        LinearLayout layout = view.findViewById(R.id.layoutMaisAcoes);
        addAction(layout, R.drawable.ic_nav_chart, "Progresso", "Gráficos, conquistas e leitura da semana.", R.id.progresso);
        addAction(layout, R.drawable.ic_nav_chart, "Conquistas", "Medalhas desbloqueadas por consistência.", R.id.conquistas);
        addAction(layout, R.drawable.ic_history, "Calendário", "Dias do mês coloridos por desempenho.", R.id.calendario);
        addAction(layout, R.drawable.ic_nav_goals, "Metas", "Ajuste água, foco diário e tempo das sessões.", R.id.metas);
        addAction(layout, R.drawable.ic_history, "Histórico", "Veja os últimos dias e entenda seu ritmo.", R.id.historico);
        addAction(layout, R.drawable.ic_settings, "Lembretes", "Configure horários e notificações do app.", R.id.configuracoes);
        addAction(layout, R.drawable.ic_backup, "Backup", "Exporte ou restaure seus dados.", R.id.backup);
        addAction(layout, R.drawable.ic_nav_profile, "Perfil", "Nome, objetivo e resumo geral do usuário.", R.id.perfil);
    }

    private void addAction(LinearLayout parent, int iconRes, String title, String subtitle, int destinationId) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(dp(12), dp(12), dp(12), dp(12));
        row.setClickable(true);
        row.setFocusable(true);
        row.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateTo(destinationId);
            }
        });

        ImageView icon = new ImageView(requireContext());
        icon.setImageResource(iconRes);
        icon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.primary));
        row.addView(icon, new LinearLayout.LayoutParams(dp(30), dp(30)));

        LinearLayout texts = new LinearLayout(requireContext());
        texts.setOrientation(LinearLayout.VERTICAL);
        texts.setPadding(dp(14), 0, 0, 0);

        TextView titleView = new TextView(requireContext());
        titleView.setText(title);
        titleView.setTextColor(ContextCompat.getColor(requireContext(), R.color.ink));
        titleView.setTextSize(16f);
        titleView.setTypeface(titleView.getTypeface(), Typeface.BOLD);

        TextView subtitleView = new TextView(requireContext());
        subtitleView.setText(subtitle);
        subtitleView.setTextColor(ContextCompat.getColor(requireContext(), R.color.muted));
        subtitleView.setTextSize(13f);
        subtitleView.setPadding(0, dp(3), 0, 0);

        texts.addView(titleView);
        texts.addView(subtitleView);
        row.addView(texts, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView arrow = new TextView(requireContext());
        arrow.setText("›");
        arrow.setTextColor(ContextCompat.getColor(requireContext(), R.color.muted));
        arrow.setTextSize(28f);
        row.addView(arrow);

        parent.addView(row, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
