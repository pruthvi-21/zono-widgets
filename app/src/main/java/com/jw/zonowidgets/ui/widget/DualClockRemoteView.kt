package com.jw.zonowidgets.ui.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.widget.RemoteViews
import com.jw.zonowidgets.R
import com.jw.zonowidgets.data.CityRepository
import com.jw.zonowidgets.utils.WidgetPrefs

/**
 * Represents the RemoteViews for a dual-clock widget instance.
 * Constructs the widget layout dynamically.
 */
class DualClockRemoteView(
    private val context: Context,
    private val widgetId: Int,
) : RemoteViews(context.packageName, R.layout.widget_dual_clock) {

    init {
        removeAllViews(R.id.root)

        val prefs = WidgetPrefs(context)

        val ids = prefs.getCityIds(widgetId)
        val settings = prefs.getSettings(widgetId)

        val layoutId = getLayoutId(context, widgetId)

        ids.forEach {
            val city = CityRepository.getCityById(it)

            if (city != null) {
                addView(
                    R.id.root,
                    DualClockItemRemoteView(
                        context = context,
                        widgetId = widgetId,
                        layoutId = layoutId,
                        isCompact = isCompactLayout(context, widgetId),
                        cityTimeZone = city,
                        settings = settings
                    )
                )
            } else {
                val errorMessage = context.getString(R.string.error_no_timezone)
                val errorView = RemoteViews(context.packageName, R.layout.widget_dual_clock_error)
                errorView.setTextViewText(R.id.error_message, errorMessage)
                addView(R.id.root, errorView)
            }
        }
    }

    /**
     * Returns the layout ID to use based on widget's current size.
     * Uses a more compact layout for smaller widgets.
     */
    private fun getLayoutId(context: Context, widgetId: Int): Int {
        return if (isCompactLayout(context, widgetId)) {
            R.layout.widget_dual_clock_item_compact
        } else {
            R.layout.widget_dual_clock_item
        }
    }

    private fun isCompactLayout(context: Context, widgetId: Int): Boolean {
        val options = AppWidgetManager.getInstance(context)
            .getAppWidgetOptions(widgetId)
        val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
        return minHeight < 90
    }
}