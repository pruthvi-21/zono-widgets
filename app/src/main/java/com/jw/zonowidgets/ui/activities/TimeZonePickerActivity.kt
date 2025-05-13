package com.jw.zonowidgets.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.jw.zonowidgets.ui.activities.screens.TimeZonePickerScreen
import com.jw.zonowidgets.ui.theme.ZonoWidgetsTheme

class TimeZonePickerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ZonoWidgetsTheme {
                TimeZonePickerScreen()
            }
        }
    }
}
