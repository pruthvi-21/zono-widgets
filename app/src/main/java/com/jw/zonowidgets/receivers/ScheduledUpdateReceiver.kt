package com.jw.zonowidgets.receivers

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.jw.zonowidgets.ui.widget.DualClockAppWidget
import com.jw.zonowidgets.utils.ACTION_SCHEDULED_WIDGET_UPDATE
import com.jw.zonowidgets.utils.WidgetUpdateScheduler

class ScheduledUpdateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_SCHEDULED_WIDGET_UPDATE) {
            val manager = AppWidgetManager.getInstance(context)
            val provider = ComponentName(context, DualClockAppWidget::class.java)
            val ids = manager.getAppWidgetIds(provider)

            ids.forEach {
                DualClockAppWidget.refreshWidget(context, it)
            }

            WidgetUpdateScheduler.scheduleNext(context)
        }
    }
}