package com.jw.zonowidgets.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TileSetting(
    modifier: Modifier = Modifier,
    title: (@Composable () -> Unit),
    summary: (@Composable () -> Unit),
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clickable { onClick() }
            .padding(vertical = 14.dp, horizontal = 20.dp)
    ) {
        title()
        summary()
    }
}