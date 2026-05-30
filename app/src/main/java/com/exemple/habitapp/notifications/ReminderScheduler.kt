package com.exemple.habitapp.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

object ReminderScheduler {
    const val EXTRA_TYPE = "reminder_type"
    const val TYPE_WATER = "water"
    const val TYPE_FOCUS = "focus"
    const val TYPE_ROUTINE = "routine"

    const val PREF_WATER_ENABLED = "reminder_water_enabled"
    const val PREF_FOCUS_ENABLED = "reminder_focus_enabled"
    const val PREF_ROUTINE_ENABLED = "reminder_routine_enabled"
    const val PREF_WATER_START_HOUR = "reminder_water_start_hour"
    const val PREF_WATER_START_MINUTE = "reminder_water_start_minute"
    const val PREF_WATER_INTERVAL_HOURS = "reminder_water_interval_hours"
    const val PREF_FOCUS_HOUR = "reminder_focus_hour"
    const val PREF_FOCUS_MINUTE = "reminder_focus_minute"
    const val PREF_ROUTINE_HOUR = "reminder_routine_hour"
    const val PREF_ROUTINE_MINUTE = "reminder_routine_minute"

    fun scheduleDefaultReminders(context: Context) {
        val prefs = context.getSharedPreferences("habit_data", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("notifications_enabled", true)) {
            cancelAll(context)
            return
        }

        if (prefs.getBoolean(PREF_WATER_ENABLED, prefs.getBoolean("water_reminder_enabled", true))) {
            val intervalHours = prefs.getInt(PREF_WATER_INTERVAL_HOURS, 2).coerceIn(1, 8)
            scheduleRepeating(
                context = context,
                type = TYPE_WATER,
                hour = prefs.getInt(PREF_WATER_START_HOUR, 10),
                minute = prefs.getInt(PREF_WATER_START_MINUTE, 0),
                intervalMillis = AlarmManager.INTERVAL_HOUR * intervalHours,
            )
        } else {
            cancelReminder(context, TYPE_WATER)
        }

        if (prefs.getBoolean(PREF_FOCUS_ENABLED, prefs.getBoolean("focus_reminder_enabled", true))) {
            scheduleRepeating(
                context = context,
                type = TYPE_FOCUS,
                hour = prefs.getInt(PREF_FOCUS_HOUR, 16),
                minute = prefs.getInt(PREF_FOCUS_MINUTE, 30),
                intervalMillis = AlarmManager.INTERVAL_DAY,
            )
        } else {
            cancelReminder(context, TYPE_FOCUS)
        }

        if (prefs.getBoolean(PREF_ROUTINE_ENABLED, true)) {
            scheduleRepeating(
                context = context,
                type = TYPE_ROUTINE,
                hour = prefs.getInt(PREF_ROUTINE_HOUR, 21),
                minute = prefs.getInt(PREF_ROUTINE_MINUTE, 0),
                intervalMillis = AlarmManager.INTERVAL_DAY,
            )
        } else {
            cancelReminder(context, TYPE_ROUTINE)
        }
    }

    fun cancelAll(context: Context) {
        cancelReminder(context, TYPE_WATER)
        cancelReminder(context, TYPE_FOCUS)
        cancelReminder(context, TYPE_ROUTINE)
    }

    fun cancelReminder(context: Context, type: String) {
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        manager.cancel(pendingIntent(context, type, type.hashCode()))
    }

    fun scheduleSnooze(context: Context, type: String?, minutes: Int) {
        val cleanType = type ?: TYPE_ROUTINE
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerAt = System.currentTimeMillis() + minutes.coerceAtLeast(1) * 60_000L
        manager.set(
            AlarmManager.RTC_WAKEUP,
            triggerAt,
            pendingIntent(context, cleanType, "snooze_$cleanType".hashCode()),
        )
    }

    private fun scheduleRepeating(context: Context, type: String, hour: Int, minute: Int, intervalMillis: Long) {
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour.coerceIn(0, 23))
            set(Calendar.MINUTE, minute.coerceIn(0, 59))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        while (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.timeInMillis = calendar.timeInMillis + intervalMillis
        }
        manager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            intervalMillis,
            pendingIntent(context, type, type.hashCode()),
        )
    }

    private fun pendingIntent(context: Context, type: String, requestCode: Int): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java)
            .putExtra(EXTRA_TYPE, type)
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
