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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import com.jw.zonowidgets.R
import com.jw.zonowidgets.data.model.CityTimeZoneInfo
import com.jw.zonowidgets.ui.theme.ZonoWidgetsTheme
import com.jw.zonowidgets.ui.theme.defaultShape
import com.jw.zonowidgets.ui.widget.DualClockAppWidget
import com.jw.zonowidgets.utils.CITY_TIME_ZONES
import com.jw.zonowidgets.utils.EXTRA_SELECTED_ZONE_ID
import com.jw.zonowidgets.utils.PREFERENCES_NAME

class ClockSettingsActivity : ComponentActivity() {

    private val prefs by lazy { getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE) }

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
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = stringResource(R.string.clock_settings),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            },
                            modifier = Modifier.padding(top = dimensionResource(R.dimen.toolbar_top_margin))
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

        var selectedTz1 by remember { mutableStateOf(loadOrDefaultTimeZone("${widgetId}_item1")) }
        var selectedTz2 by remember { mutableStateOf(loadOrDefaultTimeZone("${widgetId}_item2")) }

        var timezoneBeingEdited by remember { mutableStateOf(1) }

        val launcher =
            rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode != RESULT_OK || result.data == null) return@rememberLauncherForActivityResult

                val id = result.data!!.getIntExtra(EXTRA_SELECTED_ZONE_ID, -1)
                val selected =
                    CITY_TIME_ZONES.find { it.id == id } ?: return@rememberLauncherForActivityResult

                if (timezoneBeingEdited == 1) {
                    prefs.edit { putInt("${widgetId}_item1", selected.id) }
                    selectedTz1 = selected
                } else {
                    prefs.edit { putInt("${widgetId}_item2", selected.id) }
                    selectedTz2 = selected
                }

                DualClockAppWidget.updateWidget(context, widgetId)
            }

        Column(modifier = modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .clip(defaultShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                TimeZoneSettingTile(
                    title = stringResource(R.string.first_city),
                    timeZoneInfo = selectedTz1,
                    onClick = {
                        timezoneBeingEdited = 1
                        launcher.launch(Intent(context, TimeZonePickerActivity::class.java))
                    }
                )
                HorizontalDivider(Modifier.padding(horizontal = 20.dp))
                TimeZoneSettingTile(
                    title = stringResource(R.string.second_city),
                    timeZoneInfo = selectedTz2,
                    onClick = {
                        timezoneBeingEdited = 2
                        launcher.launch(Intent(context, TimeZonePickerActivity::class.java))
                    }
                )
            }

            Text(
                text = stringResource(R.string.background),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(horizontal = 32.dp, vertical = 5.dp)
                    .padding(top = 15.dp),
            )
            DayNightSwitch()

            Spacer(Modifier.weight(1f))

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                contentPadding = PaddingValues(15.dp),
                shape = defaultShape,
                onClick = {
                    DualClockAppWidget.updateWidget(this@ClockSettingsActivity, widgetId)
                    setResult(RESULT_OK, Intent().putExtra(EXTRA_APPWIDGET_ID, widgetId))
                    finish()
                }
            ) {
                Text(text = stringResource(R.string.done))
            }
        }
    }

    @Composable
    private fun TimeZoneSettingTile(
        title: String,
        timeZoneInfo: CityTimeZoneInfo,
        onClick: () -> Unit,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .clickable { onClick() }
                .padding(vertical = 14.dp, horizontal = 20.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 16.sp,
            )
            Text(
                text = timeZoneInfo.city,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }

    @Composable
    private fun DayNightSwitch() {
        var isChecked by remember {
            mutableStateOf(prefs.getBoolean("${widgetId}_day_night_switch", true))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .padding(horizontal = 12.dp)
                .clip(defaultShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable {
                    isChecked = !isChecked
                    prefs.edit {
                        putBoolean("${widgetId}_day_night_switch", isChecked)
                    }
                    DualClockAppWidget.updateWidget(this@ClockSettingsActivity, widgetId)
                }
                .padding(vertical = 14.dp, horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = stringResource(R.string.day_night_switch_title),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = stringResource(R.string.day_night_switch_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(10.dp))

            Switch(
                checked = isChecked,
                onCheckedChange = null,
            )
        }
    }

    private fun loadOrDefaultTimeZone(key: String): CityTimeZoneInfo {
        val savedId = prefs.getInt(key, -1)
        return CITY_TIME_ZONES.find { it.id == savedId } ?: CITY_TIME_ZONES.first().also {
            prefs.edit { putInt(key, it.id) }
        }
    }
}
