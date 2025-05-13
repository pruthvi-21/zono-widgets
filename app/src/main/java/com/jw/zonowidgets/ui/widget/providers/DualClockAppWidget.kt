package com.jw.zonowidgets.ui.widget.providers

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import com.jw.zonowidgets.data.CityRepository
import com.jw.zonowidgets.ui.widget.DualClockRemoteView
import com.jw.zonowidgets.utils.WidgetPrefs
import com.jw.zonowidgets.utils.WidgetUpdateScheduler
import java.time.ZoneId

class DualClockAppWidget : AppWidgetProvider() {

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
        WidgetUpdateScheduler.scheduleNext(context)
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
        private const val TAG = "DualClockAppWidget"

        /**
         * Updates the UI of a specific widget instance.
         */
        fun refreshWidget(context: Context, widgetId: Int) {
            AppWidgetManager
                .getInstance(context)
                .updateAppWidget(widgetId, DualClockRemoteView(context, widgetId))
        }

        fun getWidgetIds(context: Context): IntArray {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            return appWidgetManager.getAppWidgetIds(
                ComponentName(context, DualClockAppWidget::class.java)
            ) ?: intArrayOf()
        }

        fun getAllWidgetTimeZones(context: Context): Set<ZoneId> {
            val prefs = WidgetPrefs(context)
            val widgetIds = getWidgetIds(context)

            val allZoneIds = widgetIds.flatMap { widgetId ->
                prefs.getCityIds(widgetId).mapNotNull { cityId ->
                    CityRepository.getCityById(cityId)?.timeZoneId?.let { zoneId ->
                        runCatching { ZoneId.of(zoneId) }.getOrNull()
                    }
                }
            }.toSet()

            return allZoneIds
        }
    }
}