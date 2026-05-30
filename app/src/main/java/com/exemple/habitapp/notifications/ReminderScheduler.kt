package com.exemple.habitapp.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

object ReminderScheduler {
    const val EXTRA_TYPE = "type"
    private const val WATER_REQUEST = 4101
    private const val FOCUS_REQUEST = 4102

    fun scheduleDefaultReminders(context: Context) {
        val prefs = context.getSharedPreferences("habit_data", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("notifications_enabled", true)) {
            cancelAll(context)
            return
        }
        if (prefs.getBoolean("water_reminder_enabled", true)) schedule(context, "water", WATER_REQUEST, 10, 0)
        if (prefs.getBoolean("focus_reminder_enabled", true)) schedule(context, "focus", FOCUS_REQUEST, 16, 30)
    }

    fun cancelAll(context: Context) {
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        manager.cancel(pendingIntent(context, "water", WATER_REQUEST))
        manager.cancel(pendingIntent(context, "focus", FOCUS_REQUEST))
    }

    private fun schedule(context: Context, type: String, requestCode: Int, hour: Int, minute: Int) {
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }
        manager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent(context, type, requestCode),
        )
    }

    private fun pendingIntent(context: Context, type: String, requestCode: Int): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).putExtra(EXTRA_TYPE, type)
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
