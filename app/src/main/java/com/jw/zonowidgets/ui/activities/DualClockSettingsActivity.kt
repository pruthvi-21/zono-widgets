package com.jw.zonowidgets.ui.activities

import android.app.AlarmManager
import android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID
import android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import com.jw.zonowidgets.R
import com.jw.zonowidgets.ui.activities.screens.DualClockSettingsScreen
import com.jw.zonowidgets.ui.theme.ZonoWidgetsTheme
import com.jw.zonowidgets.ui.viewmodel.DualClockSettingsViewModel
import com.jw.zonowidgets.ui.viewmodel.DualClockSettingsViewModelFactory
import com.jw.zonowidgets.utils.WidgetPrefs

class DualClockSettingsActivity : ComponentActivity() {

    private val prefs by lazy { WidgetPrefs(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val widgetId = intent.getIntExtra(EXTRA_APPWIDGET_ID, INVALID_APPWIDGET_ID)
        if (widgetId == INVALID_APPWIDGET_ID) {
            Toast.makeText(this, R.string.unable_to_open_settings, Toast.LENGTH_SHORT).show()
            finish()
        }

        showExactAlarmPermissionDialog()

        val viewModel by viewModels<DualClockSettingsViewModel> {
            DualClockSettingsViewModelFactory(widgetId, prefs)
        }

        setContent {
            ZonoWidgetsTheme {
                DualClockSettingsScreen(
                    widgetId = widgetId,
                    viewModel = viewModel,
                )
            }
        }
    }

    private fun showExactAlarmPermissionDialog() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (!alarmManager.canScheduleExactAlarms() && prefs.shouldShowExactAlarmDialog()) {
            AlertDialog.Builder(this)
                .setTitle(R.string.enable_precise_alarms)
                .setMessage(R.string.enable_precise_alarms_description)
                .setPositiveButton(R.string.allow) { _, _ ->
                    openExactAlarmSettings()
                }
                .setNegativeButton(R.string.cancel) { _, _ ->
                    prefs.setShowExactAlarmDialog(false)
                }
                .show()
        }
    }

    private fun openExactAlarmSettings() {
        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
            data = "package:${packageName}".toUri()
        }
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Log.w(TAG, "Unable to open exact alarm permission settings.")
        }
    }

    companion object {
        private const val TAG = "DualClockSettingsActivity"
    }
}
