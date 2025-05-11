package com.jw.zonowidgets.receivers

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import com.jw.zonowidgets.ui.widget.providers.DualClockAppWidget
import com.jw.zonowidgets.utils.ACTION_SCHEDULED_WIDGET_UPDATE
import com.jw.zonowidgets.utils.WidgetUpdateScheduler

class WidgetUpdateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val widgetIds = AppWidgetManager
            .getInstance(context)
            .getAppWidgetIds(ComponentName(context, DualClockAppWidget::class.java))

        when (intent.action) {
            ACTION_SCHEDULED_WIDGET_UPDATE,
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_LOCALE_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
                -> {
                Log.d(TAG, "onReceive: Broadcast received - ${intent.action}")
                widgetIds.forEach { DualClockAppWidget.refreshWidget(context, it) }

                WidgetUpdateScheduler.scheduleNext(context)
            }
        }
    }

    companion object {
        private const val TAG = "WidgetUpdateReceiver"
    }
}