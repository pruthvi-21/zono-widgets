package com.jw.zonowidgets.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jw.zonowidgets.R
import com.jw.zonowidgets.data.CityRepository
import com.jw.zonowidgets.ui.components.PreferenceSummaryText
import com.jw.zonowidgets.ui.components.PreferenceTitleText
import com.jw.zonowidgets.ui.components.SubHeading
import com.jw.zonowidgets.ui.components.TileSetting
import com.jw.zonowidgets.ui.theme.ZonoWidgetsTheme
import com.jw.zonowidgets.ui.theme.defaultShape
import com.jw.zonowidgets.utils.EXTRA_SELECTED_ZONE_ID
import com.jw.zonowidgets.utils.buildColoredString
import com.jw.zonowidgets.utils.getCityName
import com.jw.zonowidgets.utils.getCountryName
import com.jw.zonowidgets.utils.readableOffset

class TimeZonePickerActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            var query by rememberSaveable { mutableStateOf("") }
            val focusRequester = remember { FocusRequester() }

            ZonoWidgetsTheme {
                Scaffold(
                    contentWindowInsets = WindowInsets.safeDrawing,
                    topBar = {
                        TopAppBar(
                            title = {
                                LaunchedEffect(Unit) {
                                    focusRequester.requestFocus()
                                }
                                BasicTextField(
                                    value = query,
                                    onValueChange = { query = it },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .focusRequester(focusRequester)
                                        .background(Color.Transparent)
                                        .padding(horizontal = 5.dp),
                                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 20.sp
                                    ),
                                    decorationBox = { innerTextField ->
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 0.dp, vertical = 12.dp)
                                        ) {
                                            if (query.isEmpty()) {
                                                Text(
                                                    text = stringResource(R.string.search_cities),
                                                    color = TextFieldDefaults.colors().unfocusedPlaceholderColor,
                                                    fontSize = 20.sp
                                                )
                                            }
                                            innerTextField()
                                        }
                                    }
                                )
                            },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_chevron_left),
                                        contentDescription = stringResource(R.string.navigate_up),
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            },
                            modifier = Modifier.padding(top = dimensionResource(R.dimen.toolbar_top_margin)),
                        )
                    }
                ) { innerPadding ->
                    MyContent(
                        modifier = Modifier.padding(innerPadding),
                        query = query
                    )
                }
            }
        }
    }

    @Composable
    private fun MyContent(modifier: Modifier, query: String) {
        val grouped = remember(query) {
            CityRepository.getAllCities(filterQuery = query)
                .sortedBy { it.getCityName(this) }
                .groupBy { it.getCityName(this).first() }
        }
        val cardShape = defaultShape

        LazyColumn(modifier = modifier.padding(horizontal = 12.dp)) {

            grouped.entries.forEachIndexed { index, (initial, zones) ->
                item {
                    SubHeading(
                        label = initial.toString(),
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                    )
                }
                itemsIndexed(zones) { idx, zone ->
                    val offset = zone.readableOffset()
                    val shape = when {
                        zones.size == 1 -> cardShape
                        idx == 0 -> cardShape.copy(
                            bottomStart = CornerSize(0.dp),
                            bottomEnd = CornerSize(0.dp),
                        )

                        idx == zones.lastIndex -> cardShape.copy(
                            topStart = CornerSize(0.dp),
                            topEnd = CornerSize(0.dp),
                        )

                        else -> RectangleShape
                    }
                    TileSetting(
                        title = {
                            val city = zone.getCityName(this@TimeZonePickerActivity)
                            val country = zone.getCountryName(this@TimeZonePickerActivity)
                            val title = if(country.isNotEmpty()) "$city, $country" else city
                            PreferenceTitleText(buildColoredString(title, query))
                        },
                        summary = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(20.dp),
                            ) {
                                PreferenceSummaryText(
                                    text = buildColoredString(zone.timeZoneId, query),
                                    modifier = Modifier.weight(1f)
                                )
                                PreferenceSummaryText(stringResource(R.string.gmt, offset))
                            }
                        },
                        modifier = Modifier
                            .clip(shape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        onClick = {
                            setResult(RESULT_OK, Intent().putExtra(EXTRA_SELECTED_ZONE_ID, zone.id))
                            finish()
                        }
                    )
                    if (idx < zones.lastIndex) {
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(start = 20.dp, end = 20.dp)
                        )
                    }
                }
            }
            item {
                Spacer(Modifier.height(30.dp))
            }
        }
    }
}
