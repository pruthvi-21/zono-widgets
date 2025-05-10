package com.jw.zonowidgets.ui.viewmodel

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jw.zonowidgets.data.CityRepository
import com.jw.zonowidgets.data.model.CityTimeZoneInfo
import com.jw.zonowidgets.ui.widget.DualClockAppWidget
import com.jw.zonowidgets.utils.WidgetPrefs
import com.jw.zonowidgets.utils.getCityName
import com.jw.zonowidgets.utils.getCountryName

class ClockSettingsViewModel(
    private val widgetId: Int,
    private val prefs: WidgetPrefs,
) : ViewModel() {

    private val _firstTimeZoneInfo = mutableStateOf(loadOrDefaultTimeZone(1))
    val firstTimeZoneInfo by _firstTimeZoneInfo

    private val _secondTimeZoneInfo = mutableStateOf(loadOrDefaultTimeZone(2))
    val secondTimeZoneInfo by _secondTimeZoneInfo

    private val _isDayNightModeEnabled = mutableStateOf(prefs.getDayNightSwitch(widgetId))
    val isDayNightModeEnabled by _isDayNightModeEnabled

    private val _opacityValue = mutableFloatStateOf(prefs.getBackgroundOpacity(widgetId))
    val opacityValue by _opacityValue

    private val _timezoneBeingEdited = mutableIntStateOf(1)
    val timezoneBeingEdited by _timezoneBeingEdited

    fun updateFirstTimeZone(info: CityTimeZoneInfo) {
        _firstTimeZoneInfo.value = info
    }

    fun updateSecondTimeZone(info: CityTimeZoneInfo) {
        _secondTimeZoneInfo.value = info
    }

    fun toggleDayNight() {
        _isDayNightModeEnabled.value = !isDayNightModeEnabled
    }

    fun updateOpacity(value: Float) {
        _opacityValue.floatValue = value
    }

    fun setTimezoneBeingEdited(index: Int) {
        _timezoneBeingEdited.intValue = index
    }

    fun saveSettings() {
        prefs.setCityIdAt(widgetId, 1, firstTimeZoneInfo.id)
        prefs.setCityIdAt(widgetId, 2, secondTimeZoneInfo.id)
        prefs.setDayNightSwitch(widgetId, isDayNightModeEnabled)
        prefs.setBackgroundOpacity(widgetId, opacityValue)
    }

    private fun loadOrDefaultTimeZone(position: Int): CityTimeZoneInfo {
        val savedId = prefs.getCityIdAt(widgetId, position)
        return CityRepository.getCityById(savedId)
            ?: CityRepository.defaultCity.also {
            prefs.setCityIdAt(widgetId, position, it.id)
        }
    }

    fun getFirstCityName(context: Context): String {
        var cityName = firstTimeZoneInfo.getCityName(context)
        val countryName = firstTimeZoneInfo.getCountryName(context)

        if (countryName.isNotEmpty()) cityName += ", $countryName"

        return cityName
    }

    fun getSecondCityName(context: Context): String {
        var cityName = secondTimeZoneInfo.getCityName(context)
        val countryName = secondTimeZoneInfo.getCountryName(context)

        if (countryName.isNotEmpty()) cityName += ", $countryName"

        return cityName
    }

    fun refreshWidget(context: Context, widgetId: Int) {
        val manager = AppWidgetManager.getInstance(context)
        val info = manager.getAppWidgetInfo(widgetId)
        val providerClassName = info?.provider?.className

        when (providerClassName) {
            DualClockAppWidget::class.java.name -> {
                DualClockAppWidget.refreshWidget(context, widgetId)
            }
        }
    }
}

class ClockSettingsViewModelFactory(
    private val widgetId: Int,
    private val prefs: WidgetPrefs,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ClockSettingsViewModel(widgetId, prefs) as T
    }
}