package com.exemple.habitapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;

import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.List;

public class ProgressoFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_progresso, container, false);

        TextView txtScore = view.findViewById(R.id.txtScoreProgresso);
        TextView txtResumo = view.findViewById(R.id.txtResumo);
        TextView txtEstudos = view.findViewById(R.id.txtEstudos);
        TextView txtSessoes = view.findViewById(R.id.txtSessoes);
        TextView txtChecklist = view.findViewById(R.id.txtChecklist);
        TextView txtInsights = view.findViewById(R.id.txtInsights);
        TextView txtHumor = view.findViewById(R.id.txtHumorProgresso);
        TextView txtEnergia = view.findViewById(R.id.txtEnergiaProgresso);
        TextView txtHabitos = view.findViewById(R.id.txtHabitosProgresso);
        TextView txtResumoSemana = view.findViewById(R.id.txtResumoSemana);
        TextView txtNivelProgresso = view.findViewById(R.id.txtNivelProgresso);
        LinearLayout layoutConquistas = view.findViewById(R.id.layoutConquistas);
        LinearLayout layoutWeekBars = view.findViewById(R.id.layoutWeekBarsProgresso);
        LinearLayout layoutWeekChart = view.findViewById(R.id.layoutWeekChartProgresso);
        LinearProgressIndicator progressAgua = view.findViewById(R.id.progressAgua);
        LinearProgressIndicator progressEstudos = view.findViewById(R.id.progressEstudos);
        LinearProgressIndicator progressChecklist = view.findViewById(R.id.progressChecklist);
        LinearProgressIndicator progressHabitos = view.findViewById(R.id.progressHabitos);

        SharedPreferences prefs = requireActivity().getSharedPreferences("habit_data", Context.MODE_PRIVATE);
        HabitStore.ensureToday(prefs);
        HabitStore.saveTodaySnapshot(prefs);

        double aguaLitros = prefs.getFloat("agua_litros", 0f);
        double metaLitros = prefs.getFloat("meta_litros", 2.0f);
        int estudos = prefs.getInt("estudos_concluidos_min", 0);
        int metaEstudos = prefs.getInt("meta_estudos_min", 60);
        int sessoes = prefs.getInt("sessoes_foco_concluidas", 0);
        int checklist = getChecklistConcluido(prefs);
        List<String> habitos = getCustomHabits(prefs);
        int habitosConcluidos = getHabitosExtrasConcluidos(prefs, habitos);
        int totalAgua = prefs.getInt("total_agua_ml_registrado", 0);
        int totalFoco = prefs.getInt("total_foco_min_registrado", 0);

        int aguaMl = (int) Math.round(aguaLitros * 1000);
        int metaMl = (int) Math.round(metaLitros * 1000);
        int aguaPercent = percentual(aguaMl, metaMl);
        int estudoPercent = percentual(estudos, metaEstudos);
        int checklistPercent = percentual(checklist, 3);
        int habitosPercent = habitos.isEmpty() ? 100 : percentual(habitosConcluidos, habitos.size());
        int score = HabitStore.getTodayScore(prefs);
        int streak = HabitStore.getStreak(prefs);
        int weeklyAverage = HabitStore.getWeeklyAverage(prefs);

        UiAnimator.animatePercentText(txtScore, score);
        txtResumoSemana.setText("Média " + weeklyAverage + "% | sequência " + streak + (streak == 1 ? " dia" : " dias") + " | " + totalFoco + " min foco total");
        txtNivelProgresso.setText("Nível: " + HabitStore.getLevelName(prefs));
        txtResumo.setText(aguaMl + " ml / " + metaMl + " ml");
        txtEstudos.setText(estudos + " min / " + metaEstudos + " min");
        txtSessoes.setText(sessoes + (sessoes == 1 ? " sessão concluída" : " sessões concluídas"));
        txtChecklist.setText(checklist + " de 3 concluídos");
        txtHabitos.setText(habitos.isEmpty()
                ? "Nenhum hábito extra criado"
                : habitosConcluidos + " de " + habitos.size() + " concluídos");
        txtHumor.setText("Humor: " + labelNivel(prefs.getInt("mood_" + HabitStore.todayKey(), -1)));
        txtEnergia.setText("Energia: " + labelNivel(prefs.getInt("energy_" + HabitStore.todayKey(), -1)));
        txtInsights.setText(criarInsight(aguaPercent, estudoPercent, checklistPercent, habitosPercent, habitos.isEmpty()));

        UiAnimator.animateProgress(progressAgua, aguaPercent);
        UiAnimator.animateProgress(progressEstudos, estudoPercent);
        UiAnimator.animateProgress(progressChecklist, checklistPercent);
        UiAnimator.animateProgress(progressHabitos, habitosPercent);
        renderWeekBars(layoutWeekBars, prefs);
        renderWeekChart(layoutWeekChart, prefs);
        renderConquistas(layoutConquistas, score, aguaPercent, estudoPercent, checklistPercent, habitos, habitosConcluidos, sessoes, streak, weeklyAverage, totalAgua, totalFoco);

        return view;
    }

    private int getChecklistConcluido(SharedPreferences prefs) {
        long hoje = HabitStore.todayKey();
        int total = 0;
        if (prefs.getBoolean("check_planejamento_" + hoje, false)) total++;
        if (prefs.getBoolean("check_treino_" + hoje, false)) total++;
        if (prefs.getBoolean("check_sono_" + hoje, false)) total++;
        return total;
    }

    private List<String> getCustomHabits(SharedPreferences prefs) {
        String salvos = prefs.getString("custom_habits", "");
        List<String> habitos = new ArrayList<>();
        if (TextUtils.isEmpty(salvos)) return habitos;

        String[] partes = salvos.split("\\|");
        for (String parte : partes) {
            if (!TextUtils.isEmpty(parte.trim())) {
                habitos.add(parte.trim());
            }
        }
        return habitos;
    }

    private int getHabitosExtrasConcluidos(SharedPreferences prefs, List<String> habitos) {
        int total = 0;
        for (String habito : habitos) {
            if (prefs.getBoolean(getHabitoKey(habito), false)) total++;
        }
        return total;
    }

    private String getHabitoKey(String habito) {
        return "custom_habit_" + HabitStore.todayKey() + "_" + habito.hashCode();
    }

    private void renderConquistas(LinearLayout layout, int score, int agua, int estudo, int checklist, List<String> habitos, int habitosConcluidos, int sessoes, int streak, int weeklyAverage, int totalAgua, int totalFoco) {
        layout.removeAllViews();

        adicionarConquista(layout, agua >= 100, "Meta de água fechada");
        adicionarConquista(layout, estudo >= 100, "Meta de foco fechada");
        adicionarConquista(layout, checklist >= 100, "Checklist completo");
        adicionarConquista(layout, score >= 80, "Score acima de 80%");
        adicionarConquista(layout, sessoes >= 3, "3 sessões de foco no dia");
        adicionarConquista(layout, !habitos.isEmpty() && habitosConcluidos == habitos.size(), "Todos os hábitos extras concluídos");
        adicionarConquista(layout, streak >= 3, "3 dias fortes em sequência");
        adicionarConquista(layout, streak >= 7, "7 dias de consistência");
        adicionarConquista(layout, weeklyAverage >= 70, "Média semanal acima de 70%");
        adicionarConquista(layout, totalAgua >= 10000, "10 litros de água registrados");
        adicionarConquista(layout, totalFoco >= 300, "300 minutos de foco registrados");
    }

    private void adicionarConquista(LinearLayout layout, boolean concluida, String titulo) {
        TextView linha = new TextView(requireContext());
        linha.setText((concluida ? "✓ " : "• ") + titulo);
        linha.setTextColor(ContextCompat.getColor(requireContext(), concluida ? R.color.success : R.color.muted));
        linha.setTextSize(14f);
        linha.setPadding(0, 6, 0, 6);
        layout.addView(linha);
    }

    private void renderWeekChart(LinearLayout layout, SharedPreferences prefs) {
        layout.removeAllViews();
        int[] scores = HabitStore.getWeekScores(prefs);

        for (int i = 0; i < scores.length; i++) {
            LinearLayout column = new LinearLayout(requireContext());
            column.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
            column.setOrientation(LinearLayout.VERTICAL);

            View bar = new View(requireContext());
            int barHeight = dp(Math.max(18, scores[i]));
            GradientDrawable drawable = new GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    new int[]{
                            ContextCompat.getColor(requireContext(), R.color.primary),
                            ContextCompat.getColor(requireContext(), R.color.success)
                    }
            );
            drawable.setCornerRadius(dp(8));
            bar.setBackground(drawable);
            column.addView(bar, new LinearLayout.LayoutParams(dp(22), barHeight));

            TextView label = new TextView(requireContext());
            label.setText(i == 6 ? "Hoje" : "-" + (6 - i));
            label.setTextColor(ContextCompat.getColor(requireContext(), R.color.muted));
            label.setTextSize(11f);
            label.setGravity(Gravity.CENTER);
            label.setPadding(0, dp(8), 0, 0);
            column.addView(label, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
            layout.addView(column, params);
        }
    }

    private void renderWeekBars(LinearLayout layout, SharedPreferences prefs) {
        layout.removeAllViews();
        int[] scores = HabitStore.getWeekScores(prefs);

        for (int i = 0; i < scores.length; i++) {
            LinearLayout row = new LinearLayout(requireContext());
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(0, 4, 0, 4);

            TextView label = new TextView(requireContext());
            label.setText(i == 6 ? "Hoje" : "-" + (6 - i) + "d");
            label.setTextColor(ContextCompat.getColor(requireContext(), R.color.muted));
            label.setTextSize(12f);
            row.addView(label, new LinearLayout.LayoutParams(dp(48), ViewGroup.LayoutParams.WRAP_CONTENT));

            LinearProgressIndicator bar = new LinearProgressIndicator(requireContext());
            bar.setMax(100);
            bar.setProgressCompat(scores[i], false);
            bar.setIndicatorColor(ContextCompat.getColor(requireContext(), R.color.primary));
            bar.setTrackColor(ContextCompat.getColor(requireContext(), R.color.line));
            row.addView(bar, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            TextView value = new TextView(requireContext());
            value.setText(scores[i] + "%");
            value.setGravity(Gravity.END);
            value.setTextColor(ContextCompat.getColor(requireContext(), R.color.ink));
            value.setTextSize(12f);
            row.addView(value, new LinearLayout.LayoutParams(dp(50), ViewGroup.LayoutParams.WRAP_CONTENT));

            layout.addView(row);
        }
    }

    private int percentual(double atual, double meta) {
        if (meta <= 0) return 0;
        return (int) Math.max(0, Math.min(100, Math.round((atual / meta) * 100)));
    }

    private String criarInsight(int agua, int estudo, int checklist, int habitos, boolean semHabitos) {
        if (agua >= 100 && estudo >= 100 && checklist >= 100 && (semHabitos || habitos >= 100)) {
            return "Dia completo. Agora o mais profissional é repetir amanhã.";
        }

        if (agua <= estudo && agua <= checklist && agua <= habitos) {
            return "Seu melhor próximo passo é hidratar. Um copo já muda o painel.";
        }

        if (estudo <= agua && estudo <= checklist && estudo <= habitos) {
            return "Abra uma sessão curta de foco para puxar o score para cima.";
        }

        if (!semHabitos && habitos <= agua && habitos <= estudo && habitos <= checklist) {
            return "Escolha um hábito extra e marque uma vitória pequena agora.";
        }

        return "Feche um item do checklist para transformar intenção em rotina.";
    }

    private String labelNivel(int nivel) {
        if (nivel == 0) return "baixo";
        if (nivel == 1) return "ok";
        if (nivel == 2) return "alto";
        return "ainda não registrado";
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

}
