package com.exemple.habitapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottom_nav);

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }

        bottomNav.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.home) {
                loadFragment(new HomeFragment());
                return true;

            } else if (id == R.id.agua) {
                loadFragment(new AguaFragment());
                return true;

            } else if (id == R.id.estudos) {
                loadFragment(new EstudosFragment());
                return true;

            } else if (id == R.id.progresso) {
                loadFragment(new ProgressoFragment());
                return true;
            }

            return false;
        });

    }

    public void navigateTo(int itemId) {
        bottomNav.setSelectedItemId(itemId);
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_container, fragment)
                .commit();
    }
}
