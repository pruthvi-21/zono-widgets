package com.jw.zonowidgets.utils

import com.jw.zonowidgets.data.model.CityTimeZoneInfo
import java.time.ZoneId
import java.time.ZonedDateTime

fun CityTimeZoneInfo.readableOffset(): String {
    val offset = ZonedDateTime.now(ZoneId.of(timeZoneId)).offset.toString()

    val formattedOffset =
        if (offset.uppercase() == "Z") "+0"
        else {
            Regex("([+-])(\\d{2}):(\\d{2})").replace(offset) { matchResult ->
                val sign = matchResult.groupValues[1]
                val hour = matchResult.groupValues[2].toInt()
                val minutes = matchResult.groupValues[3]

                if (minutes == "00") "$sign$hour"
                else "$sign$hour:$minutes"
            }
        }
    return formattedOffset
}