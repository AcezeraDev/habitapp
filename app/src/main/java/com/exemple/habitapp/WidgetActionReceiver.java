package com.exemple.habitapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

public class WidgetActionReceiver extends BroadcastReceiver {

    public static final String ACTION_ADD_WATER = "com.exemple.habitapp.ADD_WATER_FROM_WIDGET";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !ACTION_ADD_WATER.equals(intent.getAction())) {
            return;
        }

        SharedPreferences prefs = context.getSharedPreferences("habit_data", Context.MODE_PRIVATE);
        HabitStore.ensureToday(prefs);
        int currentMl = HabitStore.getAguaMl(prefs);
        int nextMl = currentMl + 250;
        prefs.edit()
                .putFloat("agua_litros", nextMl / 1000f)
                .putInt("total_agua_ml_registrado", prefs.getInt("total_agua_ml_registrado", 0) + 250)
                .apply();
        HabitStore.saveTodaySnapshot(prefs);
        HabitWidgetProvider.updateAll(context);
        Toast.makeText(context, "+250 ml registrados.", Toast.LENGTH_SHORT).show();
    }
}
