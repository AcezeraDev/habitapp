package com.exemple.habitapp.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.exemple.habitapp.MainActivity
import com.exemple.habitapp.R
import com.exemple.habitapp.data.HabitRepository
import com.exemple.habitapp.ui.navigation.HabitRoute
import com.exemple.habitapp.widgets.WidgetActionReceiver

object NotificationHelper {
    private const val CHANNEL_ID = "habit_reminders"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < 26) return
        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Lembretes do HabitApp",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Lembretes inteligentes de água, foco e rotina."
        }
        manager.createNotificationChannel(channel)
    }

    fun showReminder(context: Context, type: String) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        createChannels(context)
        val state = HabitRepository(context).snapshot()
        val (title, message) = when (type) {
            ReminderScheduler.TYPE_WATER -> {
                val missing = (state.waterGoalMl - state.waterMl).coerceAtLeast(0)
                "Hora da água" to if (missing == 0) {
                    "Meta de água fechada. Abra o painel para manter a sequência."
                } else {
                    "Faltam $missing ml para bater a meta de hoje. Um copo agora já ajuda."
                }
            }
            ReminderScheduler.TYPE_FOCUS -> {
                val missing = (state.focusGoalMinutes - state.focusMinutes).coerceAtLeast(0)
                "Bloco de foco" to if (missing == 0) {
                    "Foco do dia fechado. Revise seu progresso e proteja a rotina."
                } else {
                    "Faltam $missing min de foco hoje. Comece com uma sessão curta."
                }
            }
            else -> {
                val missing = 4 - state.routineBlocks.count { it }
                "Fechamento da rotina" to if (missing <= 0) {
                    "Rotina completa. Veja conquistas e prepare o próximo dia."
                } else {
                    "Faltam $missing blocos para fechar a rotina de hoje."
                }
            }
        }

        val pendingIntent = openAppIntent(context, type)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_notification, actionLabel(type), actionIntent(context, type))
            .addAction(R.drawable.ic_clock_history, "Lembrar em 15 min", snoozeIntent(context, type))
            .addAction(R.drawable.ic_nav_home, "Abrir app", pendingIntent)
            .build()
        NotificationManagerCompat.from(context).notify(type.hashCode(), notification)
    }

    private fun openAppIntent(context: Context, type: String): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
            .putExtra(MainActivity.EXTRA_NAV_ROUTE, destinationRoute(type))
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        return PendingIntent.getActivity(
            context,
            type.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun actionIntent(context: Context, type: String): PendingIntent {
        val action = when (type) {
            ReminderScheduler.TYPE_WATER -> WidgetActionReceiver.ACTION_ADD_WATER
            ReminderScheduler.TYPE_FOCUS -> WidgetActionReceiver.ACTION_ADD_FOCUS
            else -> WidgetActionReceiver.ACTION_COMPLETE_ROUTINE
        }
        val intent = Intent(context, WidgetActionReceiver::class.java).setAction(action)
        return PendingIntent.getBroadcast(
            context,
            410 + type.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun snoozeIntent(context: Context, type: String): PendingIntent {
        val intent = Intent(context, WidgetActionReceiver::class.java)
            .setAction(WidgetActionReceiver.ACTION_SNOOZE_REMINDER)
            .putExtra(ReminderScheduler.EXTRA_TYPE, type)
        return PendingIntent.getBroadcast(
            context,
            520 + type.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun destinationRoute(type: String): String = when (type) {
        ReminderScheduler.TYPE_WATER -> HabitRoute.Water.route
        ReminderScheduler.TYPE_FOCUS -> HabitRoute.Focus.route
        else -> HabitRoute.Routine.route
    }

    private fun actionLabel(type: String): String = when (type) {
        ReminderScheduler.TYPE_WATER -> "Registrar água"
        ReminderScheduler.TYPE_FOCUS -> "Registrar foco"
        else -> "Marcar rotina"
    }
}
