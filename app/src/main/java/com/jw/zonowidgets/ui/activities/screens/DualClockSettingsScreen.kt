package com.jw.zonowidgets.ui.activities.screens

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID
import android.content.Intent
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.jw.zonowidgets.R
import com.jw.zonowidgets.ui.activities.TimeZonePickerActivity
import com.jw.zonowidgets.ui.components.PreferenceSummaryText
import com.jw.zonowidgets.ui.components.PreferenceTitleText
import com.jw.zonowidgets.ui.components.SliderSetting
import com.jw.zonowidgets.ui.components.SubHeading
import com.jw.zonowidgets.ui.components.SwitchSetting
import com.jw.zonowidgets.ui.components.TileSetting
import com.jw.zonowidgets.ui.theme.defaultShape
import com.jw.zonowidgets.ui.theme.preferenceSummaryStyle
import com.jw.zonowidgets.ui.viewmodel.DualClockSettingsViewModel
import com.jw.zonowidgets.ui.viewmodel.DualClockSettingsViewModel.Companion.FIRST_CLOCK
import com.jw.zonowidgets.ui.viewmodel.DualClockSettingsViewModel.Companion.SECOND_CLOCK
import com.jw.zonowidgets.utils.EXTRA_SELECTED_ZONE_ID

@Composable
fun DualClockSettingsScreen(
    widgetId: Int,
    viewModel: DualClockSettingsViewModel,
) {
    val context = LocalContext.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = { TopBar() },
        bottomBar = {
            BottomBar(
                widgetId = widgetId,
                onSave = {
                    viewModel.saveSettings()
                    viewModel.refreshWidget(context, widgetId)
                }
            )
        }
    ) { innerPadding ->

        val launcher =
            rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode != RESULT_OK) return@rememberLauncherForActivityResult
                val id = result.data?.getStringExtra(EXTRA_SELECTED_ZONE_ID)
                    ?: return@rememberLauncherForActivityResult

                viewModel.onCitySelected(id)
                viewModel.refreshWidget(context, widgetId)
            }

        Column(
            modifier = Modifier
                .padding(innerPadding)
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar() {
    val activity = LocalActivity.current
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.clock_settings),
                color = MaterialTheme.colorScheme.primary
            )
        },
        navigationIcon = {
            IconButton(onClick = {
                activity?.setResult(RESULT_CANCELED)
                activity?.finish()
            }) {
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
    widgetId: Int,
    onSave: () -> Unit,
) {
    val activity = LocalActivity.current

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
            onClick = {
                activity?.setResult(RESULT_CANCELED)
                activity?.finish()
            }
        ) {
            Text(text = stringResource(R.string.cancel), fontSize = 18.sp)
        }

        TextButton(
            modifier = Modifier
                .weight(1f),
            contentPadding = PaddingValues(15.dp),
            shape = defaultShape,
            onClick = {
                onSave()
                activity?.setResult(
                    RESULT_OK,
                    Intent().putExtra(EXTRA_APPWIDGET_ID, widgetId)
                )
                activity?.finish()
            }
        ) {
            Text(text = stringResource(R.string.save), fontSize = 18.sp)
        }
    }
}
