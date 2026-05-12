package com.exemple.habitapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String type = intent.getStringExtra(ReminderScheduler.EXTRA_TYPE);
        if (type == null) {
            type = ReminderScheduler.TYPE_ROUTINE;
        }
        NotificationHelper.showReminder(context, type);
    }
}
