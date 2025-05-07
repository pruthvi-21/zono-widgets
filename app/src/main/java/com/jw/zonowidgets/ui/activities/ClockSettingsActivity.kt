package com.jw.zonowidgets.ui.activities

import android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID
import android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jw.zonowidgets.R
import com.jw.zonowidgets.data.CityRepository
import com.jw.zonowidgets.ui.components.PreferenceSummaryText
import com.jw.zonowidgets.ui.components.PreferenceTitleText
import com.jw.zonowidgets.ui.components.SliderSetting
import com.jw.zonowidgets.ui.components.SubHeading
import com.jw.zonowidgets.ui.components.SwitchSetting
import com.jw.zonowidgets.ui.components.TileSetting
import com.jw.zonowidgets.ui.theme.ZonoWidgetsTheme
import com.jw.zonowidgets.ui.theme.defaultShape
import com.jw.zonowidgets.ui.theme.preferenceSummaryStyle
import com.jw.zonowidgets.ui.viewmodel.ClockSettingsViewModel
import com.jw.zonowidgets.ui.viewmodel.ClockSettingsViewModelFactory
import com.jw.zonowidgets.ui.widget.DualClockAppWidget
import com.jw.zonowidgets.utils.EXTRA_SELECTED_ZONE_ID
import com.jw.zonowidgets.utils.WidgetPrefs

class ClockSettingsActivity : ComponentActivity() {

    private val prefs by lazy { WidgetPrefs(this) }

    private var widgetId: Int = INVALID_APPWIDGET_ID

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        widgetId = intent.getIntExtra(EXTRA_APPWIDGET_ID, INVALID_APPWIDGET_ID)

        if (widgetId == INVALID_APPWIDGET_ID) {
            Toast.makeText(this, R.string.unable_to_open_settings, Toast.LENGTH_SHORT).show()
            finish()
        }

        setContent {
            ZonoWidgetsTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets.safeDrawing,
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = stringResource(R.string.clock_settings),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
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
                ) { innerPadding ->
                    MyContent(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }

    @Composable
    private fun MyContent(modifier: Modifier) {
        val context = LocalContext.current

        val viewModel by viewModels<ClockSettingsViewModel> {
            ClockSettingsViewModelFactory(widgetId, prefs)
        }

        val launcher =
            rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode != RESULT_OK || result.data == null) return@rememberLauncherForActivityResult

                val id = result.data!!.getStringExtra(EXTRA_SELECTED_ZONE_ID)
                    ?: return@rememberLauncherForActivityResult
                val selected = CityRepository.getCityById(id)
                    ?: return@rememberLauncherForActivityResult

                if (viewModel.timezoneBeingEdited == 1) {
                    viewModel.updateFirstTimeZone(selected)
                } else {
                    viewModel.updateSecondTimeZone(selected)
                }

                DualClockAppWidget.refreshWidget(context, widgetId)
            }

        Column(modifier = modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
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
                                text = stringResource(viewModel.firstTimeZoneInfo.cityRes),
                                color = MaterialTheme.colorScheme.primary,
                            )
                        },
                        onClick = {
                            viewModel.setTimezoneBeingEdited(1)
                            launcher.launch(Intent(context, TimeZonePickerActivity::class.java))
                        }
                    )
                    HorizontalDivider(Modifier.padding(horizontal = 20.dp))
                    TileSetting(
                        title = { PreferenceTitleText(stringResource(R.string.second_city)) },
                        summary = {
                            PreferenceSummaryText(
                                text = stringResource(viewModel.secondTimeZoneInfo.cityRes),
                                style = MaterialTheme.typography.preferenceSummaryStyle,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        },
                        onClick = {
                            viewModel.setTimezoneBeingEdited(2)
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
                        checked = viewModel.isDayNightModeEnabled,
                        onClick = { viewModel.toggleDayNight() },
                    )
                    HorizontalDivider(Modifier.padding(horizontal = 20.dp))
                    SliderSetting(
                        title = stringResource(R.string.background_opacity_title),
                        value = viewModel.opacityValue,
                        onValueChange = { viewModel.updateOpacity(it) },
                    )
                }
            }

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                contentPadding = PaddingValues(15.dp),
                shape = defaultShape,
                onClick = {
                    viewModel.saveSettings()

                    DualClockAppWidget.refreshWidget(this@ClockSettingsActivity, widgetId)
                    setResult(RESULT_OK, Intent().putExtra(EXTRA_APPWIDGET_ID, widgetId))
                    finish()
                }
            ) {
                Text(text = stringResource(R.string.save))
            }
        }
    }
}
