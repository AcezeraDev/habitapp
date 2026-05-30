package com.exemple.habitapp.widgets

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.exemple.habitapp.data.HabitRepository
import com.exemple.habitapp.notifications.ReminderScheduler

class WidgetActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        val repository = HabitRepository(context)
        when (action) {
            ACTION_ADD_WATER -> {
                val added = repository.addWater(250)
                Toast.makeText(context, if (added > 0) "+$added ml registrados." else "Meta de água já concluída.", Toast.LENGTH_SHORT).show()
            }
            ACTION_ADD_FOCUS -> {
                repository.completeFocusSession(15)
                Toast.makeText(context, "+15 min de foco registrados.", Toast.LENGTH_SHORT).show()
            }
            ACTION_COMPLETE_ROUTINE -> {
                repository.completeRoutine()
                Toast.makeText(context, "Rotina marcada.", Toast.LENGTH_SHORT).show()
            }
            ACTION_SNOOZE_REMINDER -> {
                ReminderScheduler.scheduleSnooze(context, intent.getStringExtra(ReminderScheduler.EXTRA_TYPE), 15)
                Toast.makeText(context, "Vou lembrar de novo em 15 min.", Toast.LENGTH_SHORT).show()
            }
        }
        HabitWidgetProvider.updateAll(context)
    }

    companion object {
        const val ACTION_ADD_WATER = "com.exemple.habitapp.ADD_WATER_FROM_WIDGET"
        const val ACTION_ADD_FOCUS = "com.exemple.habitapp.ADD_FOCUS_FROM_NOTIFICATION"
        const val ACTION_COMPLETE_ROUTINE = "com.exemple.habitapp.COMPLETE_ROUTINE_FROM_NOTIFICATION"
        const val ACTION_SNOOZE_REMINDER = "com.exemple.habitapp.SNOOZE_REMINDER"
    }
}
