package com.exemple.habitapp;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public final class NotificationHelper {

    public static final String CHANNEL_REMINDERS = "habit_reminders";

    private NotificationHelper() {
    }

    public static void createChannels(Context context) {
        if (Build.VERSION.SDK_INT < 26) return;

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_REMINDERS,
                "Lembretes do HabitApp",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        channel.setDescription("Lembretes de água, foco e rotina.");

        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }

    public static void showReminder(Context context, String type) {
        if (Build.VERSION.SDK_INT >= 33
                && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        createChannels(context);

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                type.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | immutableFlag()
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_REMINDERS)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getTitle(type))
                .setContentText(getMessage(type))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setColor(ContextCompat.getColor(context, R.color.primary));

        NotificationManagerCompat.from(context).notify(type.hashCode(), builder.build());
    }

    private static String getTitle(String type) {
        if (ReminderScheduler.TYPE_WATER.equals(type)) return "Hora da hidratação";
        if (ReminderScheduler.TYPE_FOCUS.equals(type)) return "Bloco de foco";
        return "Feche sua rotina";
    }

    private static String getMessage(String type) {
        if (ReminderScheduler.TYPE_WATER.equals(type)) return "Registre um copo e mantenha a meta leve.";
        if (ReminderScheduler.TYPE_FOCUS.equals(type)) return "Uma sessão curta já melhora o score do dia.";
        return "Passe pelo checklist e transforme intenção em consistência.";
    }

    public static int immutableFlag() {
        return Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0;
    }
}
