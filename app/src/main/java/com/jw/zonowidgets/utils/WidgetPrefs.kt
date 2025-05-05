package com.jw.zonowidgets.utils

import android.content.Context
import android.icu.util.TimeZone
import androidx.core.content.edit
import com.jw.zonowidgets.data.model.CityTimeZoneInfo

class WidgetPrefs(context: Context) {
    private val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)

    fun getUse24HourFormat(widgetId: Int): Boolean =
        prefs.getBoolean("use_24_hour_$widgetId", false)

    fun setUse24HourFormat(widgetId: Int, value: Boolean) {
        prefs.edit { putBoolean("use_24_hour_$widgetId", value) }
    }

    fun getDayNightSwitch(widgetId: Int): Boolean =
        prefs.getBoolean("day_night_switch_$widgetId", true)

    fun setDayNightSwitch(widgetId: Int, value: Boolean) {
        prefs.edit { putBoolean("day_night_switch_$widgetId", value) }
    }

    fun getCityIdAt(widgetId: Int, position: Int): String? =
        prefs.getString("item${position}_${widgetId}", null)

    fun setCityIdAt(widgetId: Int, position: Int, id: String) {
        prefs.edit { putString("item${position}_${widgetId}", id) }
    }

    fun getBackgroundOpacity(widgetId: Int): Float =
        prefs.getFloat("background_opacity_$widgetId", 1f)

    fun setBackgroundOpacity(widgetId: Int, value: Float) {
        prefs.edit { putFloat("background_opacity_$widgetId", value) }
    }

    companion object {
        val DEFAULT_CITY: CityTimeZoneInfo by lazy {
            val currentTimeZoneId = TimeZone.getDefault().id
            World.cities.find { it.timeZoneId == currentTimeZoneId } ?: World.cities.first()
        }
    }
}