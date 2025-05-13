package com.jw.zonowidgets.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.jw.zonowidgets.receivers.WidgetUpdateReceiver
import com.jw.zonowidgets.ui.widget.providers.DualClockAppWidget
import java.util.Date

object WidgetUpdateScheduler {

    private const val TAG = "WidgetUpdateScheduler"

    fun scheduleNext(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: run {
            Log.w(TAG, "AlarmManager not available.")
            return
        }

        val allZoneIds = DualClockAppWidget.getAllWidgetTimeZones(context)
        if (allZoneIds.isEmpty()) {
            Log.i(TAG, "No time zones found. Skipping scheduling.")
            return
        }

        val nextTriggerMillis = getNextTriggerInUtc(allZoneIds.toList())

        if (nextTriggerMillis != null && alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                nextTriggerMillis,
                getPendingIntent(context)
            )
            Log.i(TAG, "Scheduled exact alarm at ${Date(nextTriggerMillis)}")
        } else {
            Log.w(
                TAG,
                "Exact alarm not possible or no valid time found. Falling back to 15-minute interval."
            )

            val now = System.currentTimeMillis()
            val interval = 15 * 60 * 1000L // 15 minutes
            val fallbackTrigger = ((now + interval - 1) / interval) * interval // Round to next quarter

            alarmManager.setInexactRepeating(
                AlarmManager.RTC,
                fallbackTrigger,
                AlarmManager.INTERVAL_HALF_HOUR,
                getPendingIntent(context)
            )
        }
    }

    fun cancel(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(getPendingIntent(context))
        Log.i(TAG, "Cancelled $ACTION_SCHEDULED_WIDGET_UPDATE")
    }

    private fun getPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, WidgetUpdateReceiver::class.java).apply {
            action = ACTION_SCHEDULED_WIDGET_UPDATE
        }

        return PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}