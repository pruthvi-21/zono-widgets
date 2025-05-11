package com.jw.zonowidgets.utils

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
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

fun CityTimeZoneInfo.getCityName(context: Context) = context.getString(cityRes)

fun CityTimeZoneInfo.getCountryName(context: Context) = context.getString(countryRes)

@Composable
fun buildColoredString(text: String, textToHighlight: String): AnnotatedString {
    val spanStyle = SpanStyle(
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
    val annotatedString = remember(text, textToHighlight) {
        buildAnnotatedString {
            val startIndex = text.indexOf(textToHighlight, ignoreCase = true)
            if (textToHighlight.isBlank() || startIndex == -1) {
                append(text)
            } else {
                append(text.substring(0, startIndex))
                withStyle(spanStyle) {
                    append(text.substring(startIndex, startIndex + textToHighlight.length))
                }
                append(text.substring(startIndex + textToHighlight.length))
            }
        }
    }

    return annotatedString
}

fun getFlagEmoji(countryCode: String): String {
    if (countryCode.length != 2) return "\uD83C\uDF0D"

    val firstLetter = Character.codePointAt(countryCode, 0) - 0x41 + 0x1F1E6
    val secondLetter = Character.codePointAt(countryCode, 1) - 0x41 + 0x1F1E6

    return String(Character.toChars(firstLetter)) + String(Character.toChars(secondLetter))
}