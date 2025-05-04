package com.jw.zonowidgets.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

val defaultShape = RoundedCornerShape(8.dp)

@Composable
fun ZonoWidgetsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current

    val darkScheme = dynamicDarkColorScheme(context).copy(
        surface = Color.Black,
        background = Color.Black,
        surfaceVariant = Color(0xFF17171A),
        onSurfaceVariant = Color(0xFF999999),
        outlineVariant = Color(0xFF3A3A3D),
    )

    val lightScheme = dynamicLightColorScheme(context).copy(
        surface = Color(0xFFF6F6F6),
        background = Color(0xFFF6F6F6),
        surfaceVariant = Color(0xFFFCFCFF),
        onSurfaceVariant = Color(0xFF8C8C91),
        outlineVariant = Color(0xFFE6E6E6),
    )

    val colorScheme = if (darkTheme) darkScheme else lightScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

