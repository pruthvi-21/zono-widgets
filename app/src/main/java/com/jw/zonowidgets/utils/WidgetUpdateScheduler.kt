package com.jw.zonowidgets.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.jw.zonowidgets.receivers.ScheduledUpdateReceiver

object WidgetUpdateScheduler {

    private const val TAG = "WidgetUpdateScheduler"

    fun scheduleNext(context: Context) {
        val now = System.currentTimeMillis()

        val interval = 15 * 60 * 1000 // 15 minutes
        val nextTrigger = ((now / interval) + 1) * interval

        val intent = Intent(context, ScheduledUpdateReceiver::class.java).apply {
            action = ACTION_SCHEDULED_WIDGET_UPDATE
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val can = alarmManager.canScheduleExactAlarms()

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            nextTrigger,
            pendingIntent
        )

        Log.d(TAG, "Scheduled next tick at $nextTrigger, exact allowed: $can")
    }

    fun cancel(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ScheduledUpdateReceiver::class.java).apply {
            action = ACTION_SCHEDULED_WIDGET_UPDATE
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
        Log.d(TAG, "Cancelled quarter-hour tick alarm")
    }
}