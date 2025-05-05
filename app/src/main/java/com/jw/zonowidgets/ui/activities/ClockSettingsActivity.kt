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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jw.zonowidgets.R
import com.jw.zonowidgets.data.model.CityTimeZoneInfo
import com.jw.zonowidgets.ui.theme.ZonoWidgetsTheme
import com.jw.zonowidgets.ui.theme.defaultShape
import com.jw.zonowidgets.ui.widget.DualClockAppWidget
import com.jw.zonowidgets.utils.EXTRA_SELECTED_ZONE_ID
import com.jw.zonowidgets.utils.WidgetPrefs
import com.jw.zonowidgets.utils.World
import kotlin.math.roundToInt

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

        var firstTimeZoneInfo by remember { mutableStateOf(loadOrDefaultTimeZone(widgetId, 1)) }
        var secondTimeZoneInfo by remember { mutableStateOf(loadOrDefaultTimeZone(widgetId, 2)) }

        var isDayNightModeEnabled by remember { mutableStateOf(prefs.getDayNightSwitch(widgetId)) }
        var is24HourFormatEnabled by remember { mutableStateOf(prefs.getUse24HourFormat(widgetId)) }

        var backgroundOpacityValue by remember {
            mutableFloatStateOf(prefs.getBackgroundOpacity(widgetId))
        }

        var timezoneBeingEdited by remember { mutableStateOf(1) }

        val launcher =
            rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode != RESULT_OK || result.data == null) return@rememberLauncherForActivityResult

                val id = result.data!!.getStringExtra(EXTRA_SELECTED_ZONE_ID)
                    ?: return@rememberLauncherForActivityResult
                val selected =
                    World.cities.find { it.id == id }
                        ?: return@rememberLauncherForActivityResult

                if (timezoneBeingEdited == 1) {
                    firstTimeZoneInfo = selected
                } else {
                    secondTimeZoneInfo = selected
                }

                DualClockAppWidget.updateWidget(context, widgetId)
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
                    TimeZoneSettingTile(
                        title = stringResource(R.string.first_city),
                        timeZoneInfo = firstTimeZoneInfo,
                        onClick = {
                            timezoneBeingEdited = 1
                            launcher.launch(Intent(context, TimeZonePickerActivity::class.java))
                        }
                    )
                    HorizontalDivider(Modifier.padding(horizontal = 20.dp))
                    TimeZoneSettingTile(
                        title = stringResource(R.string.second_city),
                        timeZoneInfo = secondTimeZoneInfo,
                        onClick = {
                            timezoneBeingEdited = 2
                            launcher.launch(Intent(context, TimeZonePickerActivity::class.java))
                        }
                    )
                }

                SubHeading(
                    label = stringResource(R.string.additional_configuration),
                    icon = painterResource(R.drawable.ic_settings_24dp),
                    modifier = Modifier.padding(top = 15.dp),
                )

                Column(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .clip(defaultShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    SwitchSetting(
                        title = stringResource(R.string.format_24_hour_switch_title),
                        checked = is24HourFormatEnabled,
                        onClick = { is24HourFormatEnabled = it },
                    )
                    HorizontalDivider(Modifier.padding(horizontal = 20.dp))
                    SwitchSetting(
                        title = stringResource(R.string.day_night_switch_title),
                        summary = stringResource(R.string.day_night_switch_description),
                        checked = isDayNightModeEnabled,
                        onClick = { isDayNightModeEnabled = it },
                    )
                    HorizontalDivider(Modifier.padding(horizontal = 20.dp))
                    BackgroundOpacitySlider(
                        value = backgroundOpacityValue,
                        onValueChange = { backgroundOpacityValue = it },
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
                    prefs.setCityIdAt(widgetId, 1, firstTimeZoneInfo.id)
                    prefs.setCityIdAt(widgetId, 2, secondTimeZoneInfo.id)
                    prefs.setUse24HourFormat(widgetId, is24HourFormatEnabled)
                    prefs.setDayNightSwitch(widgetId, isDayNightModeEnabled)
                    prefs.setBackgroundOpacity(widgetId, backgroundOpacityValue)

                    DualClockAppWidget.updateWidget(this@ClockSettingsActivity, widgetId)
                    setResult(RESULT_OK, Intent().putExtra(EXTRA_APPWIDGET_ID, widgetId))
                    finish()
                }
            ) {
                Text(text = stringResource(R.string.save))
            }
        }
    }

    @Composable
    private fun SubHeading(
        modifier: Modifier = Modifier,
        label: String,
        icon: Painter? = null,
    ) {
        Row(
            modifier = modifier
                .padding(horizontal = 32.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            icon?.let {
                Icon(
                    painter = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
            )
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
    private fun SwitchSetting(
        modifier: Modifier = Modifier,
        title: String,
        summary: String? = null,
        checked: Boolean,
        onClick: ((Boolean) -> Unit)? = null,
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable {
                    onClick?.let { it(!checked) }
                }
                .padding(vertical = 14.dp, horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )
                summary?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.width(10.dp))

            Switch(
                checked = checked,
                onCheckedChange = null,
            )
        }
    }

    @Composable
    fun BackgroundOpacitySlider(
        value: Float,
        onValueChange: (Float) -> Unit,
        modifier: Modifier = Modifier,
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(vertical = 14.dp, horizontal = 20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.background_opacity_title),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "${(value * 100).roundToInt()}%",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }
            Slider(
                value = value,
                onValueChange = onValueChange,
                steps = 9,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    private fun loadOrDefaultTimeZone(widgetId: Int, position: Int): CityTimeZoneInfo {
        val savedId = prefs.getCityIdAt(widgetId, position)
        return World.cities.find { it.id == savedId } ?: WidgetPrefs.DEFAULT_CITY.also {
            prefs.setCityIdAt(widgetId, position, it.id)
        }
    }
}
