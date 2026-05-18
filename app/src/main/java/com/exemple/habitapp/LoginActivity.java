package com.exemple.habitapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private TextInputLayout layoutNome;
    private TextInputLayout layoutEmail;
    private TextInputLayout layoutSenha;
    private TextInputEditText inputNome;
    private TextInputEditText inputEmail;
    private TextInputEditText inputSenha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeController.apply(this);
        prefs = getSharedPreferences("habit_data", Context.MODE_PRIVATE);

        if (prefs.getBoolean("perfil_logado", false)) {
            abrirProximaTela();
            return;
        }

        setContentView(R.layout.activity_login);
        layoutNome = findViewById(R.id.layoutLoginNome);
        layoutEmail = findViewById(R.id.layoutLoginEmail);
        layoutSenha = findViewById(R.id.layoutLoginSenha);
        inputNome = findViewById(R.id.inputLoginNome);
        inputEmail = findViewById(R.id.inputLoginEmail);
        inputSenha = findViewById(R.id.inputLoginSenha);
        MaterialButton btnEntrar = findViewById(R.id.btnLoginEntrar);

        inputNome.setText(prefs.getString("nome_usuario", ""));
        inputEmail.setText(prefs.getString("email_usuario", ""));
        btnEntrar.setOnClickListener(v -> salvarLogin());
        UiAnimator.enter(findViewById(R.id.rootLogin));
    }

    private void salvarLogin() {
        layoutNome.setError(null);
        layoutEmail.setError(null);
        layoutSenha.setError(null);

        String nome = text(inputNome);
        String email = text(inputEmail);
        String senha = text(inputSenha);

        if (TextUtils.isEmpty(nome)) {
            layoutNome.setError("Digite seu nome.");
            return;
        }

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            layoutEmail.setError("Digite um e-mail valido.");
            return;
        }

        if (!TextUtils.isEmpty(senha) && senha.length() < 4) {
            layoutSenha.setError("Use pelo menos 4 caracteres.");
            return;
        }

        prefs.edit()
                .putBoolean("perfil_logado", true)
                .putString("nome_usuario", nome)
                .putString("email_usuario", email)
                .putBoolean("perfil_tem_senha", !TextUtils.isEmpty(senha))
                .apply();

        abrirProximaTela();
    }

    private void abrirProximaTela() {
        startActivity(new Intent(this, OnboardingActivity.class));
        finish();
    }

    private String text(TextInputEditText input) {
        return input.getText() != null ? input.getText().toString().trim() : "";
    }
}
