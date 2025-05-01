package com.jw.zonowidgets.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.jw.zonowidgets.utils.CITY_TIME_ZONES
import com.jw.zonowidgets.utils.PREFERENCES_NAME
import com.jw.zonowidgets.R
import com.jw.zonowidgets.ui.activities.ClockSettingsActivity


class DualClockAppWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, widgetIds: IntArray) {
        super.onUpdate(context, manager, widgetIds)
        widgetIds.forEach { updateWidget(context, it) }
    }

    companion object {

        fun updateWidget(context: Context, widgetId: Int) {

            val views = RemoteViews(context.packageName, R.layout.widget_dual_clock)
            views.removeAllViews(R.id.root)

            val set1 = buildRemoteView(context, "${widgetId}_item1")
            val set2 = buildRemoteView(context, "${widgetId}_item2")
            views.addView(R.id.root, set1)
            views.addView(R.id.root, set2)

            val configIntent = Intent(context, ClockSettingsActivity::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                widgetId,
                configIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            views.setOnClickPendingIntent(R.id.root, pendingIntent)

            AppWidgetManager.getInstance(context).updateAppWidget(widgetId, views)
        }

        private fun buildRemoteView(context: Context, key: String): RemoteViews {
            val prefs = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

            val id = prefs.getInt(key, -1)
            val cityTimeZone = CITY_TIME_ZONES.firstOrNull { it.id == id }

            return RemoteViews(context.packageName, R.layout.widget_clock).apply {
                if (cityTimeZone != null) {
                    val place = if (!cityTimeZone.city.contains("/")) cityTimeZone.city
                    else cityTimeZone.city.split("/")[0].trim()

                    setTextViewText(R.id.place, place)
                    setString(R.id.date, "setTimeZone", cityTimeZone.timeZoneId)
                    setString(R.id.time, "setTimeZone", cityTimeZone.timeZoneId)
                    setString(R.id.amPmText, "setTimeZone", cityTimeZone.timeZoneId)
                }
            }
        }
    }
}