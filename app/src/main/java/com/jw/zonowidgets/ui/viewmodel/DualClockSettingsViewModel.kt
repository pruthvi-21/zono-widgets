package com.jw.zonowidgets.ui.viewmodel

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jw.zonowidgets.data.CityRepository
import com.jw.zonowidgets.data.model.CityTimeZoneInfo
import com.jw.zonowidgets.ui.widget.providers.DualClockAppWidget
import com.jw.zonowidgets.utils.DualWidgetSettings
import com.jw.zonowidgets.utils.WidgetPrefs
import com.jw.zonowidgets.utils.WidgetUpdateScheduler
import com.jw.zonowidgets.utils.getCityName
import com.jw.zonowidgets.utils.getCountryName

/**
 * ViewModel for managing dual clock widget settings.
 */
class DualClockSettingsViewModel(
    private val widgetId: Int,
    private val prefs: WidgetPrefs,
) : ViewModel() {

    private val _firstTimeZoneInfo = mutableStateOf(CityRepository.defaultCity)
    private val _secondTimeZoneInfo = mutableStateOf(CityRepository.defaultCity)

    private val _settings = mutableStateOf(DualWidgetSettings())
    val settings by _settings

    private val _editingClockIndex = mutableIntStateOf(-1)

    init {
        val savedIds = prefs.getCityIds(widgetId)
        val firstCity = savedIds.getOrNull(0)?.let { CityRepository.getCityById(it) }
        val secondCity = savedIds.getOrNull(1)?.let { CityRepository.getCityById(it) }

        if (firstCity != null) _firstTimeZoneInfo.value = firstCity
        if (secondCity != null) _secondTimeZoneInfo.value = secondCity

        _settings.value = prefs.getSettings(widgetId)
    }

    fun toggleDayNight() {
        _settings.value = _settings.value.copy(
            isDayNightModeEnabled = !_settings.value.isDayNightModeEnabled
        )
    }

    fun updateOpacity(value: Float) {
        _settings.value = _settings.value.copy(
            backgroundOpacity = value
        )
    }

    fun setEditingClockIndex(index: Int) {
        _editingClockIndex.intValue = index
    }

    fun onCitySelected(id: String) {
        val selected = CityRepository.getCityById(id) ?: return
        when (_editingClockIndex.intValue) {
            FIRST_CLOCK -> _firstTimeZoneInfo.value = selected
            SECOND_CLOCK -> _secondTimeZoneInfo.value = selected
        }
    }

    fun getFirstCityName(context: Context): String {
        return formatCityName(context, _firstTimeZoneInfo.value)
    }

    fun getSecondCityName(context: Context): String {
        return formatCityName(context, _secondTimeZoneInfo.value)
    }

    private fun formatCityName(context: Context, timeZoneInfo: CityTimeZoneInfo): String {
        var cityName = timeZoneInfo.getCityName(context)
        val countryName = timeZoneInfo.getCountryName(context)
        if (countryName.isNotEmpty()) cityName += ", $countryName"
        return cityName
    }

    fun saveSettings() {
        val cityIds = listOf(_firstTimeZoneInfo.value.id, _secondTimeZoneInfo.value.id)
        prefs.setCityIds(widgetId, cityIds)
        prefs.setSettings(widgetId, settings)
    }

    fun refreshWidget(context: Context, widgetId: Int) {
        val providerClassName = AppWidgetManager.getInstance(context)
            .getAppWidgetInfo(widgetId)
            ?.provider
            ?.className ?: return

        if (providerClassName == DualClockAppWidget::class.java.name) {
            DualClockAppWidget.refreshWidget(context, widgetId)
        }
        WidgetUpdateScheduler.scheduleNext(context)
    }

    companion object {
        const val FIRST_CLOCK = 0
        const val SECOND_CLOCK = 1
    }
}

class DualClockSettingsViewModelFactory(
    private val widgetId: Int,
    private val prefs: WidgetPrefs,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DualClockSettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DualClockSettingsViewModel(widgetId, prefs) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}