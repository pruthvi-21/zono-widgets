package com.jw.zonowidgets.data.model

import androidx.annotation.StringRes

data class CityTimeZoneInfo(
    val id: String,
    @StringRes val cityRes: Int,
    val timeZoneId: String,
)