package com.jw.zonowidgets.ui.views

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.withStyledAttributes
import com.jw.zonowidgets.R

class ClockSettingItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : LinearLayout(context, attrs) {
    private val titleView: TextView
    private val cityView: TextView

    init {
        inflate(context, R.layout.view_clock_setting_item, this)

        titleView = findViewById(R.id.title)
        cityView = findViewById(R.id.summary)

        context.withStyledAttributes(attrs, R.styleable.ClockSettingItemView) {
            titleView.text = getString(R.styleable.ClockSettingItemView_android_text)
        }
    }

    fun setCityLabel(city: String) {
        cityView.text = city
    }
}