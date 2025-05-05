package com.jw.zonowidgets.receivers

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.jw.zonowidgets.ui.widget.DualClockAppWidget
import com.jw.zonowidgets.utils.WidgetUpdateScheduler

class BootOrUpdateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
                -> {
                val manager = AppWidgetManager.getInstance(context)
                val ids =
                    manager.getAppWidgetIds(ComponentName(context, DualClockAppWidget::class.java))

                ids.forEach { DualClockAppWidget.refreshWidget(context, it) }

                WidgetUpdateScheduler.cancel(context)
                WidgetUpdateScheduler.scheduleNext(context)
            }
        }
    }
}