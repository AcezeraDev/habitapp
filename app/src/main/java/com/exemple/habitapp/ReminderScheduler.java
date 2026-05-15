package com.exemple.habitapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.Calendar;

public final class ReminderScheduler {

    public static final String EXTRA_TYPE = "reminder_type";
    public static final String TYPE_WATER = "water";
    public static final String TYPE_FOCUS = "focus";
    public static final String TYPE_ROUTINE = "routine";

    public static final String PREF_WATER_ENABLED = "reminder_water_enabled";
    public static final String PREF_FOCUS_ENABLED = "reminder_focus_enabled";
    public static final String PREF_ROUTINE_ENABLED = "reminder_routine_enabled";
    public static final String PREF_WATER_START_HOUR = "reminder_water_start_hour";
    public static final String PREF_WATER_START_MINUTE = "reminder_water_start_minute";
    public static final String PREF_WATER_INTERVAL_HOURS = "reminder_water_interval_hours";
    public static final String PREF_FOCUS_HOUR = "reminder_focus_hour";
    public static final String PREF_FOCUS_MINUTE = "reminder_focus_minute";
    public static final String PREF_ROUTINE_HOUR = "reminder_routine_hour";
    public static final String PREF_ROUTINE_MINUTE = "reminder_routine_minute";

    private ReminderScheduler() {
    }

    public static void scheduleDefaultReminders(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("habit_data", Context.MODE_PRIVATE);

        if (prefs.getBoolean(PREF_WATER_ENABLED, true)) {
            int hour = prefs.getInt(PREF_WATER_START_HOUR, 10);
            int minute = prefs.getInt(PREF_WATER_START_MINUTE, 0);
            int intervalHours = Math.max(1, prefs.getInt(PREF_WATER_INTERVAL_HOURS, 2));
            scheduleRepeating(context, TYPE_WATER, hour, minute, AlarmManager.INTERVAL_HOUR * intervalHours);
        } else {
            cancelReminder(context, TYPE_WATER);
        }

        if (prefs.getBoolean(PREF_FOCUS_ENABLED, true)) {
            scheduleRepeating(
                    context,
                    TYPE_FOCUS,
                    prefs.getInt(PREF_FOCUS_HOUR, 16),
                    prefs.getInt(PREF_FOCUS_MINUTE, 30),
                    AlarmManager.INTERVAL_DAY
            );
        } else {
            cancelReminder(context, TYPE_FOCUS);
        }

        if (prefs.getBoolean(PREF_ROUTINE_ENABLED, true)) {
            scheduleRepeating(
                    context,
                    TYPE_ROUTINE,
                    prefs.getInt(PREF_ROUTINE_HOUR, 21),
                    prefs.getInt(PREF_ROUTINE_MINUTE, 0),
                    AlarmManager.INTERVAL_DAY
            );
        } else {
            cancelReminder(context, TYPE_ROUTINE);
        }
    }

    public static void cancelReminder(Context context, String type) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;
        alarmManager.cancel(getPendingIntent(context, type));
    }

    public static void scheduleSnooze(Context context, String type, int minutes) {
        if (type == null) type = TYPE_ROUTINE;
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;
        long triggerAt = System.currentTimeMillis() + Math.max(1, minutes) * 60L * 1000L;
        alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerAt,
                getPendingIntent(context, type, ("snooze_" + type).hashCode())
        );
    }

    private static void scheduleRepeating(Context context, String type, int hour, int minute, long interval) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long triggerAt = calendar.getTimeInMillis();
        long now = System.currentTimeMillis();
        while (triggerAt <= now) {
            triggerAt += interval;
        }

        alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                triggerAt,
                interval,
                getPendingIntent(context, type)
        );
    }

    private static PendingIntent getPendingIntent(Context context, String type) {
        return getPendingIntent(context, type, type.hashCode());
    }

    private static PendingIntent getPendingIntent(Context context, String type, int requestCode) {
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra(EXTRA_TYPE, type);
        return PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | NotificationHelper.immutableFlag()
        );
    }
}
