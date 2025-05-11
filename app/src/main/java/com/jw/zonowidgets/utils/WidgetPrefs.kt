package com.jw.zonowidgets.utils

import android.content.Context
import androidx.core.content.edit

data class DualWidgetSettings(
    val isDayNightModeEnabled: Boolean = true,
    val backgroundOpacity: Float = 1f,
)

class WidgetPrefs(context: Context) {
    private val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)

    fun setCityIds(widgetId: Int, ids: List<String>) {
        prefs.edit {
            putString("${widgetId}_timezone_ids", ids.joinToString(","))
        }
    }

    fun getCityIds(widgetId: Int): List<String> {
        val raw = prefs.getString("${widgetId}_timezone_ids", null)
        val ids = raw?.split(",") ?: emptyList()
        return ids
    }

    fun getSettings(widgetId: Int): DualWidgetSettings {
        return DualWidgetSettings(
            isDayNightModeEnabled = prefs.getBoolean("${widgetId}_day_night_enabled", true),
            backgroundOpacity = prefs.getFloat("${widgetId}_background_opacity", 1f),
        )
    }

    fun setSettings(widgetId: Int, value: DualWidgetSettings) {
        prefs.edit {
            putBoolean("${widgetId}_day_night_enabled", value.isDayNightModeEnabled)
            putFloat("${widgetId}_background_opacity", value.backgroundOpacity)
        }
    }

    fun cleanup(widgetId: Int) {
        prefs.edit {
            remove("${widgetId}_timezone_ids")
            remove("${widgetId}_day_night_enabled")
            remove("${widgetId}_background_opacity")
        }
    }
}