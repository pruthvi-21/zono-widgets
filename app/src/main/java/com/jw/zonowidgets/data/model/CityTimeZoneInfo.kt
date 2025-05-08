package com.jw.zonowidgets.data.model

import androidx.annotation.StringRes

data class CityTimeZoneInfo(
    val id: String,
    val isoCode: String,
    @StringRes val countryRes: Int,
    @StringRes val cityRes: Int,
    val timeZoneId: String,
)