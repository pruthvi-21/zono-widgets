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
import com.jw.zonowidgets.ui.activities.DualClockSettingsActivity
import com.jw.zonowidgets.utils.WidgetPrefs
import com.jw.zonowidgets.utils.WidgetUpdateScheduler
import com.jw.zonowidgets.utils.getCityName
import com.jw.zonowidgets.utils.getFlagEmoji
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
        // Languages where AM/PM modifier appears before the time (e.g., "午前 10:30")
        private val LANGUAGES_WITH_LEADING_AMPM = setOf("ja", "ko", "zh")

        fun refreshWidget(context: Context, widgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_dual_clock).apply {
                removeAllViews(R.id.root)

                addView(R.id.root, buildRemoteView(context, widgetId, 1))
                addView(R.id.root, buildRemoteView(context, widgetId, 2))
            }

            val configIntent = Intent(context, DualClockSettingsActivity::class.java).apply {
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

            // Get user settings for this widget
            val id = prefs.getCityIdAt(widgetId, position)
            val isDayNightEnabled = prefs.getDayNightSwitch(widgetId)
            val backgroundOpacity = prefs.getBackgroundOpacity(widgetId)

            val cityTimeZone = CityRepository.getCityById(id) ?: CityRepository.defaultCity
            val tz = ZoneId.of(cityTimeZone.timeZoneId)
            val isDayTime = isDayTime(tz)

            return RemoteViews(context.packageName, getLayoutId()).apply {
                setTextViewText(R.id.place, cityTimeZone.getCityName(context))

                setString(R.id.date, "setTimeZone", tz.id)
                setString(R.id.time, "setTimeZone", tz.id)
                setString(R.id.amPmText, "setTimeZone", tz.id)

                setLayoutDirections(this)
                setForeground(this, context, isDayNightEnabled, isDayTime, cityTimeZone.isoCode)
                setBackground(this, isDayNightEnabled, isDayTime, backgroundOpacity)
            }
        }

        private fun setForeground(
            remoteViews: RemoteViews,
            context: Context,
            isDayNightEnabled: Boolean,
            isDayTime: Boolean,
            isoCode: String,
        ) {
            if (isDayNightEnabled) {
                val color = if (isDayTime) context.getColor(R.color.text_color_day)
                else context.getColor(R.color.text_color_night)

                remoteViews.setTextColor(R.id.place, color)
                remoteViews.setTextColor(R.id.date, color)
                remoteViews.setTextColor(R.id.time, color)
                remoteViews.setTextColor(R.id.amPmText, color)
            }

            val iconRes = if (isDayTime) R.drawable.ic_sun_24dp else R.drawable.ic_moon_24dp
            remoteViews.setImageViewResource(R.id.icon, iconRes)

            remoteViews.setTextViewText(R.id.flag_view, getFlagEmoji(isoCode))
        }

        private fun isDayTime(tz: ZoneId): Boolean {
            val hour = ZonedDateTime.now(tz).hour
            return hour in 6..17
        }

        private fun getLayoutId() = R.layout.widget_dual_clock_item

        // Adjusts layout direction for proper language and AM/PM placement
        private fun setLayoutDirections(remoteViews: RemoteViews) {
            val locale = Locale.getDefault()
            val isSystemRtl = locale.layoutDirection == View.LAYOUT_DIRECTION_RTL

            // For certain languages (e.g., Japanese), reverse direction to show AM/PM properly
            val timeLayoutDirection = if (locale.language in LANGUAGES_WITH_LEADING_AMPM) {
                if (isSystemRtl) View.LAYOUT_DIRECTION_LTR else View.LAYOUT_DIRECTION_RTL
            } else locale.layoutDirection

            remoteViews.setInt(R.id.time_container, "setLayoutDirection", timeLayoutDirection)
        }

        private fun setBackground(
            remoteViews: RemoteViews,
            isDayNightEnabled: Boolean,
            isDayTime: Boolean,
            backgroundOpacity: Float,
        ) {
            if (isDayNightEnabled) {
                val res = if (isDayTime) R.drawable.bg_widget_clock_day
                else R.drawable.bg_widget_clock_night
                remoteViews.setInt(R.id.background, "setBackgroundResource", res)
            }

            remoteViews.setFloat(R.id.background, "setAlpha", backgroundOpacity)
        }
    }
}