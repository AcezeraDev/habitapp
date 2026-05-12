package com.exemple.habitapp;

import android.content.res.ColorStateList;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class OnboardingActivity extends AppCompatActivity {

    private static final int TOTAL_STEPS = 8;
    private static final int ONBOARDING_VERSION = 2;

    private SharedPreferences prefs;
    private int step = 0;

    private String nome = "";
    private String objetivo = "Mais disciplina";
    private String rotina = "Equilibrada";
    private String horario = "Manhã";
    private double aguaAtual = 0.0;
    private double metaAgua = 2.0;
    private int metaEstudos = 60;
    private int focoMinutos = 25;
    private int diasDesafio = 30;

    private ProgressBar progressOnboarding;
    private TextView txtStep;
    private TextView txtQuestion;
    private TextView txtSubtitle;
    private TextInputLayout inputLayout;
    private TextInputEditText inputAnswer;
    private MaterialButton optionOne;
    private MaterialButton optionTwo;
    private MaterialButton optionThree;
    private MaterialButton btnBack;
    private MaterialButton btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeController.apply(this);

        prefs = getSharedPreferences("habit_data", Context.MODE_PRIVATE);

        boolean setupDeHoje = prefs.getLong("daily_setup_day", -1) == HabitStore.todayKey();
        boolean fluxoAtualizado = prefs.getInt("onboarding_version", 0) == ONBOARDING_VERSION;

        if (setupDeHoje && fluxoAtualizado) {
            abrirApp();
            return;
        }

        setContentView(R.layout.activity_onboarding);
        bindViews();
        preencherValoresSalvos();
        renderStep();
    }

    private void bindViews() {
        progressOnboarding = findViewById(R.id.progressOnboarding);
        txtStep = findViewById(R.id.txtStep);
        txtQuestion = findViewById(R.id.txtQuestion);
        txtSubtitle = findViewById(R.id.txtSubtitle);
        inputLayout = findViewById(R.id.inputLayout);
        inputAnswer = findViewById(R.id.inputAnswer);
        optionOne = findViewById(R.id.optionOne);
        optionTwo = findViewById(R.id.optionTwo);
        optionThree = findViewById(R.id.optionThree);
        btnBack = findViewById(R.id.btnBack);
        btnNext = findViewById(R.id.btnNext);

        optionOne.setOnClickListener(v -> selecionarOpcao(0));
        optionTwo.setOnClickListener(v -> selecionarOpcao(1));
        optionThree.setOnClickListener(v -> selecionarOpcao(2));

        btnBack.setOnClickListener(v -> {
            salvarRespostaAtual();
            if (step > 0) {
                step--;
                renderStep();
            }
        });

        btnNext.setOnClickListener(v -> {
            if (!salvarRespostaAtual()) return;

            if (step == TOTAL_STEPS - 1) {
                salvarConfiguracao();
            } else {
                step++;
                renderStep();
            }
        });
    }

    private void preencherValoresSalvos() {
        nome = prefs.getString("nome_usuario", "");
        metaAgua = prefs.getFloat("meta_litros", 2.0f);
        metaEstudos = prefs.getInt("meta_estudos_min", 60);
        focoMinutos = prefs.getInt("foco_minutos", 25);
        diasDesafio = prefs.getInt("challenge_goal_days", 30);
    }

    private void renderStep() {
        progressOnboarding.setMax(TOTAL_STEPS);
        progressOnboarding.setProgress(step + 1);
        txtStep.setText("Passo " + (step + 1) + " de " + TOTAL_STEPS);
        btnBack.setVisibility(step == 0 ? View.INVISIBLE : View.VISIBLE);
        btnNext.setText(step == TOTAL_STEPS - 1 ? "Criar minha rotina" : "Continuar");
        inputAnswer.setText("");
        inputLayout.setVisibility(View.GONE);

        optionOne.setVisibility(View.GONE);
        optionTwo.setVisibility(View.GONE);
        optionThree.setVisibility(View.GONE);

        if (step == 0) {
            txtQuestion.setText("Como você quer ser chamado?");
            txtSubtitle.setText("Vamos personalizar sua rotina diária com um toque mais seu.");
            mostrarInput(nome, "Digite seu nome");
        } else if (step == 1) {
            txtQuestion.setText("Qual e seu foco principal agora?");
            txtSubtitle.setText("Escolha o objetivo que mais combina com sua fase.");
            mostrarOpcoes(objetivo, "Mais disciplina", "Beber mais água", "Estudar melhor");
        } else if (step == 2) {
            txtQuestion.setText("Como esta sua rotina hoje?");
            txtSubtitle.setText("Isso ajuda o HabitApp a montar metas mais realistas.");
            mostrarOpcoes(rotina, "Corrida", "Equilibrada", "Tranquila");
        } else if (step == 3) {
            txtQuestion.setText("Em qual período você rende melhor?");
            txtSubtitle.setText("Seu foco pode começar no melhor horário do seu dia.");
            mostrarOpcoes(horario, "Manhã", "Tarde", "Noite");
        } else if (step == 4) {
            txtQuestion.setText("Quantos litros de água você já bebeu hoje?");
            txtSubtitle.setText("Pode ser aproximado. Exemplo: 0.5, 1 ou 1.5.");
            mostrarInput(String.valueOf(aguaAtual), "Litros bebidos hoje");
        } else if (step == 5) {
            txtQuestion.setText("Qual meta de água parece boa para hoje?");
            txtSubtitle.setText("Uma meta simples e clara fica mais fácil de cumprir.");
            mostrarOpcoes(String.valueOf(metaAgua), "1.5 L", "2.0 L", "2.5 L");
        } else if (step == 6) {
            txtQuestion.setText("Quanto tempo você quer estudar hoje?");
            txtSubtitle.setText("O app vai usar isso no resumo e no progresso diário.");
            mostrarOpcoes(String.valueOf(metaEstudos), "30 min", "60 min", "90 min");
        } else {
            txtQuestion.setText("Qual sessão de foco combina com você?");
            txtSubtitle.setText("Também vamos manter o desafio de fotos em 30 dias.");
            mostrarOpcoes(String.valueOf(focoMinutos), "15 min", "25 min", "45 min");
        }
    }

    private void mostrarInput(String valor, String hint) {
        inputAnswer.setVisibility(View.VISIBLE);
        inputLayout.setVisibility(View.VISIBLE);
        inputAnswer.setHint(hint);
        inputAnswer.setText(valor);
        inputAnswer.requestFocus();
    }

    private void mostrarOpcoes(String atual, String primeira, String segunda, String terceira) {
        configurarOpcao(optionOne, primeira, atual);
        configurarOpcao(optionTwo, segunda, atual);
        configurarOpcao(optionThree, terceira, atual);
    }

    private void configurarOpcao(MaterialButton button, String texto, String atual) {
        button.setVisibility(View.VISIBLE);
        button.setText(texto);
        boolean selecionado = texto.equals(atual)
                || texto.startsWith(atual + " ")
                || atual.startsWith(texto.replace(" L", "").replace(" min", ""));
        button.setStrokeWidth(selecionado ? 4 : 1);
        int background = ContextCompat.getColor(this, selecionado ? R.color.onboarding_selected : R.color.onboarding_option);
        int text = ContextCompat.getColor(this, selecionado ? R.color.onboarding_selected_text : R.color.onboarding_option_text);
        button.setBackgroundTintList(ColorStateList.valueOf(background));
        button.setTextColor(text);
    }

    private void selecionarOpcao(int index) {
        String valor = index == 0 ? optionOne.getText().toString()
                : index == 1 ? optionTwo.getText().toString()
                : optionThree.getText().toString();

        if (step == 1) objetivo = valor;
        if (step == 2) rotina = valor;
        if (step == 3) horario = valor;
        if (step == 5) metaAgua = extrairDouble(valor, metaAgua);
        if (step == 6) metaEstudos = extrairInt(valor, metaEstudos);
        if (step == 7) focoMinutos = extrairInt(valor, focoMinutos);

        renderStep();
    }

    private boolean salvarRespostaAtual() {
        String resposta = texto(inputAnswer);

        if (step == 0) {
            nome = resposta;
        } else if (step == 4) {
            Double litros = TextUtils.isEmpty(resposta) ? 0.0 : parseDoubleOrNull(resposta);
            if (litros == null || litros < 0) {
                Toast.makeText(this, "Informe um valor válido.", Toast.LENGTH_SHORT).show();
                return false;
            }
            aguaAtual = litros;
        }

        return true;
    }

    private void salvarConfiguracao() {
        if (metaAgua <= 0 || metaEstudos <= 0 || focoMinutos <= 0) {
            Toast.makeText(this, "Escolha metas maiores que zero.", Toast.LENGTH_SHORT).show();
            return;
        }

        prefs.edit()
                .putString("nome_usuario", TextUtils.isEmpty(nome) ? "Guerreiro" : nome)
                .putString("objetivo_principal", objetivo)
                .putString("ritmo_rotina", rotina)
                .putString("melhor_horario", horario)
                .putFloat("agua_litros", (float) Math.max(0, aguaAtual))
                .putFloat("meta_litros", (float) metaAgua)
                .putInt("meta_estudos_min", metaEstudos)
                .putInt("foco_minutos", focoMinutos)
                .putInt("challenge_goal_days", diasDesafio)
                .putInt("onboarding_version", ONBOARDING_VERSION)
                .putLong("daily_setup_day", HabitStore.todayKey())
                .apply();

        abrirApp();
    }

    private String texto(TextInputEditText input) {
        return input.getText() != null ? input.getText().toString().trim() : "";
    }

    private double lerDouble(String valor, double padrao) {
        Double parsed = parseDoubleOrNull(valor);
        return parsed != null ? parsed : padrao;
    }

    private Double parseDoubleOrNull(String valor) {
        try {
            String limpo = valor.trim().replace(",", ".");
            return TextUtils.isEmpty(limpo) ? null : Double.parseDouble(limpo);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private double extrairDouble(String valor, double padrao) {
        return lerDouble(valor.replace("L", "").trim(), padrao);
    }

    private int extrairInt(String valor, int padrao) {
        try {
            return Integer.parseInt(valor.replace("min", "").trim());
        } catch (NumberFormatException e) {
            return padrao;
        }
    }

    private void abrirApp() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
