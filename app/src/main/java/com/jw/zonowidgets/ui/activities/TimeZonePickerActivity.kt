package com.jw.zonowidgets.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jw.zonowidgets.R
import com.jw.zonowidgets.data.model.CityTimeZoneInfo
import com.jw.zonowidgets.ui.theme.ZonoWidgetsTheme
import com.jw.zonowidgets.utils.CITY_TIME_ZONES
import com.jw.zonowidgets.utils.EXTRA_SELECTED_ZONE_ID
import java.time.ZoneId
import java.time.ZonedDateTime

class TimeZonePickerActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ZonoWidgetsTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = stringResource(R.string.select_your_city),
                                    color = MaterialTheme.colorScheme.primary
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
        val grouped = remember {
            CITY_TIME_ZONES
                .sortedBy { it.city }
                .groupBy { it.city.first() }
        }

        LazyColumn(modifier = modifier) {
            grouped.entries.forEachIndexed { index, (initial, zones) ->
                item {
                    if (index != 0) {
                        HorizontalDivider()
                    }
                    Text(
                        text = initial.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(horizontal = 20.dp, vertical = 4.dp)
                            .padding(top = 3.dp)
                    )
                }
                items(zones) { zone ->
                    ListItem(zone)
                }
            }
        }
    }

    @Composable
    private fun ListItem(timeZoneInfo: CityTimeZoneInfo) {
        val offset = getString(
            R.string.gmt,
            ZonedDateTime.now(ZoneId.of(timeZoneInfo.timeZoneId)).offset.toString()
        )

        Column(
            modifier = Modifier
                .heightIn(min = 64.dp)
                .fillMaxWidth()
                .clickable {
                    setResult(RESULT_OK, Intent().putExtra(EXTRA_SELECTED_ZONE_ID, timeZoneInfo.id))
                    finish()
                }
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Text(
                text = timeZoneInfo.city
            )
            Text(
                text = offset,
                color = Color(0xFF999999)
            )
        }
    }

}

