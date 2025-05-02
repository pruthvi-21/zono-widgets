package com.jw.zonowidgets.ui.activities

import android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID
import android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.jw.zonowidgets.R
import com.jw.zonowidgets.ui.fragments.ClockSettingsFragment
import com.jw.zonowidgets.ui.widget.DualClockAppWidget

class ClockSettingsActivity : AppCompatActivity() {

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

        val widgetId = intent.getIntExtra(EXTRA_APPWIDGET_ID, INVALID_APPWIDGET_ID)

        val fragmentArguments = Bundle().apply { putInt(EXTRA_APPWIDGET_ID, widgetId) }
        val clockSettingsFragment = ClockSettingsFragment().apply { arguments = fragmentArguments }

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, clockSettingsFragment)
            .commit()

        val saveButton: Button = findViewById(R.id.save_button)
        saveButton.setOnClickListener {
            DualClockAppWidget.updateWidget(this, widgetId)
            setResult(RESULT_OK, Intent().putExtra(EXTRA_APPWIDGET_ID, widgetId))
            finish()
        }
    }
}