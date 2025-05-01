package com.jw.zonowidgets.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jw.zonowidgets.data.model.CityTimeZoneInfo
import com.jw.zonowidgets.ui.views.TimeZoneListItemView

class TimeZoneListAdapter(
    private val timeZones: List<CityTimeZoneInfo>,
    private val onClick: (CityTimeZoneInfo) -> Unit,
) : RecyclerView.Adapter<TimeZoneListAdapter.TimeZoneViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeZoneViewHolder {
        val view = TimeZoneListItemView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        return TimeZoneViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimeZoneViewHolder, position: Int) {
        val timeZone = timeZones[position]
        holder.listItem.bindTimeZone(timeZone)
        holder.listItem.setOnClickListener { onClick(timeZone) }
    }

    override fun getItemCount(): Int = timeZones.size

    inner class TimeZoneViewHolder(
        val listItem: TimeZoneListItemView,
    ) : RecyclerView.ViewHolder(listItem)
}