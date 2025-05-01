package com.jw.zonowidgets.ui.views

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.LinearLayout
import android.widget.TextView
import com.jw.zonowidgets.R
import com.jw.zonowidgets.data.model.CityTimeZoneInfo
import java.time.ZoneId
import java.time.ZonedDateTime

class TimeZoneListItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : LinearLayout(context, attrs) {
    private val label: TextView
    private val offset: TextView

    init {
        inflate(context, R.layout.view_time_zone_list_item, this)

        label = findViewById(R.id.label)
        offset = findViewById(R.id.offset)

        val typedValue = TypedValue()
        context.theme.resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true)
        setBackgroundResource(typedValue.resourceId)
    }

    fun bindTimeZone(info: CityTimeZoneInfo) {
        label.text = info.city
        offset.text = context.getString(
            R.string.gmt,
            ZonedDateTime.now(ZoneId.of(info.timeZoneId)).offset.toString()
        )
    }
}