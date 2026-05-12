package com.exemple.habitapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

public final class ReminderScheduler {

    public static final String EXTRA_TYPE = "reminder_type";
    public static final String TYPE_WATER = "water";
    public static final String TYPE_FOCUS = "focus";
    public static final String TYPE_ROUTINE = "routine";

    private ReminderScheduler() {
    }

    public static void scheduleDefaultReminders(Context context) {
        scheduleRepeating(context, TYPE_WATER, 10, 0, AlarmManager.INTERVAL_HOUR * 2);
        scheduleRepeating(context, TYPE_FOCUS, 16, 30, AlarmManager.INTERVAL_DAY);
        scheduleRepeating(context, TYPE_ROUTINE, 21, 0, AlarmManager.INTERVAL_DAY);
    }

    private static void scheduleRepeating(Context context, String type, int hour, int minute, long interval) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                interval,
                getPendingIntent(context, type)
        );
    }

    private static PendingIntent getPendingIntent(Context context, String type) {
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra(EXTRA_TYPE, type);
        return PendingIntent.getBroadcast(
                context,
                type.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | NotificationHelper.immutableFlag()
        );
    }
}
