package com.jw.zonowidgets.ui.preferences

import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.util.TypedValue
import androidx.core.content.ContextCompat
import androidx.preference.Preference
import com.jw.zonowidgets.data.model.CityTimeZoneInfo
import com.jw.zonowidgets.utils.CITY_TIME_ZONES

class TimeZonePreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : Preference(context, attrs) {

    private var selectedTz: CityTimeZoneInfo = CITY_TIME_ZONES.first()

    fun setCityTimeZone(tz: CityTimeZoneInfo) {
        selectedTz = tz
        notifyChanged()
    }

    override fun getSummary(): CharSequence {
        val text = selectedTz.city
        val spannable = SpannableString(text)

        val typedValue = TypedValue()
        val theme = context.theme
        if (theme.resolveAttribute(android.R.attr.colorPrimary, typedValue, true)) {
            val color = if (typedValue.resourceId != 0) {
                ContextCompat.getColor(context, typedValue.resourceId)
            } else {
                typedValue.data
            }

            spannable.setSpan(
                ForegroundColorSpan(color),
                0,
                text.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        return spannable
    }
}