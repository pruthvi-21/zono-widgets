package com.jw.zonowidgets.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jw.zonowidgets.utils.CITY_TIME_ZONES
import com.jw.zonowidgets.utils.EXTRA_SELECTED_ZONE_ID
import com.jw.zonowidgets.R
import com.jw.zonowidgets.ui.adapter.TimeZoneListAdapter
import me.zhanghai.android.fastscroll.FastScrollerBuilder

class TimeZonePickerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_time_zone_picker)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)

        recyclerView.adapter = TimeZoneListAdapter(CITY_TIME_ZONES.sortedBy { it.city }) {
            setResult(RESULT_OK, Intent().putExtra(EXTRA_SELECTED_ZONE_ID, it.id))
            finish()
        }

        val dividerItemDecoration =
            DividerItemDecoration(recyclerView.context, LinearLayoutManager.VERTICAL)
        recyclerView.addItemDecoration(dividerItemDecoration)

        FastScrollerBuilder(recyclerView).build()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }
}

