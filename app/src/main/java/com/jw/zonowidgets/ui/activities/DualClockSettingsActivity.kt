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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.jw.zonowidgets.R
import com.jw.zonowidgets.ui.components.PreferenceSummaryText
import com.jw.zonowidgets.ui.components.PreferenceTitleText
import com.jw.zonowidgets.ui.components.SliderSetting
import com.jw.zonowidgets.ui.components.SubHeading
import com.jw.zonowidgets.ui.components.SwitchSetting
import com.jw.zonowidgets.ui.components.TileSetting
import com.jw.zonowidgets.ui.theme.ZonoWidgetsTheme
import com.jw.zonowidgets.ui.theme.defaultShape
import com.jw.zonowidgets.ui.theme.preferenceSummaryStyle
import com.jw.zonowidgets.ui.viewmodel.DualClockSettingsViewModel
import com.jw.zonowidgets.ui.viewmodel.DualClockSettingsViewModel.Companion.FIRST_CLOCK
import com.jw.zonowidgets.ui.viewmodel.DualClockSettingsViewModel.Companion.SECOND_CLOCK
import com.jw.zonowidgets.ui.viewmodel.DualClockSettingsViewModelFactory
import com.jw.zonowidgets.utils.EXTRA_SELECTED_ZONE_ID
import com.jw.zonowidgets.utils.WidgetPrefs

class DualClockSettingsActivity : ComponentActivity() {

    private val prefs by lazy { WidgetPrefs(this) }

    private var widgetId: Int = INVALID_APPWIDGET_ID

    private val viewModel by viewModels<DualClockSettingsViewModel> {
        DualClockSettingsViewModelFactory(widgetId, prefs)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        widgetId = intent.getIntExtra(EXTRA_APPWIDGET_ID, INVALID_APPWIDGET_ID)

        if (widgetId == INVALID_APPWIDGET_ID) {
            Toast.makeText(this, R.string.unable_to_open_settings, Toast.LENGTH_SHORT).show()
            finish()
        }

        showExactAlarmPermissionDialog(this)

        setContent {
            ZonoWidgetsTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets.safeDrawing,
                    topBar = {
                        TopBar(
                            onNavigationIconClick = {
                                setResult(RESULT_CANCELED)
                                finish()
                            }
                        )
                    },
                    bottomBar = {
                        BottomBar(
                            onCancel = {
                                setResult(RESULT_CANCELED)
                                finish()
                            },
                            onSave = {
                                viewModel.saveSettings()
                                viewModel.refreshWidget(
                                    this@DualClockSettingsActivity,
                                    widgetId
                                )
                                setResult(
                                    RESULT_OK,
                                    Intent().putExtra(EXTRA_APPWIDGET_ID, widgetId)
                                )
                                finish()
                            }
                        )
                    }
                ) { innerPadding ->
                    MyContent(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }

    @Composable
    private fun MyContent(modifier: Modifier) {
        val context = LocalContext.current

        val launcher =
            rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode != RESULT_OK) return@rememberLauncherForActivityResult
                val id = result.data?.getStringExtra(EXTRA_SELECTED_ZONE_ID)
                    ?: return@rememberLauncherForActivityResult

                viewModel.onCitySelected(id)
                viewModel.refreshWidget(context, widgetId)
            }

        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .clip(defaultShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                TileSetting(
                    title = { PreferenceTitleText(stringResource(R.string.first_city)) },
                    summary = {
                        PreferenceSummaryText(
                            text = viewModel.getFirstCityName(context),
                            color = MaterialTheme.colorScheme.primary,
                        )
                    },
                    onClick = {
                        viewModel.setEditingClockIndex(FIRST_CLOCK)
                        launcher.launch(Intent(context, TimeZonePickerActivity::class.java))
                    }
                )
                HorizontalDivider(Modifier.padding(horizontal = 20.dp))
                TileSetting(
                    title = { PreferenceTitleText(stringResource(R.string.second_city)) },
                    summary = {
                        PreferenceSummaryText(
                            text = viewModel.getSecondCityName(context),
                            style = MaterialTheme.typography.preferenceSummaryStyle,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    },
                    onClick = {
                        viewModel.setEditingClockIndex(SECOND_CLOCK)
                        launcher.launch(Intent(context, TimeZonePickerActivity::class.java))
                    }
                )
            }

            SubHeading(
                label = stringResource(R.string.additional_configuration),
                icon = painterResource(R.drawable.ic_settings_24dp),
                modifier = Modifier
                    .padding(top = 15.dp)
                    .padding(horizontal = 12.dp),
            )

            Column(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .clip(defaultShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                SwitchSetting(
                    title = stringResource(R.string.day_night_switch_title),
                    summary = stringResource(R.string.day_night_switch_description),
                    checked = viewModel.settings.isDayNightModeEnabled,
                    onClick = { viewModel.toggleDayNight() },
                )
                HorizontalDivider(Modifier.padding(horizontal = 20.dp))
                SliderSetting(
                    title = stringResource(R.string.background_opacity_title),
                    value = viewModel.settings.backgroundOpacity,
                    onValueChange = { viewModel.updateOpacity(it) },
                )
            }
        }
    }

    private fun showExactAlarmPermissionDialog(context: Context) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (!alarmManager.canScheduleExactAlarms() && prefs.shouldShowExactAlarmDialog()) {
            AlertDialog.Builder(context)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    onNavigationIconClick: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.clock_settings),
                color = MaterialTheme.colorScheme.primary
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigationIconClick) {
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_left),
                    contentDescription = stringResource(R.string.navigate_up),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        modifier = Modifier.padding(top = dimensionResource(R.dimen.toolbar_top_margin)),
        windowInsets = WindowInsets.safeDrawing,
    )
}

@Composable
private fun BottomBar(
    onCancel: () -> Unit,
    onSave: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextButton(
            modifier = Modifier
                .weight(1f),
            contentPadding = PaddingValues(15.dp),
            shape = defaultShape,
            onClick = onCancel
        ) {
            Text(text = stringResource(R.string.cancel), fontSize = 18.sp)
        }

        TextButton(
            modifier = Modifier
                .weight(1f),
            contentPadding = PaddingValues(15.dp),
            shape = defaultShape,
            onClick = onSave
        ) {
            Text(text = stringResource(R.string.save), fontSize = 18.sp)
        }
    }
}
