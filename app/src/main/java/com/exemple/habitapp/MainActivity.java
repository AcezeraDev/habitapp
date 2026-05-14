package com.exemple.habitapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    private static final String STATE_ITEM_ID = "current_item_id";

    private FrameLayout frameContainer;
    private LinearLayout bottomNavContainer;
    private int currentItemId = R.id.home;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeController.apply(this);
        setContentView(R.layout.activity_main);
        HabitStore.ensureToday(getSharedPreferences("habit_data", MODE_PRIVATE));
        HabitWidgetProvider.updateAll(this);
        NotificationHelper.createChannels(this);
        ReminderScheduler.scheduleDefaultReminders(this);
        requestNotificationPermissionIfNeeded();

        frameContainer = findViewById(R.id.frame_container);
        bottomNavContainer = findViewById(R.id.bottom_nav_container);
        configurarNavegacaoInferior();

        int requestedItem = getIntent() != null ? getIntent().getIntExtra("nav_target", R.id.home) : R.id.home;
        int initialItem = savedInstanceState != null
                ? savedInstanceState.getInt(STATE_ITEM_ID, R.id.home)
                : requestedItem;
        navigateTo(initialItem);
    }

    @Override
    protected void onResume() {
        super.onResume();
        HabitWidgetProvider.updateAll(this);
    }

    @Override
    protected void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        int target = intent != null ? intent.getIntExtra("nav_target", currentItemId) : currentItemId;
        navigateTo(target);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_ITEM_ID, currentItemId);
        super.onSaveInstanceState(outState);
    }

    private void configurarNavegacaoInferior() {
        adicionarItemNav("Início", R.id.home, R.drawable.ic_nav_home);
        adicionarItemNav("Água", R.id.agua, R.drawable.ic_nav_water);
        adicionarItemNav("Foco", R.id.estudos, R.drawable.ic_nav_focus);
        adicionarItemNav("Relatórios", R.id.progresso, R.drawable.ic_nav_chart);
        adicionarItemNav("Mais", R.id.mais, R.drawable.ic_nav_more);
    }

    private void adicionarItemNav(String titulo, int id, int iconRes) {
        MaterialButton button = new MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
        button.setId(View.generateViewId());
        button.setTag(id);
        button.setText(titulo);
        button.setTextSize(10.5f);
        button.setSingleLine(true);
        button.setMaxLines(1);
        button.setEllipsize(TextUtils.TruncateAt.END);
        button.setIncludeFontPadding(false);
        button.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        button.setGravity(Gravity.CENTER);
        button.setAllCaps(false);
        button.setIconResource(iconRes);
        button.setIconSize(dp(24));
        button.setIconGravity(MaterialButton.ICON_GRAVITY_TOP);
        button.setIconPadding(dp(4));
        button.setMinWidth(0);
        button.setMinimumWidth(0);
        button.setMinHeight(0);
        button.setMinimumHeight(0);
        button.setInsetTop(0);
        button.setInsetBottom(0);
        button.setCornerRadius(dp(8));
        button.setStrokeWidth(0);
        button.setOnClickListener(v -> {
            Object tag = v.getTag();
            if (tag instanceof Integer) {
                navigateTo((Integer) tag);
            }
        });

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
        params.setMargins(dp(2), dp(3), dp(2), dp(3));
        bottomNavContainer.addView(button, params);
    }

    public void navigateTo(int itemId) {
        currentItemId = itemId;
        atualizarItemSelecionado();
        loadFragmentById(itemId);
    }

    private void atualizarItemSelecionado() {
        int selectedColor = ContextCompat.getColor(this, R.color.white);
        int defaultColor = ContextCompat.getColor(this, R.color.muted);
        int selectedBackground = ContextCompat.getColor(this, R.color.nav_selected_background);
        int transparent = ContextCompat.getColor(this, android.R.color.transparent);

        for (int i = 0; i < bottomNavContainer.getChildCount(); i++) {
            View child = bottomNavContainer.getChildAt(i);
            if (!(child instanceof MaterialButton)) continue;

            MaterialButton button = (MaterialButton) child;
            boolean selected = button.getTag() instanceof Integer && isNavItemSelected((Integer) button.getTag());
            int color = selected ? selectedColor : defaultColor;

            button.setTextColor(color);
            button.setIconTint(ColorStateList.valueOf(color));
            button.setBackgroundTintList(ColorStateList.valueOf(selected ? selectedBackground : transparent));
            button.animate()
                    .scaleX(selected ? 1.05f : 1f)
                    .scaleY(selected ? 1.05f : 1f)
                    .setDuration(160)
                    .start();
        }
    }

    private boolean isNavItemSelected(int navItemId) {
        if (navItemId == currentItemId) return true;
        if (navItemId == R.id.mais) {
            return currentItemId == R.id.progresso
                    || currentItemId == R.id.metas
                    || currentItemId == R.id.perfil
                    || currentItemId == R.id.habitos
                    || currentItemId == R.id.historico
                    || currentItemId == R.id.configuracoes
                    || currentItemId == R.id.backup
                    || currentItemId == R.id.conquistas
                    || currentItemId == R.id.calendario
                    || currentItemId == R.id.missoes
                    || currentItemId == R.id.relatorio
                    || currentItemId == R.id.aparencia
                    || currentItemId == R.id.temas
                    || currentItemId == R.id.desafios
                    || currentItemId == R.id.diario
                    || currentItemId == R.id.estatisticas;
        }
        return false;
    }

    private void loadFragmentById(int id) {
        if (id == R.id.home) {
            loadFragment(new HomeFragment());
        } else if (id == R.id.agua) {
            loadFragment(new AguaFragment());
        } else if (id == R.id.estudos) {
            loadFragment(new EstudosFragment());
        } else if (id == R.id.rotina) {
            loadFragment(new RotinaFragment());
        } else if (id == R.id.mais) {
            loadFragment(new MaisFragment());
        } else if (id == R.id.habitos) {
            loadFragment(new HabitosFragment());
        } else if (id == R.id.metas) {
            loadFragment(new MetasFragment());
        } else if (id == R.id.perfil) {
            loadFragment(new PerfilFragment());
        } else if (id == R.id.progresso) {
            loadFragment(new ProgressoFragment());
        } else if (id == R.id.historico) {
            loadFragment(new HistoricoFragment());
        } else if (id == R.id.configuracoes) {
            loadFragment(new ConfiguracoesFragment());
        } else if (id == R.id.backup) {
            loadFragment(new BackupFragment());
        } else if (id == R.id.conquistas) {
            loadFragment(new ConquistasFragment());
        } else if (id == R.id.calendario) {
            loadFragment(new CalendarioFragment());
        } else if (id == R.id.missoes) {
            loadFragment(new MissoesFragment());
        } else if (id == R.id.relatorio) {
            loadFragment(new RelatorioFragment());
        } else if (id == R.id.aparencia) {
            loadFragment(new AparenciaFragment());
        } else if (id == R.id.temas) {
            loadFragment(new TemasFragment());
        } else if (id == R.id.desafios) {
            loadFragment(new DesafiosFragment());
        } else if (id == R.id.diario) {
            loadFragment(new DiarioFragment());
        } else if (id == R.id.estatisticas) {
            loadFragment(new EstatisticasFragment());
        }
    }

    private void loadFragment(Fragment fragment) {
        frameContainer.setAlpha(0f);
        frameContainer.setTranslationY(dp(10));
        getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.frame_container, fragment)
                .commit();
        frameContainer.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(180)
                .start();
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < 33) return;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            return;
        }
        requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1001);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
