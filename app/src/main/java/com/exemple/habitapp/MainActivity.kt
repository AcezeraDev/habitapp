package com.exemple.habitapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.exemple.habitapp.notifications.NotificationHelper
import com.exemple.habitapp.notifications.ReminderScheduler
import com.exemple.habitapp.ui.compose.HabitAppRoot
import com.exemple.habitapp.ui.navigation.HabitRoute
import com.exemple.habitapp.viewmodel.HabitMainViewModel
import com.exemple.habitapp.widgets.HabitWidgetProvider

class MainActivity : ComponentActivity() {
    private val viewModel: HabitMainViewModel by viewModels()
    private var requestedRoute by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedRoute = routeFromIntent(intent)
        NotificationHelper.createChannels(this)
        ReminderScheduler.scheduleDefaultReminders(this)
        requestNotificationPermissionIfNeeded()

        setContent {
            HabitAppRoot(
                viewModel = viewModel,
                requestedRoute = requestedRoute,
                onRouteConsumed = { requestedRoute = null },
            )
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
        HabitWidgetProvider.updateAll(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        requestedRoute = routeFromIntent(intent)
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < 33) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            return
        }
        requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
    }

    private fun routeFromIntent(intent: Intent?): String? {
        intent ?: return null
        intent.getStringExtra(EXTRA_NAV_ROUTE)?.let { return it }
        return when (intent.getIntExtra("nav_target", 0)) {
            R.id.home -> HabitRoute.Home.route
            R.id.agua -> HabitRoute.Water.route
            R.id.estudos -> HabitRoute.Focus.route
            R.id.rotina -> HabitRoute.Routine.route
            R.id.habitos -> HabitRoute.Habits.route
            R.id.progresso -> HabitRoute.Progress.route
            R.id.mais -> HabitRoute.More.route
            else -> null
        }
    }

    companion object {
        const val EXTRA_NAV_ROUTE = "nav_route"
    }
}
