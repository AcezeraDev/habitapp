package com.exemple.habitapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.Locale;

public class RotinaFragment extends Fragment {

    private SharedPreferences prefs;
    private TextView txtResumo;
    private TextView txtScore;
    private TextView txtPlano;
    private LinearProgressIndicator progressRotina;
    private MaterialButton btnManha;
    private MaterialButton btnTarde;
    private MaterialButton btnNoite;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rotina, container, false);
        prefs = requireActivity().getSharedPreferences("habit_data", Context.MODE_PRIVATE);
        HabitStore.ensureToday(prefs);

        txtResumo = view.findViewById(R.id.txtRotinaResumo);
        txtScore = view.findViewById(R.id.txtRotinaScore);
        txtPlano = view.findViewById(R.id.txtRotinaPlano);
        progressRotina = view.findViewById(R.id.progressRotina);

        configurarPeriodo(view);
        configurarChecks(view);
        atualizarTela();
        return view;
    }

    private void configurarPeriodo(View view) {
        btnManha = view.findViewById(R.id.btnRotinaManha);
        btnTarde = view.findViewById(R.id.btnRotinaTarde);
        btnNoite = view.findViewById(R.id.btnRotinaNoite);

        btnManha.setOnClickListener(v -> salvarPeriodo("Manhã"));
        btnTarde.setOnClickListener(v -> salvarPeriodo("Tarde"));
        btnNoite.setOnClickListener(v -> salvarPeriodo("Noite"));

        atualizarPeriodo();
    }

    private void configurarChecks(View view) {
        configurarCheck(view.findViewById(R.id.checkRotinaManha), "rotina_bloco_manha_");
        configurarCheck(view.findViewById(R.id.checkRotinaAlimentacao), "rotina_bloco_alimentacao_");
        configurarCheck(view.findViewById(R.id.checkRotinaTreino), "rotina_bloco_treino_");
        configurarCheck(view.findViewById(R.id.checkRotinaSono), "rotina_bloco_sono_");
    }

    private void configurarCheck(CheckBox checkBox, String keyPrefix) {
        checkBox.setChecked(prefs.getBoolean(keyPrefix + HabitStore.todayKey(), false));
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(keyPrefix + HabitStore.todayKey(), isChecked).apply();
            atualizarTela();
        });
    }

    private void salvarPeriodo(String periodo) {
        prefs.edit().putString("rotina_periodo_principal", periodo).apply();
        Toast.makeText(getContext(), "Período definido: " + periodo, Toast.LENGTH_SHORT).show();
        atualizarPeriodo();
        atualizarTela();
    }

    private void atualizarPeriodo() {
        String periodo = prefs.getString("rotina_periodo_principal", prefs.getString("melhor_horario", "Manhã"));
        btnManha.setAlpha(("Manhã".equals(periodo) || "Manha".equals(periodo)) ? 1f : 0.55f);
        btnTarde.setAlpha("Tarde".equals(periodo) ? 1f : 0.55f);
        btnNoite.setAlpha("Noite".equals(periodo) ? 1f : 0.55f);
    }

    private void atualizarTela() {
        int concluidos = getRotinaConcluida();
        int score = HabitStore.percent(concluidos, 4);
        String periodo = prefs.getString("rotina_periodo_principal", prefs.getString("melhor_horario", "Manhã"));

        txtResumo.setText(concluidos + " de 4 blocos concluídos no período " + periodo.toLowerCase(Locale.ROOT) + ".");
        UiAnimator.animatePercentText(txtScore, score);
        UiAnimator.animateProgress(progressRotina, score);
        txtPlano.setText(criarPlano(periodo, concluidos));
    }

    private int getRotinaConcluida() {
        long hoje = HabitStore.todayKey();
        int total = 0;
        if (prefs.getBoolean("rotina_bloco_manha_" + hoje, false)) total++;
        if (prefs.getBoolean("rotina_bloco_alimentacao_" + hoje, false)) total++;
        if (prefs.getBoolean("rotina_bloco_treino_" + hoje, false)) total++;
        if (prefs.getBoolean("rotina_bloco_sono_" + hoje, false)) total++;
        return total;
    }

    private String criarPlano(String periodo, int concluidos) {
        if (concluidos == 0) {
            return "Comece pequeno: escolha um bloco de 10 minutos para ativar o dia.";
        }

        if (concluidos < 4) {
            return "Foque no próximo bloco do período " + periodo.toLowerCase(Locale.ROOT) + ". Rotina forte nasce de repetição simples.";
        }

        return "Rotina fechada. Agora mantenha o ambiente pronto para repetir amanhã.";
    }
}
