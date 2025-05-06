package com.jw.zonowidgets.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.jw.zonowidgets.R
import com.jw.zonowidgets.ui.activities.ClockSettingsActivity
import com.jw.zonowidgets.utils.WidgetPrefs
import com.jw.zonowidgets.utils.WidgetUpdateScheduler
import com.jw.zonowidgets.utils.World
import java.time.ZoneId
import java.time.ZonedDateTime

class DualClockAppWidget : AppWidgetProvider() {

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WidgetUpdateScheduler.scheduleNext(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        WidgetUpdateScheduler.cancel(context)
    }

    override fun onUpdate(context: Context, manager: AppWidgetManager, widgetIds: IntArray) {
        super.onUpdate(context, manager, widgetIds)
        widgetIds.forEach { refreshWidget(context, it) }
    }

    companion object {

        fun refreshWidget(context: Context, widgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_dual_clock)
            views.removeAllViews(R.id.root)

            val view1 = buildRemoteView(context, widgetId, 1)
            val view2 = buildRemoteView(context, widgetId, 2)

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

        private fun buildRemoteView(context: Context, widgetId: Int, position: Int): RemoteViews {
            val prefs = WidgetPrefs(context)

            val id = prefs.getCityIdAt(widgetId, position)
            val cityTimeZone = World.cities.firstOrNull { it.id == id } ?: WidgetPrefs.DEFAULT_CITY

            return RemoteViews(context.packageName, R.layout.widget_clock).apply {
                val place = cityTimeZone.city.substringBefore("/")
                setTextViewText(R.id.place, place)

                val tz = ZoneId.of(cityTimeZone.timeZoneId)

                setString(R.id.date, "setTimeZone", tz.id)
                setString(R.id.time, "setTimeZone", tz.id)
                setString(R.id.amPmText, "setTimeZone", tz.id)

                val isDayNightEnabled = prefs.getDayNightSwitch(widgetId)
                val use24Hour = prefs.getUse24HourFormat(widgetId)
                val backgroundOpacity = prefs.getBackgroundOpacity(widgetId)

                setFloat(R.id.background, "setAlpha", backgroundOpacity)

                if (isDayNightEnabled) {
                    val now = ZonedDateTime.now(tz)
                    val hour = now.hour
                    val isDayTime = hour in 6..17

                    val color = if (isDayTime) context.getColor(R.color.text_color_day)
                    else context.getColor(R.color.text_color_night)

                    setTextColor(R.id.place, color)
                    setTextColor(R.id.date, color)
                    setTextColor(R.id.time, color)
                    setTextColor(R.id.amPmText, color)

                    val backgroundRes =
                        if (isDayTime) R.drawable.bg_widget_clock_day
                        else R.drawable.bg_widget_clock_night
                    setInt(R.id.background, "setBackgroundResource", backgroundRes)

                    val iconRes = if (isDayTime) R.drawable.ic_sun_24dp else R.drawable.ic_moon_24dp
                    setImageViewResource(R.id.icon, iconRes)
                }

                if (use24Hour) {
                    setCharSequence(R.id.time, "setFormat24Hour", "HH:mm")
                    setCharSequence(R.id.time, "setFormat12Hour", null)
                    setInt(R.id.amPmText, "setVisibility", View.GONE)
                } else {
                    setCharSequence(R.id.time, "setFormat12Hour", "hh:mm")
                    setCharSequence(R.id.time, "setFormat24Hour", null)
                    setInt(R.id.amPmText, "setVisibility", View.VISIBLE)
                }
            }
        }
    }
}