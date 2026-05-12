package com.exemple.habitapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeController.apply(this);
        setContentView(R.layout.activity_splash);

        View logo = findViewById(R.id.imgSplashLogo);
        logo.setScaleX(0.86f);
        logo.setScaleY(0.86f);
        logo.setAlpha(0f);
        logo.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(420)
                .start();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            SharedPreferences prefs = getSharedPreferences("habit_data", MODE_PRIVATE);
            Class<?> destino = prefs.getBoolean("perfil_logado", false)
                    ? OnboardingActivity.class
                    : LoginActivity.class;
            startActivity(new Intent(this, destino));
            finish();
        }, 850);
    }
}
