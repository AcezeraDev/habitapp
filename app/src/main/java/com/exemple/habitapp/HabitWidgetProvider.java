package com.exemple.habitapp;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

public class HabitWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId);
        }
    }

    public static void updateAll(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        ComponentName component = new ComponentName(context, HabitWidgetProvider.class);
        int[] ids = manager.getAppWidgetIds(component);
        for (int id : ids) {
            updateWidget(context, manager, id);
        }
    }

    private static void updateWidget(Context context, AppWidgetManager manager, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences("habit_data", Context.MODE_PRIVATE);
        HabitStore.ensureToday(prefs);
        HabitStore.saveTodaySnapshot(prefs);

        int score = HabitStore.getTodayScore(prefs);
        int agua = HabitStore.getAguaMl(prefs);
        int metaAgua = HabitStore.getMetaAguaMl(prefs);
        int foco = prefs.getInt("estudos_concluidos_min", 0);
        int metaFoco = prefs.getInt("meta_estudos_min", 60);
        int streak = HabitStore.getStreak(prefs);
        int missingWater = Math.max(0, metaAgua - agua);
        int missingFocus = Math.max(0, metaFoco - foco);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.app_widget_habit);
        views.setTextViewText(R.id.widgetTitle, "HabitApp | " + streak + "d streak");
        views.setTextViewText(R.id.widgetScore, "Score " + score + "%");
        views.setTextViewText(R.id.widgetDetails, nextWidgetText(missingWater, missingFocus, agua, metaAgua, foco, metaFoco));
        views.setProgressBar(R.id.widgetProgress, 100, score, false);
        views.setOnClickPendingIntent(R.id.widgetRoot, openAppIntent(context, R.id.home, 10));
        views.setOnClickPendingIntent(R.id.widgetAddWater, widgetAction(context, WidgetActionReceiver.ACTION_ADD_WATER, 20));
        views.setOnClickPendingIntent(R.id.widgetOpenFocus, openAppIntent(context, R.id.estudos, 30));
        manager.updateAppWidget(appWidgetId, views);
    }

    private static String nextWidgetText(int missingWater, int missingFocus, int agua, int metaAgua, int foco, int metaFoco) {
        if (missingWater > 0) return "Proximo: +" + Math.min(250, missingWater) + " ml | agua " + agua + "/" + metaAgua + " ml";
        if (missingFocus > 0) return "Proximo: foco 15 min | foco " + foco + "/" + metaFoco + " min";
        return "Dia encaminhado. Abra o app para ver conquistas.";
    }

    private static PendingIntent openAppIntent(Context context, int destination, int requestCode) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("nav_target", destination);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | NotificationHelper.immutableFlag()
        );
    }

    private static PendingIntent widgetAction(Context context, String action, int requestCode) {
        Intent intent = new Intent(context, WidgetActionReceiver.class);
        intent.setAction(action);
        return PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | NotificationHelper.immutableFlag()
        );
    }
}
