package com.exemple.habitapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class HistoricoFragment extends Fragment {

    private SharedPreferences prefs;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_historico, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefs = requireActivity().getSharedPreferences("habit_data", Context.MODE_PRIVATE);
        HabitStore.ensureToday(prefs);
        HabitStore.saveTodaySnapshot(prefs);

        TextView resumo = view.findViewById(R.id.txtHistoricoResumo);
        LinearLayout layout = view.findViewById(R.id.layoutHistoricoDias);

        int melhorScore = 0;
        int diasFortes = 0;
        for (int offset = 0; offset > -14; offset--) {
            int score = HabitStore.getScoreForDay(prefs, HabitStore.dayKey(offset));
            melhorScore = Math.max(melhorScore, score);
            if (score >= 80) diasFortes++;
        }

        resumo.setText("Melhor score dos últimos 14 dias: " + melhorScore + "% • dias fortes: " + diasFortes);
        renderDays(layout);
    }

    private void renderDays(LinearLayout layout) {
        layout.removeAllViews();
        for (int offset = 0; offset > -14; offset--) {
            addDayCard(layout, offset);
        }
    }

    private void addDayCard(LinearLayout parent, int offset) {
        long day = HabitStore.dayKey(offset);
        int score = HabitStore.getScoreForDay(prefs, day);
        int aguaMl = offset == 0 ? HabitStore.getAguaMl(prefs) : prefs.getInt("agua_ml_day_" + day, 0);
        int estudos = offset == 0 ? prefs.getInt("estudos_concluidos_min", 0) : prefs.getInt("estudos_min_day_" + day, 0);
        int checklist = offset == 0 ? HabitStore.getChecklistConcluido(prefs, day) : prefs.getInt("checklist_day_" + day, 0);
        int habitos = offset == 0
                ? HabitStore.getHabitosExtrasConcluidos(prefs, HabitStore.getCustomHabits(prefs), day)
                : prefs.getInt("habitos_done_day_" + day, 0);
        int accent = score >= 80 ? R.color.success : R.color.primary;

        MaterialCardView card = new MaterialCardView(requireContext());
        card.setCardBackgroundColor(ContextCompat.getColor(requireContext(), score >= 80 ? R.color.success_soft : R.color.primary_soft));
        card.setRadius(dp(8));
        card.setCardElevation(dp(2));
        card.setStrokeWidth(dp(1));
        card.setStrokeColor(ContextCompat.getColor(requireContext(), accent));

        LinearLayout content = new LinearLayout(requireContext());
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(16), dp(14), dp(16), dp(14));

        LinearLayout header = new LinearLayout(requireContext());
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setOrientation(LinearLayout.HORIZONTAL);

        TextView title = new TextView(requireContext());
        title.setText(formatDay(offset));
        title.setTextColor(ContextCompat.getColor(requireContext(), R.color.ink));
        title.setTextSize(16f);
        title.setTypeface(title.getTypeface(), Typeface.BOLD);
        header.addView(title, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView scoreView = new TextView(requireContext());
        scoreView.setText(score + "%");
        scoreView.setGravity(Gravity.END);
        scoreView.setTextColor(ContextCompat.getColor(requireContext(), accent));
        scoreView.setTextSize(20f);
        scoreView.setTypeface(scoreView.getTypeface(), Typeface.BOLD);
        scoreView.setBackground(HabitUi.rounded(requireContext(), R.color.surface, accent, 1, 8));
        scoreView.setPadding(dp(10), dp(5), dp(10), dp(5));
        header.addView(scoreView);
        content.addView(header);

        LinearProgressIndicator progress = new LinearProgressIndicator(requireContext());
        progress.setMax(100);
        progress.setProgressCompat(score, false);
        progress.setIndicatorColor(ContextCompat.getColor(requireContext(), accent));
        progress.setTrackColor(ContextCompat.getColor(requireContext(), R.color.line));
        progress.setTrackThickness(dp(8));
        progress.setTrackCornerRadius(dp(8));
        LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        progressParams.setMargins(0, dp(10), 0, dp(10));
        content.addView(progress, progressParams);

        TextView details = new TextView(requireContext());
        details.setText("Água " + aguaMl + " ml • foco " + estudos + " min • checklist " + checklist + "/3 • hábitos " + habitos);
        details.setTextColor(ContextCompat.getColor(requireContext(), R.color.muted));
        details.setTextSize(13f);
        details.setBackground(HabitUi.rounded(requireContext(), R.color.surface, R.color.line, 1, 8));
        details.setPadding(dp(12), dp(10), dp(12), dp(10));
        content.addView(details);

        card.addView(content);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 0, 0, dp(10));
        parent.addView(card, cardParams);
    }

    private String formatDay(int offset) {
        if (offset == 0) return "Hoje";
        if (offset == -1) return "Ontem";

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, offset);
        return new SimpleDateFormat("dd/MM", Locale.getDefault()).format(calendar.getTime());
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
