package com.exemple.habitapp;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
        channel.setDescription("Lembretes inteligentes de agua, foco e rotina.");

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
        SharedPreferences prefs = context.getSharedPreferences("habit_data", Context.MODE_PRIVATE);
        HabitStore.ensureToday(prefs);

        String title = getTitle(type);
        String message = getMessage(type, prefs);
        PendingIntent pendingIntent = getPendingIntent(context, type);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_REMINDERS)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setColor(ContextCompat.getColor(context, getColor(type)))
                .addAction(R.drawable.ic_notification, getActionLabel(type), pendingIntent);

        NotificationManagerCompat.from(context).notify(type.hashCode(), builder.build());
    }

    private static PendingIntent getPendingIntent(Context context, String type) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("nav_target", getDestination(type));
        return PendingIntent.getActivity(
                context,
                type.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | immutableFlag()
        );
    }

    private static String getTitle(String type) {
        if (ReminderScheduler.TYPE_WATER.equals(type)) return "Hora da agua";
        if (ReminderScheduler.TYPE_FOCUS.equals(type)) return "Bloco de foco";
        return "Fechamento da rotina";
    }

    private static String getMessage(String type, SharedPreferences prefs) {
        if (ReminderScheduler.TYPE_WATER.equals(type)) {
            int falta = Math.max(0, HabitStore.getMetaAguaMl(prefs) - HabitStore.getAguaMl(prefs));
            return falta == 0
                    ? "Meta de agua fechada. Abra o painel para manter a sequencia."
                    : "Faltam " + falta + " ml para bater a meta de hoje. Um copo agora ja ajuda.";
        }

        if (ReminderScheduler.TYPE_FOCUS.equals(type)) {
            int falta = Math.max(0, prefs.getInt("meta_estudos_min", 60) - prefs.getInt("estudos_concluidos_min", 0));
            return falta == 0
                    ? "Foco do dia fechado. Revise seu progresso e proteja a rotina."
                    : "Faltam " + falta + " min de foco hoje. Comece com uma sessao curta.";
        }

        int checklist = HabitStore.getChecklistConcluido(prefs, HabitStore.todayKey());
        return checklist >= 3
                ? "Checklist completo. Veja suas conquistas e prepare o proximo dia."
                : "Faltam " + (3 - checklist) + " itens do checklist para fechar a rotina.";
    }

    private static int getDestination(String type) {
        if (ReminderScheduler.TYPE_WATER.equals(type)) return R.id.agua;
        if (ReminderScheduler.TYPE_FOCUS.equals(type)) return R.id.estudos;
        return R.id.rotina;
    }

    private static int getColor(String type) {
        if (ReminderScheduler.TYPE_WATER.equals(type)) return R.color.water;
        if (ReminderScheduler.TYPE_FOCUS.equals(type)) return R.color.study;
        return R.color.success;
    }

    private static String getActionLabel(String type) {
        if (ReminderScheduler.TYPE_WATER.equals(type)) return "Registrar agua";
        if (ReminderScheduler.TYPE_FOCUS.equals(type)) return "Abrir foco";
        return "Ver rotina";
    }

    public static int immutableFlag() {
        return Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0;
    }
}
