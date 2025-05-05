package com.jw.zonowidgets.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jw.zonowidgets.ui.theme.preferenceCategoryStyle
import com.jw.zonowidgets.ui.theme.preferenceSummaryColor

@Composable
fun SubHeading(
    modifier: Modifier = Modifier,
    label: String,
    icon: Painter? = null,
) {
    Row(
        modifier = modifier
            .padding(horizontal = 20.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        icon?.let {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.preferenceSummaryColor,
                modifier = Modifier.size(14.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.preferenceCategoryStyle,
            color = MaterialTheme.colorScheme.preferenceSummaryColor,
            fontWeight = FontWeight.Bold,
        )
    }
}
