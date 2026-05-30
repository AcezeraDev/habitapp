package com.exemple.habitapp.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val type = intent?.getStringExtra(ReminderScheduler.EXTRA_TYPE).orEmpty()
        val prefs = context.getSharedPreferences("habit_data", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("notifications_enabled", true)) return
        if (type == "water" && !prefs.getBoolean("water_reminder_enabled", true)) return
        if (type == "focus" && !prefs.getBoolean("focus_reminder_enabled", true)) return
        NotificationHelper.showReminder(context, type.ifBlank { "routine" })
        ReminderScheduler.scheduleDefaultReminders(context)
    }
}
