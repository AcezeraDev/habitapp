package com.exemple.habitapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

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
        adicionarItemNav("Inicio", R.id.home, R.drawable.ic_nav_home);
        adicionarItemNav("Agua", R.id.agua, R.drawable.ic_nav_water);
        adicionarItemNav("Foco", R.id.estudos, R.drawable.ic_nav_focus);
        adicionarItemNav("Progresso", R.id.progresso, R.drawable.ic_nav_chart);
        adicionarItemNav("Mais", R.id.mais, R.drawable.ic_nav_more);
    }

    private void adicionarItemNav(String titulo, int id, int iconRes) {
        LinearLayout item = new LinearLayout(this);
        item.setId(View.generateViewId());
        item.setTag(id);
        item.setGravity(Gravity.CENTER);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setClickable(true);
        item.setFocusable(true);
        item.setPadding(dp(2), dp(8), dp(2), dp(6));
        HabitUi.press(item);

        ImageView icon = new ImageView(this);
        icon.setImageResource(iconRes);
        icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        icon.setContentDescription(null);
        icon.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        item.addView(icon, new LinearLayout.LayoutParams(dp(27), dp(27)));

        TextView label = new TextView(this);
        label.setText(titulo);
        label.setTextSize(9.5f);
        label.setSingleLine(true);
        label.setMaxLines(1);
        label.setEllipsize(TextUtils.TruncateAt.END);
        label.setGravity(Gravity.CENTER);
        label.setIncludeFontPadding(false);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        labelParams.setMargins(0, dp(7), 0, 0);
        item.addView(label, labelParams);

        item.setOnClickListener(v -> {
            Object tag = v.getTag();
            if (tag instanceof Integer) {
                navigateTo((Integer) tag);
            }
        });

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
        params.setMargins(dp(2), 0, dp(2), 0);
        bottomNavContainer.addView(item, params);
    }

    public void navigateTo(int itemId) {
        currentItemId = itemId;
        atualizarItemSelecionado();
        loadFragmentById(itemId);
    }

    private void atualizarItemSelecionado() {
        int selectedColor = ContextCompat.getColor(this, R.color.primary_dark);
        int defaultColor = ContextCompat.getColor(this, R.color.muted);

        for (int i = 0; i < bottomNavContainer.getChildCount(); i++) {
            View child = bottomNavContainer.getChildAt(i);
            if (!(child instanceof LinearLayout)) continue;

            LinearLayout item = (LinearLayout) child;
            boolean selected = item.getTag() instanceof Integer && isNavItemSelected((Integer) item.getTag());
            int color = selected ? selectedColor : defaultColor;
            View iconView = item.getChildAt(0);
            View labelView = item.getChildAt(1);

            if (iconView instanceof ImageView) {
                ((ImageView) iconView).setImageTintList(ColorStateList.valueOf(color));
            }
            if (labelView instanceof TextView) {
                TextView label = (TextView) labelView;
                label.setTextColor(color);
                label.setTypeface(label.getTypeface(), selected ? Typeface.BOLD : Typeface.NORMAL);
            }

            item.setBackground(selected
                    ? HabitUi.rounded(this, R.color.nav_selected_background, R.color.nav_selected_background, 0, 26)
                    : HabitUi.rounded(this, android.R.color.transparent, android.R.color.transparent, 0, 26));
            item.animate()
                    .scaleX(selected ? 1.02f : 1f)
                    .scaleY(selected ? 1.02f : 1f)
                    .setDuration(160)
                    .start();
        }
    }

    private boolean isNavItemSelected(int navItemId) {
        if (navItemId == currentItemId) return true;
        if (navItemId == R.id.mais) {
            return currentItemId == R.id.metas
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
        frameContainer.setTranslationY(dp(18));
        frameContainer.setScaleX(0.985f);
        frameContainer.setScaleY(0.985f);
        getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.frame_container, fragment)
                .commit();
        frameContainer.animate()
                .alpha(1f)
                .translationY(0f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(260)
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
