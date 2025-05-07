package com.jw.zonowidgets.utils

import android.content.Context
import androidx.core.content.edit

class WidgetPrefs(context: Context) {
    private val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)

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
}