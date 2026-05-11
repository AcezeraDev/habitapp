package com.exemple.habitapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity {

    private TabLayout mainTabs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        HabitStore.ensureToday(getSharedPreferences("habit_data", MODE_PRIVATE));

        mainTabs = findViewById(R.id.main_tabs);
        configurarTabs();

        if (savedInstanceState == null) {
            navigateTo(R.id.home);
        }
    }

    private void configurarTabs() {
        adicionarTab("Inicio", R.id.home);
        adicionarTab("Agua", R.id.agua);
        adicionarTab("Foco", R.id.estudos);
        adicionarTab("Rotina", R.id.rotina);
        adicionarTab("Metas", R.id.metas);
        adicionarTab("Perfil", R.id.perfil);
        adicionarTab("Progresso", R.id.progresso);

        mainTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Object tag = tab.getTag();
                if (tag instanceof Integer) {
                    loadFragmentById((Integer) tag);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void adicionarTab(String titulo, int id) {
        TabLayout.Tab tab = mainTabs.newTab();
        tab.setText(titulo);
        tab.setTag(id);
        mainTabs.addTab(tab, false);
    }

    public void navigateTo(int itemId) {
        for (int i = 0; i < mainTabs.getTabCount(); i++) {
            TabLayout.Tab tab = mainTabs.getTabAt(i);
            if (tab != null && tab.getTag() instanceof Integer && ((Integer) tab.getTag()) == itemId) {
                tab.select();
                return;
            }
        }

        loadFragmentById(itemId);
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
        } else if (id == R.id.metas) {
            loadFragment(new MetasFragment());
        } else if (id == R.id.perfil) {
            loadFragment(new PerfilFragment());
        } else if (id == R.id.progresso) {
            loadFragment(new ProgressoFragment());
        }
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_container, fragment)
                .commit();
    }
}
