package com.jw.zonowidgets.ui.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.res.Configuration
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import androidx.core.text.layoutDirection
import com.jw.zonowidgets.R
import com.jw.zonowidgets.data.model.CityTimeZoneInfo
import com.jw.zonowidgets.utils.DualWidgetSettings
import com.jw.zonowidgets.utils.getCityName
import com.jw.zonowidgets.utils.getFlagEmoji
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Locale

class DualClockItemRemoteView(
    private val context: Context,
    private val widgetId: Int,
    layoutId: Int,
    private val isCompact: Boolean,
    private val cityTimeZone: CityTimeZoneInfo,
    private val settings: DualWidgetSettings,
) : RemoteViews(context.packageName, layoutId) {

    private val zoneId = ZoneId.of(cityTimeZone.timeZoneId)

    init {
        setupBaseTimeZoneBindings()
        applyForegroundStyling()
        applyBackgroundStyling()
        applyLayoutDirection()
    }

    // --------------------
    // Setup methods
    // --------------------

    private fun setupBaseTimeZoneBindings() {
        setTextViewText(R.id.place, cityTimeZone.getCityName(context))
        setString(R.id.date, "setTimeZone", zoneId.id)
        setString(R.id.time, "setTimeZone", zoneId.id)
        setString(R.id.amPmText, "setTimeZone", zoneId.id)
    }

    private fun applyForegroundStyling() {
        if (settings.isDayNightModeEnabled) {
            val textColor = getDayNightTextColor()
            setTextColor(R.id.place, textColor)
            setTextColor(R.id.date, textColor)
            setTextColor(R.id.time, textColor)
            setTextColor(R.id.amPmText, textColor)
        }

        setImageViewResource(R.id.icon, getDayNightIcon())
        setTextViewText(R.id.flag_view, getFlagEmoji(cityTimeZone.isoCode))

        adjustTextSizeAndVisibilityBasedOnWidth()
    }

    private fun applyLayoutDirection() {
        val locale = Locale.getDefault()
        val isRtl = locale.layoutDirection == View.LAYOUT_DIRECTION_RTL

        val layoutDir = if (locale.language in LANGUAGES_WITH_LEADING_AMPM) {
            if (isRtl) View.LAYOUT_DIRECTION_LTR else View.LAYOUT_DIRECTION_RTL
        } else locale.layoutDirection

        setInt(R.id.time_container, "setLayoutDirection", layoutDir)
    }

    private fun applyBackgroundStyling() {
        if (settings.isDayNightModeEnabled) {
            setInt(R.id.background, "setBackgroundResource", getDayNightBackground())
        }
        setFloat(R.id.background, "setAlpha", settings.backgroundOpacity)
    }

    // --------------------
    // Helper methods
    // --------------------

    private fun adjustTextSizeAndVisibilityBasedOnWidth() {
        val config = Configuration(context.resources.configuration).apply { fontScale = 1f }
        val newContext = context.createConfigurationContext(config)
        val resources = newContext.resources

        if (width in Int.MIN_VALUE..ICON_HIDDEN_THRESHOLD) {
            setViewVisibility(R.id.flag_view, View.GONE)
            setViewVisibility(R.id.icon, View.GONE)
        } else {
            setViewVisibility(R.id.flag_view, View.VISIBLE)
            setViewVisibility(R.id.icon, View.VISIBLE)
        }

        if (isCompact) {
            val placeFontSize = resources.getDimension(R.dimen.widget_place_font_size_compact)
            val dateFontSize = resources.getDimension(R.dimen.widget_date_font_size_compact)
            val timeFontSize = resources.getDimension(R.dimen.widget_time_font_size_compact)
            val ampmFontSize = resources.getDimension(R.dimen.widget_ampm_font_size_compact)

            setTextViewTextSize(R.id.place, TypedValue.COMPLEX_UNIT_PX, placeFontSize)
            setTextViewTextSize(R.id.date, TypedValue.COMPLEX_UNIT_PX, dateFontSize)
            setTextViewTextSize(R.id.time, TypedValue.COMPLEX_UNIT_PX, timeFontSize)
            setTextViewTextSize(R.id.amPmText, TypedValue.COMPLEX_UNIT_PX, ampmFontSize)
        } else {
            val placeFontSize = resources.getDimension(R.dimen.widget_place_font_size)
            val dateFontSize = resources.getDimension(R.dimen.widget_date_font_size)

            setTextViewTextSize(R.id.place, TypedValue.COMPLEX_UNIT_PX, placeFontSize)
            setTextViewTextSize(R.id.date, TypedValue.COMPLEX_UNIT_PX, dateFontSize)

            when (width) {
                in Int.MIN_VALUE..SMALL_WIDTH_THRESHOLD -> {
                    val timeFontSizeMin = resources.getDimension(R.dimen.widget_time_font_size_min)
                    val ampmFontSizeMin = resources.getDimension(R.dimen.widget_ampm_font_size_min)
                    val flagFontSizeMin = resources.getDimension(R.dimen.widget_flag_font_size_min)

                    setTextViewTextSize(R.id.time, TypedValue.COMPLEX_UNIT_PX, timeFontSizeMin)
                    setTextViewTextSize(R.id.amPmText, TypedValue.COMPLEX_UNIT_PX, ampmFontSizeMin)
                    setTextViewTextSize(R.id.flag_view, TypedValue.COMPLEX_UNIT_PX, flagFontSizeMin)
                }

                else -> {
                    val timeFontSize = resources.getDimension(R.dimen.widget_time_font_size)
                    val ampmFontSize = resources.getDimension(R.dimen.widget_ampm_font_size)
                    val flagFontSize = resources.getDimension(R.dimen.widget_flag_font_size)

                    setTextViewTextSize(R.id.time, TypedValue.COMPLEX_UNIT_PX, timeFontSize)
                    setTextViewTextSize(R.id.amPmText, TypedValue.COMPLEX_UNIT_PX, ampmFontSize)
                    setTextViewTextSize(R.id.flag_view, TypedValue.COMPLEX_UNIT_PX, flagFontSize)
                }
            }
        }
    }

    private fun getDayNightTextColor(): Int {
        val color =
            if (isDayTime) R.color.widget_text_color_day else R.color.widget_text_color_night
        return context.getColor(color)
    }

    private fun getDayNightIcon(): Int {
        return if (isDayTime) R.drawable.ic_sun_24dp else R.drawable.ic_moon_24dp
    }

    private fun getDayNightBackground(): Int {
        return if (isDayTime) R.drawable.bg_widget_clock_day else R.drawable.bg_widget_clock_night
    }

    private val isDayTime: Boolean
        get() {
            val hour = ZonedDateTime.now(zoneId).hour
            return hour in 6..17
        }

    private val width: Int
        get() {
            val options = AppWidgetManager.getInstance(context).getAppWidgetOptions(widgetId)
            return options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
        }

    companion object {
        private val LANGUAGES_WITH_LEADING_AMPM = setOf("ja", "ko", "zh")
        private const val ICON_HIDDEN_THRESHOLD = 313
        private const val SMALL_WIDTH_THRESHOLD = 343
    }
}