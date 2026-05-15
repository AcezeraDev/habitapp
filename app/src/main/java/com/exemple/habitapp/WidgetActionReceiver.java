package com.exemple.habitapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

public class WidgetActionReceiver extends BroadcastReceiver {

    public static final String ACTION_ADD_WATER = "com.exemple.habitapp.ADD_WATER_FROM_WIDGET";
    public static final String ACTION_ADD_FOCUS = "com.exemple.habitapp.ADD_FOCUS_FROM_NOTIFICATION";
    public static final String ACTION_COMPLETE_ROUTINE = "com.exemple.habitapp.COMPLETE_ROUTINE_FROM_NOTIFICATION";
    public static final String ACTION_SNOOZE_REMINDER = "com.exemple.habitapp.SNOOZE_REMINDER";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            return;
        }

        SharedPreferences prefs = context.getSharedPreferences("habit_data", Context.MODE_PRIVATE);
        HabitStore.ensureToday(prefs);

        if (ACTION_ADD_WATER.equals(intent.getAction())) {
            addWater(context, prefs);
        } else if (ACTION_ADD_FOCUS.equals(intent.getAction())) {
            addFocus(context, prefs);
        } else if (ACTION_COMPLETE_ROUTINE.equals(intent.getAction())) {
            completeRoutine(context, prefs);
        } else if (ACTION_SNOOZE_REMINDER.equals(intent.getAction())) {
            ReminderScheduler.scheduleSnooze(context, intent.getStringExtra(ReminderScheduler.EXTRA_TYPE), 15);
            Toast.makeText(context, "Vou lembrar de novo em 15 min.", Toast.LENGTH_SHORT).show();
        }
    }

    private void addWater(Context context, SharedPreferences prefs) {
        int currentMl = HabitStore.getAguaMl(prefs);
        int nextMl = currentMl + 250;
        prefs.edit()
                .putFloat("agua_litros", nextMl / 1000f)
                .putInt("total_agua_ml_registrado", prefs.getInt("total_agua_ml_registrado", 0) + 250)
                .apply();
        HabitStore.saveTodaySnapshot(prefs);
        HabitWidgetProvider.updateAll(context);
        FeedbackHelper.success(context);
        Toast.makeText(context, "+250 ml registrados.", Toast.LENGTH_SHORT).show();
    }

    private void addFocus(Context context, SharedPreferences prefs) {
        int current = prefs.getInt("estudos_concluidos_min", 0);
        int next = current + 15;
        prefs.edit()
                .putInt("estudos_concluidos_min", next)
                .putInt("sessoes_foco_concluidas", prefs.getInt("sessoes_foco_concluidas", 0) + 1)
                .putInt("total_foco_min_registrado", prefs.getInt("total_foco_min_registrado", 0) + 15)
                .apply();
        HabitStore.saveTodaySnapshot(prefs);
        HabitWidgetProvider.updateAll(context);
        FeedbackHelper.success(context);
        Toast.makeText(context, "+15 min de foco registrados.", Toast.LENGTH_SHORT).show();
    }

    private void completeRoutine(Context context, SharedPreferences prefs) {
        long today = HabitStore.todayKey();
        prefs.edit()
                .putBoolean("rotina_bloco_manha_" + today, true)
                .putBoolean("rotina_bloco_alimentacao_" + today, true)
                .putBoolean("rotina_bloco_treino_" + today, true)
                .putBoolean("rotina_bloco_sono_" + today, true)
                .putBoolean("check_planejamento_" + today, true)
                .putBoolean("check_sono_" + today, true)
                .apply();
        HabitStore.saveTodaySnapshot(prefs);
        HabitWidgetProvider.updateAll(context);
        FeedbackHelper.success(context);
        Toast.makeText(context, "Rotina marcada.", Toast.LENGTH_SHORT).show();
    }
}
