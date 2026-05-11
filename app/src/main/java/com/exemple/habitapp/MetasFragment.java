package com.exemple.habitapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;

import java.util.Locale;

public class MetasFragment extends Fragment {

    private SharedPreferences prefs;
    private TextView txtMetaAgua;
    private TextView txtMetaEstudo;
    private TextView txtMetaSessao;
    private TextView txtInsight;
    private Slider sliderAgua;
    private Slider sliderEstudo;
    private Slider sliderSessao;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_metas, container, false);
        prefs = requireActivity().getSharedPreferences("habit_data", Context.MODE_PRIVATE);
        HabitStore.ensureToday(prefs);

        txtMetaAgua = view.findViewById(R.id.txtMetaAgua);
        txtMetaEstudo = view.findViewById(R.id.txtMetaEstudo);
        txtMetaSessao = view.findViewById(R.id.txtMetaSessao);
        txtInsight = view.findViewById(R.id.txtMetasInsight);
        sliderAgua = view.findViewById(R.id.sliderAgua);
        sliderEstudo = view.findViewById(R.id.sliderEstudo);
        sliderSessao = view.findViewById(R.id.sliderSessao);
        MaterialButton btnSalvar = view.findViewById(R.id.btnSalvarMetas);

        sliderAgua.setValue(prefs.getFloat("meta_litros", 2.0f));
        sliderEstudo.setValue(prefs.getInt("meta_estudos_min", 60));
        sliderSessao.setValue(prefs.getInt("foco_minutos", 25));

        sliderAgua.addOnChangeListener((slider, value, fromUser) -> atualizarLabels());
        sliderEstudo.addOnChangeListener((slider, value, fromUser) -> atualizarLabels());
        sliderSessao.addOnChangeListener((slider, value, fromUser) -> atualizarLabels());

        btnSalvar.setOnClickListener(v -> salvarMetas());
        atualizarLabels();
        return view;
    }

    private void atualizarLabels() {
        txtMetaAgua.setText(String.format(Locale.getDefault(), "Agua: %.2f L", sliderAgua.getValue()));
        txtMetaEstudo.setText("Foco diario: " + Math.round(sliderEstudo.getValue()) + " min");
        txtMetaSessao.setText("Sessao padrao: " + Math.round(sliderSessao.getValue()) + " min");
        txtInsight.setText(criarInsight());
    }

    private void salvarMetas() {
        prefs.edit()
                .putFloat("meta_litros", sliderAgua.getValue())
                .putInt("meta_estudos_min", Math.round(sliderEstudo.getValue()))
                .putInt("foco_minutos", Math.round(sliderSessao.getValue()))
                .apply();

        HabitStore.saveTodaySnapshot(prefs);
        Toast.makeText(getContext(), "Metas salvas.", Toast.LENGTH_SHORT).show();
    }

    private String criarInsight() {
        if (sliderEstudo.getValue() > 120 || sliderAgua.getValue() > 3.25f) {
            return "Meta alta: mantenha o plano dividido em blocos pequenos.";
        }

        if (sliderSessao.getValue() <= 20) {
            return "Sessoes curtas sao otimas para consistencia diaria.";
        }

        return "Esse conjunto esta equilibrado para evoluir sem pesar demais.";
    }
}
