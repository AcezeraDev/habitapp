package com.exemple.habitapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

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
        LinearLayout layoutConquistas = view.findViewById(R.id.layoutConquistas);
        LinearProgressIndicator progressAgua = view.findViewById(R.id.progressAgua);
        LinearProgressIndicator progressEstudos = view.findViewById(R.id.progressEstudos);
        LinearProgressIndicator progressChecklist = view.findViewById(R.id.progressChecklist);
        LinearProgressIndicator progressHabitos = view.findViewById(R.id.progressHabitos);

        SharedPreferences prefs = requireActivity().getSharedPreferences("habit_data", Context.MODE_PRIVATE);

        double aguaLitros = prefs.getFloat("agua_litros", 0f);
        double metaLitros = prefs.getFloat("meta_litros", 2.0f);
        int estudos = prefs.getInt("estudos_concluidos_min", 0);
        int metaEstudos = prefs.getInt("meta_estudos_min", 60);
        int sessoes = prefs.getInt("sessoes_foco_concluidas", 0);
        int checklist = getChecklistConcluido(prefs);
        List<String> habitos = getCustomHabits(prefs);
        int habitosConcluidos = getHabitosExtrasConcluidos(prefs, habitos);

        int aguaMl = (int) Math.round(aguaLitros * 1000);
        int metaMl = (int) Math.round(metaLitros * 1000);
        int aguaPercent = percentual(aguaMl, metaMl);
        int estudoPercent = percentual(estudos, metaEstudos);
        int checklistPercent = percentual(checklist, 3);
        int habitosPercent = habitos.isEmpty() ? 100 : percentual(habitosConcluidos, habitos.size());
        int score = habitos.isEmpty()
                ? (aguaPercent + estudoPercent + checklistPercent) / 3
                : (aguaPercent + estudoPercent + checklistPercent + habitosPercent) / 4;

        txtScore.setText(score + "%");
        txtResumo.setText(aguaMl + " ml / " + metaMl + " ml");
        txtEstudos.setText(estudos + " min / " + metaEstudos + " min");
        txtSessoes.setText(sessoes + (sessoes == 1 ? " sessao concluida" : " sessoes concluidas"));
        txtChecklist.setText(checklist + " de 3 concluidos");
        txtHabitos.setText(habitos.isEmpty()
                ? "Nenhum habito extra criado"
                : habitosConcluidos + " de " + habitos.size() + " concluidos");
        txtHumor.setText("Humor: " + labelNivel(prefs.getInt("mood_" + getTodayKey(), -1)));
        txtEnergia.setText("Energia: " + labelNivel(prefs.getInt("energy_" + getTodayKey(), -1)));
        txtInsights.setText(criarInsight(aguaPercent, estudoPercent, checklistPercent, habitosPercent, habitos.isEmpty()));

        progressAgua.setProgressCompat(aguaPercent, true);
        progressEstudos.setProgressCompat(estudoPercent, true);
        progressChecklist.setProgressCompat(checklistPercent, true);
        progressHabitos.setProgressCompat(habitosPercent, true);
        renderConquistas(layoutConquistas, score, aguaPercent, estudoPercent, checklistPercent, habitos, habitosConcluidos, sessoes);

        return view;
    }

    private int getChecklistConcluido(SharedPreferences prefs) {
        long hoje = getTodayKey();
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
        return "custom_habit_" + getTodayKey() + "_" + habito.hashCode();
    }

    private void renderConquistas(LinearLayout layout, int score, int agua, int estudo, int checklist, List<String> habitos, int habitosConcluidos, int sessoes) {
        layout.removeAllViews();

        adicionarConquista(layout, agua >= 100, "Meta de agua fechada");
        adicionarConquista(layout, estudo >= 100, "Meta de foco fechada");
        adicionarConquista(layout, checklist >= 100, "Checklist completo");
        adicionarConquista(layout, score >= 80, "Score acima de 80%");
        adicionarConquista(layout, sessoes >= 3, "3 sessoes de foco no dia");
        adicionarConquista(layout, !habitos.isEmpty() && habitosConcluidos == habitos.size(), "Todos os habitos extras concluidos");
    }

    private void adicionarConquista(LinearLayout layout, boolean concluida, String titulo) {
        TextView linha = new TextView(requireContext());
        linha.setText((concluida ? "[ok] " : "[ ] ") + titulo);
        linha.setTextColor(concluida ? 0xFF10B981 : 0xFF64748B);
        linha.setTextSize(14f);
        linha.setPadding(0, 6, 0, 6);
        layout.addView(linha);
    }

    private int percentual(double atual, double meta) {
        if (meta <= 0) return 0;
        return (int) Math.max(0, Math.min(100, Math.round((atual / meta) * 100)));
    }

    private String criarInsight(int agua, int estudo, int checklist, int habitos, boolean semHabitos) {
        if (agua >= 100 && estudo >= 100 && checklist >= 100 && (semHabitos || habitos >= 100)) {
            return "Dia completo. Agora o mais profissional e repetir amanha.";
        }

        if (agua <= estudo && agua <= checklist && agua <= habitos) {
            return "Seu melhor proximo passo e hidratar. Um copo ja muda o painel.";
        }

        if (estudo <= agua && estudo <= checklist && estudo <= habitos) {
            return "Abra uma sessao curta de foco para puxar o score para cima.";
        }

        if (!semHabitos && habitos <= agua && habitos <= estudo && habitos <= checklist) {
            return "Escolha um habito extra e marque uma vitoria pequena agora.";
        }

        return "Feche um item do checklist para transformar intencao em rotina.";
    }

    private String labelNivel(int nivel) {
        if (nivel == 0) return "baixo";
        if (nivel == 1) return "ok";
        if (nivel == 2) return "alto";
        return "ainda nao registrado";
    }

    private long getTodayKey() {
        return System.currentTimeMillis() / (1000L * 60 * 60 * 24);
    }
}
