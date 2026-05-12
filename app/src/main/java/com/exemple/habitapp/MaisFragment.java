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
        resumo.setText("Score hoje " + HabitStore.getTodayScore(prefs) + "% | XP nivel " + XpEngine.getLevel(prefs) + " | sequencia " + HabitStore.getStreak(prefs) + " dias");

        LinearLayout layout = view.findViewById(R.id.layoutMaisAcoes);
        addAction(layout, R.drawable.ic_nav_goals, "Missoes", "Tarefas diarias com XP para subir de nivel.", R.id.missoes);
        addAction(layout, R.drawable.ic_nav_chart, "Relatorio semanal", "Resumo automatico e PDF do seu progresso.", R.id.relatorio);
        addAction(layout, R.drawable.ic_nav_chart, "Progresso", "Graficos, conquistas e leitura da semana.", R.id.progresso);
        addAction(layout, R.drawable.ic_nav_chart, "Conquistas", "Medalhas desbloqueadas por consistencia.", R.id.conquistas);
        addAction(layout, R.drawable.ic_history, "Calendario", "Dias do mes coloridos por desempenho.", R.id.calendario);
        addAction(layout, R.drawable.ic_settings, "Aparencia", "Alterne entre modo claro e escuro.", R.id.aparencia);
        addAction(layout, R.drawable.ic_settings, "Loja de temas", "Temas desbloqueados conforme seu XP cresce.", R.id.temas);
        addAction(layout, R.drawable.ic_history, "Desafios", "Ciclos de 7, 14 e 30 dias para manter ritmo.", R.id.desafios);
        addAction(layout, R.drawable.ic_edit, "Diario", "Escreva uma nota rapida sobre o dia.", R.id.diario);
        addAction(layout, R.drawable.ic_nav_chart, "Estatisticas", "Leitura avancada de agua, foco, XP e score.", R.id.estatisticas);
        addAction(layout, R.drawable.ic_nav_goals, "Metas", "Ajuste agua, foco diario e tempo das sessoes.", R.id.metas);
        addAction(layout, R.drawable.ic_history, "Historico", "Veja os ultimos dias e entenda seu ritmo.", R.id.historico);
        addAction(layout, R.drawable.ic_settings, "Lembretes", "Configure horarios e notificacoes do app.", R.id.configuracoes);
        addAction(layout, R.drawable.ic_backup, "Backup", "Exporte, restaure ou salve na nuvem.", R.id.backup);
        addAction(layout, R.drawable.ic_nav_profile, "Perfil", "Nome, objetivo e resumo geral do usuario.", R.id.perfil);
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
        arrow.setText(">");
        arrow.setTextColor(ContextCompat.getColor(requireContext(), R.color.muted));
        arrow.setTextSize(22f);
        row.addView(arrow);

        parent.addView(row, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
