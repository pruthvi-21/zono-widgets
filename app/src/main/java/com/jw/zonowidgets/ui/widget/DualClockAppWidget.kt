package com.jw.zonowidgets.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RemoteViews
import androidx.core.text.layoutDirection
import com.jw.zonowidgets.R
import com.jw.zonowidgets.data.CityRepository
import com.jw.zonowidgets.ui.activities.ClockSettingsActivity
import com.jw.zonowidgets.utils.WidgetPrefs
import com.jw.zonowidgets.utils.WidgetUpdateScheduler
import com.jw.zonowidgets.utils.getCityName
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Locale

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

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle,
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        refreshWidget(context, appWidgetId)
    }

    companion object {

        fun refreshWidget(context: Context, widgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_dual_clock).apply {
                removeAllViews(R.id.root)

                addView(R.id.root, buildRemoteView(context, widgetId, 1))
                addView(R.id.root, buildRemoteView(context, widgetId, 2))
            }

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
            val cityTimeZone = CityRepository.getCityById(id) ?: CityRepository.defaultCity

            val minHeight = AppWidgetManager
                .getInstance(context)
                .getAppWidgetOptions(widgetId)
                .getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)

            val layoutId = if (minHeight < 100 && minHeight != 0) R.layout.widget_clock_compact
            else R.layout.widget_clock

            return RemoteViews(context.packageName, layoutId).apply {
                val cityName = cityTimeZone.getCityName(context)
                val place = cityName.substringBefore("/")
                setTextViewText(R.id.place, place)

                val tz = ZoneId.of(cityTimeZone.timeZoneId)

                setString(R.id.date, "setTimeZone", tz.id)
                setString(R.id.time, "setTimeZone", tz.id)

                val locale = Locale.getDefault()
                val isSystemRtl = locale.layoutDirection == View.LAYOUT_DIRECTION_RTL

                val layoutDirection = when (locale.language) {
                    "ja", "ko", "zh" -> if (isSystemRtl) View.LAYOUT_DIRECTION_LTR else View.LAYOUT_DIRECTION_RTL
                    else -> locale.layoutDirection
                }

                setInt(R.id.time_container, "setLayoutDirection", layoutDirection)

                setString(R.id.amPmText, "setTimeZone", tz.id)

                val isDayNightEnabled = prefs.getDayNightSwitch(widgetId)
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
            }
        }
    }
}