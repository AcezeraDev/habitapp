package com.exemple.habitapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Locale;

public class OnboardingActivity extends AppCompatActivity {

    private static final int TOTAL_STEPS = 8;
    private static final int ONBOARDING_VERSION = 3;

    private SharedPreferences prefs;
    private int step = 0;

    private String nome = "";
    private String objetivo = "Mais disciplina";
    private String rotina = "Equilibrada";
    private String horario = "Manha";
    private double aguaAtual = 0.0;
    private double metaAgua = 2.0;
    private int metaEstudos = 60;
    private int focoMinutos = 25;
    private int diasDesafio = 30;

    private ProgressBar progressOnboarding;
    private TextView txtStep;
    private TextView txtQuestion;
    private TextView txtSubtitle;
    private TextView txtOnboardingHint;
    private ImageView imgQuestionIcon;
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
        UiAnimator.enter(findViewById(R.id.rootOnboarding));
    }

    private void bindViews() {
        progressOnboarding = findViewById(R.id.progressOnboarding);
        txtStep = findViewById(R.id.txtStep);
        txtQuestion = findViewById(R.id.txtQuestion);
        txtSubtitle = findViewById(R.id.txtSubtitle);
        txtOnboardingHint = findViewById(R.id.txtOnboardingHint);
        imgQuestionIcon = findViewById(R.id.imgQuestionIcon);
        inputLayout = findViewById(R.id.inputLayout);
        inputAnswer = findViewById(R.id.inputAnswer);
        optionOne = findViewById(R.id.optionOne);
        optionTwo = findViewById(R.id.optionTwo);
        optionThree = findViewById(R.id.optionThree);
        btnBack = findViewById(R.id.btnBack);
        btnNext = findViewById(R.id.btnNext);

        optionOne.setOnClickListener(v -> selecionarOpcao(0, v));
        optionTwo.setOnClickListener(v -> selecionarOpcao(1, v));
        optionThree.setOnClickListener(v -> selecionarOpcao(2, v));

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
        txtStep.setText((step + 1) + " / " + TOTAL_STEPS);
        txtOnboardingHint.setText(hintForStep());
        imgQuestionIcon.setImageResource(iconForStep());
        imgQuestionIcon.setImageTintList(iconTintForStep());
        btnBack.setVisibility(step == 0 ? View.INVISIBLE : View.VISIBLE);
        btnNext.setText(step == TOTAL_STEPS - 1 ? "Criar minha rotina" : "Continuar");
        btnNext.setIconResource(step == TOTAL_STEPS - 1 ? R.drawable.check_circle : R.drawable.ic_nav_goals);
        inputLayout.setError(null);
        inputAnswer.setText("");
        inputLayout.setVisibility(View.GONE);

        optionOne.setVisibility(View.GONE);
        optionTwo.setVisibility(View.GONE);
        optionThree.setVisibility(View.GONE);

        if (step == 0) {
            txtQuestion.setText("Como voce quer ser chamado?");
            txtSubtitle.setText("Seu painel vai usar esse nome para deixar tudo mais pessoal.");
            mostrarInput(nome, "Digite seu nome", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        } else if (step == 1) {
            txtQuestion.setText("Qual e seu foco principal agora?");
            txtSubtitle.setText("Escolha o objetivo que mais combina com sua fase atual.");
            mostrarOpcoes(objetivo, "Mais disciplina", "Beber mais agua", "Estudar melhor");
        } else if (step == 2) {
            txtQuestion.setText("Como esta sua rotina hoje?");
            txtSubtitle.setText("Metas boas precisam caber no dia real, nao no dia perfeito.");
            mostrarOpcoes(rotina, "Corrida", "Equilibrada", "Tranquila");
        } else if (step == 3) {
            txtQuestion.setText("Em qual periodo voce rende melhor?");
            txtSubtitle.setText("O app usa isso para sugerir seu melhor bloco de foco.");
            mostrarOpcoes(horario, "Manha", "Tarde", "Noite");
        } else if (step == 4) {
            txtQuestion.setText("Quanta agua voce ja bebeu hoje?");
            txtSubtitle.setText("Pode ser aproximado. Exemplos: 0.5, 1 ou 1.5 litro.");
            mostrarInput(String.valueOf(aguaAtual), "Litros bebidos hoje", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        } else if (step == 5) {
            txtQuestion.setText("Qual meta de agua parece boa?");
            txtSubtitle.setText("Uma meta clara deixa o acompanhamento mais leve.");
            mostrarOpcoes(String.valueOf(metaAgua), "1.5 L", "2.0 L", "2.5 L");
        } else if (step == 6) {
            txtQuestion.setText("Quanto tempo voce quer estudar hoje?");
            txtSubtitle.setText("Esse numero aparece no resumo diario e no progresso.");
            mostrarOpcoes(String.valueOf(metaEstudos), "30 min", "60 min", "90 min");
        } else {
            txtQuestion.setText("Qual sessao de foco combina com voce?");
            txtSubtitle.setText("Escolha um bloco que pareca sustentavel hoje.");
            mostrarOpcoes(String.valueOf(focoMinutos), "15 min", "25 min", "45 min");
        }
    }

    private void mostrarInput(String valor, String hint, int inputType) {
        inputAnswer.setVisibility(View.VISIBLE);
        inputLayout.setVisibility(View.VISIBLE);
        inputLayout.setHint(hint);
        inputLayout.setStartIconDrawable(iconForStep());
        inputLayout.setStartIconTintList(iconTintForStep());
        inputAnswer.setInputType(inputType);
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
        button.setIconResource(iconForOption(texto));
        button.setIconSize(HabitUi.dp(this, 24));
        button.setGravity(Gravity.CENTER_VERTICAL);
        button.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);

        boolean selecionado = texto.equals(atual)
                || texto.startsWith(atual + " ")
                || atual.startsWith(texto.replace(" L", "").replace(" min", ""));
        int background = ContextCompat.getColor(this, selecionado ? R.color.onboarding_selected : R.color.onboarding_option);
        int text = ContextCompat.getColor(this, selecionado ? R.color.onboarding_selected_text : R.color.ink);
        int stroke = ContextCompat.getColor(this, selecionado ? R.color.primary : R.color.line);

        button.setStrokeWidth(HabitUi.dp(this, selecionado ? 2 : 1));
        button.setStrokeColor(ColorStateList.valueOf(stroke));
        button.setBackgroundTintList(ColorStateList.valueOf(background));
        button.setTextColor(text);
        button.setIconTint(ColorStateList.valueOf(text));
    }

    private void selecionarOpcao(int index, View source) {
        String valor = index == 0 ? optionOne.getText().toString()
                : index == 1 ? optionTwo.getText().toString()
                : optionThree.getText().toString();

        if (step == 1) objetivo = valor;
        if (step == 2) rotina = valor;
        if (step == 3) horario = valor;
        if (step == 5) metaAgua = extrairDouble(valor, metaAgua);
        if (step == 6) metaEstudos = extrairInt(valor, metaEstudos);
        if (step == 7) focoMinutos = extrairInt(valor, focoMinutos);

        UiAnimator.pulse(source);
        renderStep();
    }

    private boolean salvarRespostaAtual() {
        String resposta = texto(inputAnswer);
        inputLayout.setError(null);

        if (step == 0) {
            nome = resposta;
        } else if (step == 4) {
            Double litros = TextUtils.isEmpty(resposta) ? 0.0 : parseDoubleOrNull(resposta);
            if (litros == null || litros < 0) {
                inputLayout.setError("Informe um valor valido.");
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

        HabitStore.ensureStarterHabits(prefs, objetivo);
        ReminderScheduler.scheduleDefaultReminders(this);
        abrirApp();
    }

    private int iconForStep() {
        if (step == 0) return R.drawable.ic_nav_profile;
        if (step == 1) return R.drawable.ic_nav_goals;
        if (step == 2) return R.drawable.ic_nav_routine;
        if (step == 3) return R.drawable.ic_clock_history;
        if (step == 4 || step == 5) return R.drawable.ic_nav_water;
        if (step == 6) return R.drawable.ic_nav_focus;
        return R.drawable.ic_clock_history;
    }

    private ColorStateList iconTintForStep() {
        int colorRes = R.color.primary;
        if (step == 1 || step == 7) colorRes = R.color.coral;
        if (step == 4 || step == 5) colorRes = R.color.water;
        if (step == 6) colorRes = R.color.study;
        return ColorStateList.valueOf(ContextCompat.getColor(this, colorRes));
    }

    private int iconForOption(String option) {
        String value = option.toLowerCase(Locale.ROOT);
        if (value.contains("agua") || value.endsWith(" l")) return R.drawable.ic_nav_water;
        if (value.contains("estudar") || value.contains("min")) return R.drawable.ic_nav_focus;
        if (value.contains("disciplina")) return R.drawable.ic_nav_goals;
        if (value.contains("corrida")) return R.drawable.ic_nav_routine;
        if (value.contains("equilibrada")) return R.drawable.ic_theme_palette;
        if (value.contains("tranquila")) return R.drawable.ic_nav_profile;
        if (value.contains("manha")) return R.drawable.ic_clock_history;
        if (value.contains("tarde")) return R.drawable.ic_theme_palette;
        if (value.contains("noite")) return R.drawable.ic_nav_focus;
        return R.drawable.check_circle;
    }

    private String hintForStep() {
        if (step == 0) return "Comece pelo basico: seu nome aparece na saudacao da tela inicial.";
        if (step == 1) return "Esse foco ajuda o app a criar habitos iniciais mais relevantes.";
        if (step == 2) return "Use a resposta mais honesta. Rotina leve tambem conta.";
        if (step == 3) return "O melhor horario vira sugestao no plano inteligente da home.";
        if (step == 4) return "Nao precisa ser perfeito. Um valor aproximado ja ajuda o painel.";
        if (step == 5) return "Voce pode mudar a meta de agua depois na tela de hidratacao.";
        if (step == 6) return "Escolha uma meta possivel para criar consistencia.";
        return "Blocos menores vencem quando o dia esta cheio.";
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
