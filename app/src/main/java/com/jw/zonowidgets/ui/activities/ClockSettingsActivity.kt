package com.jw.zonowidgets.ui.activities

import android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID
import android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.jw.zonowidgets.R
import com.jw.zonowidgets.data.model.CityTimeZoneInfo
import com.jw.zonowidgets.ui.views.ClockSettingItemView
import com.jw.zonowidgets.ui.widget.DualClockAppWidget
import com.jw.zonowidgets.utils.CITY_TIME_ZONES
import com.jw.zonowidgets.utils.EXTRA_SELECTED_ZONE_ID
import com.jw.zonowidgets.utils.PREFERENCES_NAME

class ClockSettingsActivity : AppCompatActivity() {

    private lateinit var timeZoneViewFirst: ClockSettingItemView
    private lateinit var timeZoneViewSecond: ClockSettingItemView
    private lateinit var saveButton: Button

    private lateinit var selectedTz1: CityTimeZoneInfo
    private lateinit var selectedTz2: CityTimeZoneInfo

    private val prefs by lazy { getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE) }

    private var widgetId: Int = INVALID_APPWIDGET_ID

    private var timezoneBeingEdited: Int = 1
    private val timeZonePickerIntent by lazy { Intent(this, TimeZonePickerActivity::class.java) }
    private val timeZonePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != RESULT_OK || result.data == null) return@registerForActivityResult

            val id = result.data!!.getIntExtra(EXTRA_SELECTED_ZONE_ID, -1)
            val selected = CITY_TIME_ZONES.find { it.id == id } ?: return@registerForActivityResult

            if (timezoneBeingEdited == 1) {
                selectedTz1 = selected
                timeZoneViewFirst.setCityLabel(selected.city)
            } else {
                selectedTz2 = selected
                timeZoneViewSecond.setCityLabel(selected.city)
            }

            DualClockAppWidget.updateWidget(this, widgetId)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_clock_widgets_settings)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        widgetId = intent.getIntExtra(EXTRA_APPWIDGET_ID, INVALID_APPWIDGET_ID)
        if (widgetId == INVALID_APPWIDGET_ID) {
            Toast.makeText(this, R.string.unable_to_open_settings, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        timeZoneViewFirst = findViewById(R.id.timezone1)
        timeZoneViewSecond = findViewById(R.id.timezone2)
        saveButton = findViewById(R.id.save_button)

        selectedTz1 = loadOrDefaultTimeZone("${widgetId}_item1")
        selectedTz2 = loadOrDefaultTimeZone("${widgetId}_item2")

        timeZoneViewFirst.apply {
            setCityLabel(selectedTz1.city)
            setOnClickListener {
                timezoneBeingEdited = 1
                timeZonePickerLauncher.launch(timeZonePickerIntent)
            }
        }
        timeZoneViewSecond.apply {
            setCityLabel(selectedTz2.city)
            setOnClickListener {
                timezoneBeingEdited = 2
                timeZonePickerLauncher.launch(timeZonePickerIntent)
            }
        }

        saveButton.setOnClickListener {
            saveTimezones()
            DualClockAppWidget.updateWidget(this, widgetId)
            setResult(RESULT_OK, Intent().putExtra(EXTRA_APPWIDGET_ID, widgetId))
            finish()
        }
    }

    private fun loadOrDefaultTimeZone(key: String): CityTimeZoneInfo {
        val savedId = prefs.getInt(key, -1)
        return CITY_TIME_ZONES.find { it.id == savedId } ?: CITY_TIME_ZONES.first().also {
            prefs.edit { putInt(key, it.id) }
        }
    }

    private fun saveTimezones() {
        prefs.edit {
            putInt("${widgetId}_item1", selectedTz1.id)
            putInt("${widgetId}_item2", selectedTz2.id)
        }
    }
}