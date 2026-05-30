package com.exemple.habitapp.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.exemple.habitapp.MainActivity
import com.exemple.habitapp.R

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
            description = "Lembretes de água, foco e rotina."
        }
        manager.createNotificationChannel(channel)
    }

    fun showReminder(context: Context, type: String) {
        createChannels(context)
        val (title, message) = when (type) {
            "water" -> "Hora de hidratar" to "Registre um copo de água e mantenha o ritmo do dia."
            "focus" -> "Bloco de foco" to "Abra uma sessão curta e avance no seu objetivo."
            else -> "Rotina em movimento" to "Escolha uma ação pequena para manter consistência."
        }
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            type.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(type.hashCode(), notification)
    }
}
