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

import java.util.Locale;

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
        resumo.setText(String.format(
                Locale.getDefault(),
                "Hoje: %d%%  |  Nível %d  |  Sequência %d dias",
                HabitStore.getTodayScore(prefs),
                XpEngine.getLevel(prefs),
                HabitStore.getStreak(prefs)
        ));

        LinearLayout layout = view.findViewById(R.id.layoutMaisAcoes);
        addSection(layout, "Essenciais");
        addAction(layout, R.drawable.ic_nav_water, "Água", "Meta diária, histórico semanal e ajuste em ml.", R.id.agua);
        addAction(layout, R.drawable.ic_nav_routine, "Rotina", "Acompanhe seu ritmo e mantenha seus blocos do dia.", R.id.rotina);
        addAction(layout, R.drawable.ic_nav_goals, "Metas", "Ajuste água, foco diário e tempo das sessões.", R.id.metas);
        addAction(layout, R.drawable.ic_notification, "Lembretes", "Configure horários e notificações do app.", R.id.configuracoes);

        addSection(layout, "Evolução");
        addAction(layout, R.drawable.ic_mission_flag, "Missões", "Tarefas diárias com XP para subir de nível.", R.id.missoes);
        addAction(layout, R.drawable.ic_nav_chart, "Relatório semanal", "Resumo automático e PDF do seu progresso.", R.id.relatorio);
        addAction(layout, R.drawable.ic_premium_trophy, "Conquistas", "Medalhas desbloqueadas por consistência.", R.id.conquistas);
        addAction(layout, R.drawable.ic_history, "Calendário", "Dias do mês coloridos por desempenho.", R.id.calendario);
        addAction(layout, R.drawable.ic_clock_history, "Histórico", "Veja os últimos dias e entenda seu ritmo.", R.id.historico);
        addAction(layout, R.drawable.ic_premium_fire, "Desafios", "Ciclos de 7, 14 e 30 dias para manter ritmo.", R.id.desafios);
        addAction(layout, R.drawable.ic_nav_chart, "Estatísticas", "Leitura avançada de água, foco, XP e score.", R.id.estatisticas);

        addSection(layout, "Conta e personalização");
        addAction(layout, R.drawable.ic_nav_profile, "Perfil", "Nome, objetivo e resumo geral do usuário.", R.id.perfil);
        addAction(layout, R.drawable.ic_premium_book, "Diário", "Escreva uma nota rápida sobre o dia.", R.id.diario);
        addAction(layout, R.drawable.ic_theme_palette, "Aparência", "Alterne entre modo claro e escuro.", R.id.aparencia);
        addAction(layout, R.drawable.ic_theme_palette, "Loja de temas", "Temas desbloqueados conforme seu XP cresce.", R.id.temas);
        addAction(layout, R.drawable.ic_backup, "Backup", "Exporte, restaure ou salve na nuvem.", R.id.backup);
        UiAnimator.enter(view);
    }

    private void addSection(LinearLayout parent, String title) {
        TextView section = new TextView(requireContext());
        section.setText(title);
        section.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_dark));
        section.setTextSize(12f);
        section.setTypeface(section.getTypeface(), Typeface.BOLD);
        section.setAllCaps(true);
        section.setPadding(dp(4), dp(12), dp(4), dp(8));
        parent.addView(section);
    }

    private void addAction(LinearLayout parent, int iconRes, String title, String subtitle, int destinationId) {
        int accent = accentFor(destinationId);
        LinearLayout row = new LinearLayout(requireContext());
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(dp(14), dp(14), dp(14), dp(14));
        row.setClickable(true);
        row.setFocusable(true);
        row.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateTo(destinationId);
            }
        });

        ImageView icon = new ImageView(requireContext());
        icon.setImageResource(iconRes);
        if (!HabitUi.isPremiumIcon(iconRes)) {
            icon.setColorFilter(ContextCompat.getColor(requireContext(), accent));
        }
        icon.setPadding(dp(9), dp(9), dp(9), dp(9));
        icon.setBackground(HabitUi.rounded(requireContext(), softFor(accent), accent, 1, 18));
        row.addView(icon, new LinearLayout.LayoutParams(dp(44), dp(44)));

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
        arrow.setText("\u203A");
        arrow.setTextColor(ContextCompat.getColor(requireContext(), accent));
        arrow.setTextSize(22f);
        row.addView(arrow);

        com.google.android.material.card.MaterialCardView card = HabitUi.surfaceCard(requireContext());
        card.setCardBackgroundColor(ContextCompat.getColor(requireContext(), softFor(accent)));
        card.setStrokeColor(ContextCompat.getColor(requireContext(), accent));
        card.addView(row);
        HabitUi.addWithBottomMargin(parent, card, 10);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private int accentFor(int destinationId) {
        if (destinationId == R.id.agua) {
            return R.color.water;
        }
        if (destinationId == R.id.backup || destinationId == R.id.configuracoes || destinationId == R.id.aparencia) {
            return R.color.primary;
        }
        if (destinationId == R.id.progresso || destinationId == R.id.relatorio || destinationId == R.id.estatisticas) {
            return R.color.study;
        }
        if (destinationId == R.id.calendario || destinationId == R.id.historico || destinationId == R.id.diario) {
            return R.color.coral;
        }
        if (destinationId == R.id.metas || destinationId == R.id.missoes || destinationId == R.id.conquistas) {
            return R.color.success;
        }
        return R.color.water;
    }

    private int softFor(int accent) {
        if (accent == R.color.study) return R.color.study_soft;
        if (accent == R.color.coral) return R.color.coral_soft;
        if (accent == R.color.success) return R.color.success_soft;
        if (accent == R.color.water) return R.color.water_soft;
        return R.color.primary_soft;
    }
}
