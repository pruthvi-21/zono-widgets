package com.jw.zonowidgets.ui.widget.providers

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.os.Bundle
import com.jw.zonowidgets.ui.widget.DualClockRemoteView
import com.jw.zonowidgets.utils.WidgetPrefs
import com.jw.zonowidgets.utils.WidgetUpdateScheduler

class DualClockAppWidget : AppWidgetProvider() {

    // Called when the first widget is added to the home screen
    override fun onEnabled(context: Context) {
        WidgetUpdateScheduler.scheduleNext(context)
    }

    // Called when the last widget is removed
    override fun onDisabled(context: Context) {
        WidgetUpdateScheduler.cancel(context)
    }

    // Called when the widget needs to be updated (periodic or manual)
    override fun onUpdate(context: Context, manager: AppWidgetManager, widgetIds: IntArray) {
        widgetIds.forEach { widgetId ->
            refreshWidget(context, widgetId)
        }
    }

    // Called when a widget instance is deleted
    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        appWidgetIds.forEach { widgetId ->
            WidgetPrefs(context).cleanup(widgetId)
        }
    }

    // Called when the widget is resized or its options are changed
    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle,
    ) {
        refreshWidget(context, appWidgetId)
    }

    companion object {
        /**
         * Updates the UI of a specific widget instance.
         */
        fun refreshWidget(context: Context, widgetId: Int) {
            AppWidgetManager
                .getInstance(context)
                .updateAppWidget(widgetId, DualClockRemoteView(context, widgetId))
        }
    }
}