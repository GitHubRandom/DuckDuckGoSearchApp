package io.duckduckgosearch.app

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class HistoryAdapter internal constructor(private val context: Context, list: ArrayList<HistoryItem>) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {
    private var list: ArrayList<HistoryItem>
    private val historyDatabase: HistoryDatabase
    private var onLastTermDeleted: OnLastTermDeleted? = null

    internal interface OnLastTermDeleted {
        fun onLastTermDeleted()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.history_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        onLastTermDeleted = context as OnLastTermDeleted
        if (PrefManager.isDarkTheme(context)) {
            holder.icon.setImageDrawable(
                    ResourcesCompat.getDrawable(context.resources,R.drawable.ic_outline_history_24px_white,null))
            holder.deleteButton.setImageDrawable(
                    ResourcesCompat.getDrawable(context.resources,R.drawable.ic_outline_delete_forever_24px_white,null))
        }
        val searchTerm = list[position].searchTerm
        holder.term.text = list[position].searchTerm
        holder.date.text = calculatePastTime(list[position].searchDate)
        holder.root.setOnClickListener {
            val searchIntent = Intent(context, SearchActivity::class.java)
            val bundle = Bundle()
            bundle.putString("search_term", searchTerm)
            searchIntent.putExtras(bundle)
            context.startActivity(searchIntent)
        }
        holder.deleteButton.setOnClickListener {
            Thread {
                if (itemCount == 1) {
                    onLastTermDeleted!!.onLastTermDeleted()
                }
                historyDatabase.historyDao().delete(list[position])
                list = historyDatabase.historyDao().allSearchHistory as ArrayList<HistoryItem>
                list.reverse()
                (context as Activity).runOnUiThread {
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, itemCount)
                }
            }.start()
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var deleteButton: ImageButton = itemView.findViewById(R.id.history_item_button)
        var icon: ImageView = itemView.findViewById(R.id.history_item_icon)
        var term: TextView = itemView.findViewById(R.id.history_item_term)
        var date: TextView = itemView.findViewById(R.id.history_item_date)
        var root: RelativeLayout = itemView.findViewById(R.id.history_item_root)
    }

    @SuppressLint("SimpleDateFormat")
    private fun calculatePastTime(date: Date?): String {
        val format: DateFormat = SimpleDateFormat("EEE. MMM d, yyyy")
        val formatDay: DateFormat = SimpleDateFormat("EEEE")
        val formatNoYear: DateFormat = SimpleDateFormat("EEE. MMM d")
        val currentCalendar = Calendar.getInstance()
        currentCalendar.time = Calendar.getInstance().time
        val day = currentCalendar[Calendar.DAY_OF_YEAR]
        val year = currentCalendar[Calendar.YEAR]
        val calendarToCompare = Calendar.getInstance()
        calendarToCompare.time = date!!
        val dayToCompare = calendarToCompare[Calendar.DAY_OF_YEAR]
        val yearToCompare = calendarToCompare[Calendar.YEAR]
        val diff = currentCalendar.timeInMillis - calendarToCompare.timeInMillis
        val seconds: Long
        val minutes: Long
        val hours: Long
        val days: Long
        seconds = diff / 1000
        return if (seconds >= 60) {
            minutes = seconds / 60
            if (minutes >= 60) {
                hours = minutes / 60
                if (hours >= 24) {
                    days = hours / 24
                    if (days < 2 && day - dayToCompare < 2 && day - dayToCompare > 0) {
                        context.resources.getString(R.string.time_calculation_yesterday)
                    } else if (days < 7 && day - dayToCompare < 7 && day - dayToCompare > 0) {
                        formatDay.format(date)
                    } else {
                        if (yearToCompare == year) {
                            formatNoYear.format(date)
                        } else {
                            format.format(date)
                        }
                    }
                } else {
                    if (hours == 1L) {
                        context.resources.getString(R.string.time_calculation_hour, 1)
                    } else {
                        context.resources.getString(R.string.time_calculation_hours, hours)
                    }
                }
            } else {
                context.resources.getString(R.string.time_calculation_minutes, minutes)
            }
        } else {
            context.resources.getString(R.string.time_calculation_few_moments)
        }
    }

    init {
        this.list = list
        historyDatabase = Room.databaseBuilder(context, HistoryDatabase::class.java, HistoryFragment.HISTORY_DB_NAME).build()
    }
}