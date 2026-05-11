package com.exemple.habitapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class OnboardingActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private TextInputEditText inputNome;
    private TextInputEditText inputAguaAtual;
    private TextInputEditText inputMetaAgua;
    private TextInputEditText inputMetaEstudos;
    private TextInputEditText inputFoco;
    private TextInputEditText inputDesafio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences("habit_data", Context.MODE_PRIVATE);

        if (prefs.getLong("daily_setup_day", -1) == getTodayKey()) {
            abrirApp();
            return;
        }

        setContentView(R.layout.activity_onboarding);

        inputNome = findViewById(R.id.inputNome);
        inputAguaAtual = findViewById(R.id.inputAguaAtual);
        inputMetaAgua = findViewById(R.id.inputMetaAgua);
        inputMetaEstudos = findViewById(R.id.inputMetaEstudos);
        inputFoco = findViewById(R.id.inputFoco);
        inputDesafio = findViewById(R.id.inputDesafio);
        MaterialButton btnSalvar = findViewById(R.id.btnSalvarConfig);

        preencherValores();
        btnSalvar.setOnClickListener(v -> salvarConfiguracao());
    }

    private void preencherValores() {
        inputNome.setText(prefs.getString("nome_usuario", ""));
        inputAguaAtual.setText("0");
        inputMetaAgua.setText(String.valueOf(prefs.getFloat("meta_litros", 2.0f)));
        inputMetaEstudos.setText(String.valueOf(prefs.getInt("meta_estudos_min", 60)));
        inputFoco.setText(String.valueOf(prefs.getInt("foco_minutos", 25)));
        inputDesafio.setText(String.valueOf(prefs.getInt("challenge_goal_days", 30)));
    }

    private void salvarConfiguracao() {
        String nome = texto(inputNome);
        double aguaAtual = lerDouble(inputAguaAtual, 0.0);
        double metaAgua = lerDouble(inputMetaAgua, 2.0);
        int metaEstudos = lerInt(inputMetaEstudos, 60);
        int focoMinutos = lerInt(inputFoco, 25);
        int diasDesafio = lerInt(inputDesafio, 30);

        if (metaAgua <= 0 || metaEstudos <= 0 || focoMinutos <= 0 || diasDesafio <= 0) {
            Toast.makeText(this, "Preencha metas maiores que zero.", Toast.LENGTH_SHORT).show();
            return;
        }

        prefs.edit()
                .putString("nome_usuario", TextUtils.isEmpty(nome) ? "Guerreiro" : nome)
                .putFloat("agua_litros", (float) Math.max(0, aguaAtual))
                .putFloat("meta_litros", (float) metaAgua)
                .putInt("meta_estudos_min", metaEstudos)
                .putInt("foco_minutos", focoMinutos)
                .putInt("challenge_goal_days", diasDesafio)
                .putLong("daily_setup_day", getTodayKey())
                .apply();

        abrirApp();
    }

    private String texto(TextInputEditText input) {
        return input.getText() != null ? input.getText().toString().trim() : "";
    }

    private double lerDouble(TextInputEditText input, double padrao) {
        try {
            String valor = texto(input).replace(",", ".");
            return TextUtils.isEmpty(valor) ? padrao : Double.parseDouble(valor);
        } catch (NumberFormatException e) {
            return padrao;
        }
    }

    private int lerInt(TextInputEditText input, int padrao) {
        try {
            String valor = texto(input);
            return TextUtils.isEmpty(valor) ? padrao : Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            return padrao;
        }
    }

    private long getTodayKey() {
        return System.currentTimeMillis() / (1000L * 60 * 60 * 24);
    }

    private void abrirApp() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
