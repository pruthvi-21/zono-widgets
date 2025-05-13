package com.jw.zonowidgets.ui.activities.screens

import android.app.Activity.RESULT_OK
import android.content.Intent
import androidx.activity.compose.LocalActivity
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
import androidx.compose.ui.platform.LocalContext
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
import com.jw.zonowidgets.ui.theme.defaultShape
import com.jw.zonowidgets.utils.EXTRA_SELECTED_ZONE_ID
import com.jw.zonowidgets.utils.buildColoredString
import com.jw.zonowidgets.utils.getCityName
import com.jw.zonowidgets.utils.getCountryName
import com.jw.zonowidgets.utils.readableOffset

@Composable
fun TimeZonePickerScreen() {
    var query by rememberSaveable { mutableStateOf("") }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            SearchBar(
                query = query,
                onQueryChange = { query = it }
            )
        }
    ) { innerPadding ->
        val context = LocalContext.current
        val activity = LocalActivity.current

        val grouped = remember(query) {
            if (query.isEmpty()) {
                CityRepository.getAllCities()
                    .sortedBy { it.getCityName(context) }
                    .groupBy { it.getCityName(context).first() }
            } else {
                val filtered = CityRepository.getFilteredCities(
                    context = context,
                    filterQuery = query
                )
                mapOf(context.getString(R.string.search_result) to filtered)
            }
        }
        val cardShape = defaultShape

        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 12.dp)
        ) {

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
                            val city = zone.getCityName(context)
                            val country = zone.getCountryName(context)
                            val title = if (country.isNotEmpty()) "$city, $country" else city
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
                            activity?.setResult(
                                RESULT_OK,
                                Intent().putExtra(EXTRA_SELECTED_ZONE_ID, zone.id)
                            )
                            activity?.finish()
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit = {},
) {
    val focusRequester = remember { FocusRequester() }

    TopAppBar(
        title = {
            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
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
            val activity = LocalActivity.current
            IconButton(onClick = { activity?.finish() }) {
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