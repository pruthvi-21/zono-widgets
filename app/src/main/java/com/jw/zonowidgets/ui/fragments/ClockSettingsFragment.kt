package com.jw.zonowidgets.ui.fragments

import android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID
import android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import androidx.appcompat.app.AppCompatActivity.RESULT_OK
import androidx.core.content.edit
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.jw.zonowidgets.R
import com.jw.zonowidgets.data.model.CityTimeZoneInfo
import com.jw.zonowidgets.ui.preferences.TimeZonePreference
import com.jw.zonowidgets.ui.activities.TimeZonePickerActivity
import com.jw.zonowidgets.ui.widget.DualClockAppWidget
import com.jw.zonowidgets.utils.CITY_TIME_ZONES
import com.jw.zonowidgets.utils.EXTRA_SELECTED_ZONE_ID
import com.jw.zonowidgets.utils.PREFERENCES_NAME

class ClockSettingsFragment : PreferenceFragmentCompat() {

    private var firstCityPreference: TimeZonePreference? = null
    private var secondCityPreference: TimeZonePreference? = null
    private var dayNightPreference: SwitchPreference? = null

    private val prefs by lazy {
        requireContext().getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE)
    }

    private var widgetId: Int = INVALID_APPWIDGET_ID

    private var timezoneBeingEdited: Int = 1
    private val timeZonePickerIntent by lazy { Intent(context, TimeZonePickerActivity::class.java) }
    private val timeZonePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != RESULT_OK || result.data == null) return@registerForActivityResult

            val id = result.data!!.getIntExtra(EXTRA_SELECTED_ZONE_ID, -1)
            val selected = CITY_TIME_ZONES.find { it.id == id } ?: return@registerForActivityResult

            if (timezoneBeingEdited == 1) {
                prefs.edit { putInt("${widgetId}_item1", selected.id) }
                firstCityPreference?.setCityTimeZone(selected)
            } else {
                prefs.edit { putInt("${widgetId}_item2", selected.id) }
                secondCityPreference?.setCityTimeZone(selected)
            }

            DualClockAppWidget.updateWidget(requireContext(), widgetId)
        }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.clock_settings)

        widgetId =
            arguments?.getInt(EXTRA_APPWIDGET_ID, INVALID_APPWIDGET_ID) ?: INVALID_APPWIDGET_ID
        if (widgetId == INVALID_APPWIDGET_ID) {
            Toast.makeText(context, R.string.unable_to_open_settings, Toast.LENGTH_SHORT).show()
            activity?.finish()
        }

        firstCityPreference = findPreference("key_first_city")
        secondCityPreference = findPreference("key_second_city")
        dayNightPreference = findPreference("key_day_night_switch")

        val selectedTz1 = loadOrDefaultTimeZone("${widgetId}_item1")
        val selectedTz2 = loadOrDefaultTimeZone("${widgetId}_item2")

        firstCityPreference?.setCityTimeZone(selectedTz1)
        secondCityPreference?.setCityTimeZone(selectedTz2)

        dayNightPreference?.isChecked = prefs.getBoolean("${widgetId}_day_night_switch", true)

        firstCityPreference?.setOnPreferenceClickListener {
            timezoneBeingEdited = 1
            timeZonePickerLauncher.launch(timeZonePickerIntent)
            true
        }

        secondCityPreference?.setOnPreferenceClickListener {
            timezoneBeingEdited = 2
            timeZonePickerLauncher.launch(timeZonePickerIntent)
            true
        }

        dayNightPreference?.setOnPreferenceChangeListener { preference, newValue ->
            prefs.edit { putBoolean("${widgetId}_day_night_switch", newValue as Boolean) }
            DualClockAppWidget.updateWidget(requireContext(), widgetId)
            true
        }
    }

    private fun loadOrDefaultTimeZone(key: String): CityTimeZoneInfo {
        val savedId = prefs.getInt(key, -1)
        return CITY_TIME_ZONES.find { it.id == savedId } ?: CITY_TIME_ZONES.first().also {
            prefs.edit { putInt(key, it.id) }
        }
    }
}