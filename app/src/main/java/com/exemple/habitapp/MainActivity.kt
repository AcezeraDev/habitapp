package com.exemple.habitapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.exemple.habitapp.notifications.NotificationHelper
import com.exemple.habitapp.notifications.ReminderScheduler
import com.exemple.habitapp.ui.compose.HabitAppRoot
import com.exemple.habitapp.viewmodel.HabitMainViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: HabitMainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationHelper.createChannels(this)
        ReminderScheduler.scheduleDefaultReminders(this)
        requestNotificationPermissionIfNeeded()

        setContent {
            HabitAppRoot(viewModel = viewModel)
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < 33) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            return
        }
        requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
    }
}
