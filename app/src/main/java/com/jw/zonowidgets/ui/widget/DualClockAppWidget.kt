package com.jw.zonowidgets.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.core.graphics.toColorInt
import com.jw.zonowidgets.R
import com.jw.zonowidgets.ui.activities.ClockSettingsActivity
import com.jw.zonowidgets.utils.PREFERENCES_NAME
import com.jw.zonowidgets.utils.World
import java.time.ZoneId
import java.time.ZonedDateTime

class DualClockAppWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, widgetIds: IntArray) {
        super.onUpdate(context, manager, widgetIds)
        widgetIds.forEach { updateWidget(context, it) }
    }

    companion object {

        fun updateWidget(context: Context, widgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_dual_clock)
            views.removeAllViews(R.id.root)

            val view1 = buildRemoteView(context, widgetId, "${widgetId}_item1")
            val view2 = buildRemoteView(context, widgetId, "${widgetId}_item2")

            views.addView(R.id.root, view1)
            views.addView(R.id.root, view2)

            val configIntent = Intent(context, ClockSettingsActivity::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            }

            val configPendingIntent = PendingIntent.getActivity(
                context,
                widgetId,
                configIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            views.setOnClickPendingIntent(R.id.root, configPendingIntent)

            AppWidgetManager.getInstance(context).updateAppWidget(widgetId, views)
        }

        private fun buildRemoteView(context: Context, widgetId: Int, key: String): RemoteViews {
            val prefs = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

            val id = prefs.getString(key, null)
            val cityTimeZone = World.cities.firstOrNull { it.id == id }

            return RemoteViews(context.packageName, R.layout.widget_clock).apply {
                if (cityTimeZone != null) {
                    val place = cityTimeZone.city.substringBefore("/")
                    setTextViewText(R.id.place, place)

                    val tz = ZoneId.of(cityTimeZone.timeZoneId)

                    setString(R.id.date, "setTimeZone", tz.id)
                    setString(R.id.time, "setTimeZone", tz.id)
                    setString(R.id.amPmText, "setTimeZone", tz.id)

                    val isDayNightEnabled = prefs.getBoolean("${widgetId}_day_night_switch", true)

                    if (isDayNightEnabled) {
                        val now = ZonedDateTime.now(tz)
                        val hour = now.hour
                        val isDayTime = hour in 6..17

                        val color =
                            if (isDayTime) "#FF9C00".toColorInt() else "#C1C0FD".toColorInt()
                        setTextColor(R.id.place, color)
                        setTextColor(R.id.date, color)
                        setTextColor(R.id.time, color)
                        setTextColor(R.id.amPmText, color)

                        val backgroundRes =
                            if (isDayTime) R.drawable.bg_widget_clock_day
                            else R.drawable.bg_widget_clock_night
                        setInt(R.id.root, "setBackgroundResource", backgroundRes)

                        val iconRes =
                            if (isDayTime) R.drawable.ic_sun_24dp else R.drawable.ic_moon_24dp
                        setImageViewResource(R.id.icon, iconRes)
                    }
                }
            }
        }
    }
}