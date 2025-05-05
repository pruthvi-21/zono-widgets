package com.jw.zonowidgets.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CornerSize
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jw.zonowidgets.R
import com.jw.zonowidgets.ui.components.SubHeading
import com.jw.zonowidgets.ui.components.TileSetting
import com.jw.zonowidgets.ui.theme.ZonoWidgetsTheme
import com.jw.zonowidgets.ui.theme.defaultShape
import com.jw.zonowidgets.utils.EXTRA_SELECTED_ZONE_ID
import com.jw.zonowidgets.utils.World
import com.jw.zonowidgets.utils.readableOffset

class TimeZonePickerActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ZonoWidgetsTheme {
                Scaffold(
                    contentWindowInsets = WindowInsets.safeDrawing,
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
        val grouped = remember {
            World.cities
                .sortedBy { it.city }
                .groupBy { it.city.first() }
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
                        title = zone.city,
                        summary = stringResource(R.string.gmt, offset),
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
