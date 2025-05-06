package com.jw.zonowidgets.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jw.zonowidgets.data.CityRepository
import com.jw.zonowidgets.data.model.CityTimeZoneInfo
import com.jw.zonowidgets.utils.WidgetPrefs

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

    private val _is24HourFormatEnabled = mutableStateOf(prefs.getUse24HourFormat(widgetId))
    val is24HourFormatEnabled by _is24HourFormatEnabled

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

    fun toggle24HourFormat() {
        _is24HourFormatEnabled.value = !is24HourFormatEnabled
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
        prefs.setUse24HourFormat(widgetId, is24HourFormatEnabled)
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
}

class ClockSettingsViewModelFactory(
    private val widgetId: Int,
    private val prefs: WidgetPrefs,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ClockSettingsViewModel(widgetId, prefs) as T
    }
}