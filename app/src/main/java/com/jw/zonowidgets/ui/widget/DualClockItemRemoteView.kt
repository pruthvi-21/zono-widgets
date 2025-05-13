package com.jw.zonowidgets.ui.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.res.Configuration
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import com.jw.zonowidgets.R
import com.jw.zonowidgets.data.model.CityTimeZoneInfo
import com.jw.zonowidgets.utils.DAY_START
import com.jw.zonowidgets.utils.DualWidgetSettings
import com.jw.zonowidgets.utils.NIGHT_START
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

    private val placeViewId: Int
    private val dateViewId: Int
    private val timeViewId: Int
    private val ampmViewId: Int
    private val flagViewId: Int
    private val iconViewId: Int
    private val backgroundViewId: Int

    init {
        ampmViewId = getAmPmId()
        placeViewId = R.id.place
        dateViewId = R.id.date
        timeViewId = R.id.time
        flagViewId = R.id.flag_view
        iconViewId = R.id.icon
        backgroundViewId = R.id.background
        setupBaseTimeZoneBindings()
        applyForegroundStyling()
        applyBackgroundStyling()
    }

    // --------------------
    // Setup methods
    // --------------------

    private fun setupBaseTimeZoneBindings() {
        setTextViewText(placeViewId, cityTimeZone.getCityName(context))
        setString(dateViewId, "setTimeZone", zoneId.id)
        setString(timeViewId, "setTimeZone", zoneId.id)
        setString(ampmViewId, "setTimeZone", zoneId.id)
    }

    private fun applyForegroundStyling() {
        if (settings.isDayNightModeEnabled) {
            val textColor = getDayNightTextColor()
            setTextColor(placeViewId, textColor)
            setTextColor(dateViewId, textColor)
            setTextColor(timeViewId, textColor)
            setTextColor(ampmViewId, textColor)
        }

        setImageViewResource(iconViewId, getDayNightIcon())
        setTextViewText(flagViewId, getFlagEmoji(cityTimeZone.isoCode))

        adjustTextSizeAndVisibilityBasedOnWidth()
    }

    private fun getAmPmId(): Int {
        // Some languages (e.g., Japanese, Korean, Chinese) place AM/PM before the time.
        // To handle this, we include both AM/PM positions (start and end) in the layout and
        // toggle visibility based on the current locale at runtime.
        //
        // Originally, we attempted to dynamically set `layoutDirection` on the time container
        // through code based on locale, but this only took effect after a full widget refresh.
        // To ensure reliable rendering on every update, we adopted this dual-view approach instead.
        val locale = Locale.getDefault()

        return if (locale.language in LANGUAGES_WITH_LEADING_AMPM) {
            setViewVisibility(R.id.amPmText1, View.VISIBLE)
            setViewVisibility(R.id.amPmText, View.GONE)
            R.id.amPmText1
        } else {
            setViewVisibility(R.id.amPmText, View.VISIBLE)
            setViewVisibility(R.id.amPmText1, View.GONE)
            R.id.amPmText
        }
    }

    private fun applyBackgroundStyling() {
        if (settings.isDayNightModeEnabled) {
            setInt(backgroundViewId, "setBackgroundResource", getDayNightBackground())
        }
        setFloat(backgroundViewId, "setAlpha", settings.backgroundOpacity)
    }

    // --------------------
    // Helper methods
    // --------------------

    private fun adjustTextSizeAndVisibilityBasedOnWidth() {
        val config = Configuration(context.resources.configuration).apply { fontScale = 1f }
        val newContext = context.createConfigurationContext(config)
        val resources = newContext.resources

        if (width in Int.MIN_VALUE..ICON_HIDDEN_THRESHOLD) {
            setViewVisibility(flagViewId, View.GONE)
            setViewVisibility(iconViewId, View.GONE)
        } else {
            setViewVisibility(flagViewId, View.VISIBLE)
            setViewVisibility(iconViewId, View.VISIBLE)
        }

        if (isCompact) {
            val placeFontSize = resources.getDimension(R.dimen.widget_place_font_size_compact)
            val dateFontSize = resources.getDimension(R.dimen.widget_date_font_size_compact)
            val timeFontSize = resources.getDimension(R.dimen.widget_time_font_size_compact)
            val ampmFontSize = resources.getDimension(R.dimen.widget_ampm_font_size_compact)

            setTextViewTextSize(placeViewId, TypedValue.COMPLEX_UNIT_PX, placeFontSize)
            setTextViewTextSize(dateViewId, TypedValue.COMPLEX_UNIT_PX, dateFontSize)
            setTextViewTextSize(timeViewId, TypedValue.COMPLEX_UNIT_PX, timeFontSize)
            setTextViewTextSize(ampmViewId, TypedValue.COMPLEX_UNIT_PX, ampmFontSize)
        } else {
            val placeFontSize = resources.getDimension(R.dimen.widget_place_font_size)
            val dateFontSize = resources.getDimension(R.dimen.widget_date_font_size)

            setTextViewTextSize(placeViewId, TypedValue.COMPLEX_UNIT_PX, placeFontSize)
            setTextViewTextSize(dateViewId, TypedValue.COMPLEX_UNIT_PX, dateFontSize)

            when (width) {
                in Int.MIN_VALUE..SMALL_WIDTH_THRESHOLD -> {
                    val timeFontSizeMin = resources.getDimension(R.dimen.widget_time_font_size_min)
                    val ampmFontSizeMin = resources.getDimension(R.dimen.widget_ampm_font_size_min)
                    val flagFontSizeMin = resources.getDimension(R.dimen.widget_flag_font_size_min)

                    setTextViewTextSize(timeViewId, TypedValue.COMPLEX_UNIT_PX, timeFontSizeMin)
                    setTextViewTextSize(ampmViewId, TypedValue.COMPLEX_UNIT_PX, ampmFontSizeMin)
                    setTextViewTextSize(flagViewId, TypedValue.COMPLEX_UNIT_PX, flagFontSizeMin)
                }

                else -> {
                    val timeFontSize = resources.getDimension(R.dimen.widget_time_font_size)
                    val ampmFontSize = resources.getDimension(R.dimen.widget_ampm_font_size)
                    val flagFontSize = resources.getDimension(R.dimen.widget_flag_font_size)

                    setTextViewTextSize(timeViewId, TypedValue.COMPLEX_UNIT_PX, timeFontSize)
                    setTextViewTextSize(ampmViewId, TypedValue.COMPLEX_UNIT_PX, ampmFontSize)
                    setTextViewTextSize(flagViewId, TypedValue.COMPLEX_UNIT_PX, flagFontSize)
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
            return hour in DAY_START..<NIGHT_START
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