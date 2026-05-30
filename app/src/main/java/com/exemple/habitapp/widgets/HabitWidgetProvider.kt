package com.exemple.habitapp.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.exemple.habitapp.MainActivity
import com.exemple.habitapp.R
import com.exemple.habitapp.data.HabitRepository
import com.exemple.habitapp.ui.navigation.HabitRoute

class HabitWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { updateWidget(context, appWidgetManager, it) }
    }

    companion object {
        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, HabitWidgetProvider::class.java)
            manager.getAppWidgetIds(component).forEach { updateWidget(context, manager, it) }
        }

        private fun updateWidget(context: Context, manager: AppWidgetManager, appWidgetId: Int) {
            val state = HabitRepository(context).snapshot()
            val missingWater = (state.waterGoalMl - state.waterMl).coerceAtLeast(0)
            val missingFocus = (state.focusGoalMinutes - state.focusMinutes).coerceAtLeast(0)
            val details = when {
                missingWater > 0 -> "Próximo: +${missingWater.coerceAtMost(250)} ml | água ${state.waterMl}/${state.waterGoalMl} ml"
                missingFocus > 0 -> "Próximo: foco 15 min | foco ${state.focusMinutes}/${state.focusGoalMinutes} min"
                else -> "Dia encaminhado. Abra o app para ver conquistas."
            }

            val views = RemoteViews(context.packageName, R.layout.app_widget_habit).apply {
                setTextViewText(R.id.widgetTitle, "HabitApp | ${state.streak}d streak")
                setTextViewText(R.id.widgetScore, "Score ${state.score}%")
                setTextViewText(R.id.widgetDetails, details)
                setProgressBar(R.id.widgetProgress, 100, state.score, false)
                setOnClickPendingIntent(R.id.widgetRoot, openAppIntent(context, HabitRoute.Home.route, 10))
                setOnClickPendingIntent(R.id.widgetAddWater, widgetAction(context, WidgetActionReceiver.ACTION_ADD_WATER, 20))
                setOnClickPendingIntent(R.id.widgetOpenFocus, openAppIntent(context, HabitRoute.Focus.route, 30))
            }
            manager.updateAppWidget(appWidgetId, views)
        }

        private fun openAppIntent(context: Context, route: String, requestCode: Int): PendingIntent {
            val intent = Intent(context, MainActivity::class.java)
                .putExtra(MainActivity.EXTRA_NAV_ROUTE, route)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            return PendingIntent.getActivity(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }

        private fun widgetAction(context: Context, action: String, requestCode: Int): PendingIntent {
            val intent = Intent(context, WidgetActionReceiver::class.java).setAction(action)
            return PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }
    }
}
