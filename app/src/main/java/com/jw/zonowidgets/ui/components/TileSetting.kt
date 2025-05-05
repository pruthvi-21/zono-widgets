package com.jw.zonowidgets.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.jw.zonowidgets.ui.theme.preferenceSummaryColor
import com.jw.zonowidgets.ui.theme.preferenceSummaryStyle
import com.jw.zonowidgets.ui.theme.preferenceTitleStyle

@Composable
fun TileSetting(
    modifier: Modifier = Modifier,
    title: String,
    summary: String,
    summaryColor: Color = MaterialTheme.colorScheme.preferenceSummaryColor,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clickable { onClick() }
            .padding(vertical = 14.dp, horizontal = 20.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.preferenceTitleStyle,
        )
        Text(
            text = summary,
            style = MaterialTheme.typography.preferenceSummaryStyle,
            color = summaryColor,
        )
    }
}