package com.jw.zonowidgets.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jw.zonowidgets.ui.theme.preferenceSummaryColor
import com.jw.zonowidgets.ui.theme.preferenceSummaryStyle
import com.jw.zonowidgets.ui.theme.preferenceTitleStyle

@Composable
fun SwitchSetting(
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
                style = MaterialTheme.typography.preferenceTitleStyle,
            )
            summary?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.preferenceSummaryStyle,
                    color = MaterialTheme.colorScheme.preferenceSummaryColor,
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