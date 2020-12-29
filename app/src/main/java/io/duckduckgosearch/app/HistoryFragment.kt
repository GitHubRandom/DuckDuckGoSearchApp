package io.duckduckgosearch.app

import android.app.Activity
import android.app.Dialog
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.*

class HistoryFragment : BottomSheetDialogFragment() {

    override fun setupDialog(dialog: Dialog, style: Int) {
        val view = View.inflate(context, R.layout.fragment_history, null)
        val historyDatabase = Room.databaseBuilder(requireContext(), HistoryDatabase::class.java, HISTORY_DB_NAME).build()
        val root: CoordinatorLayout = view.findViewById(R.id.search_history_root)
        val fragmentTitle = view.findViewById<TextView>(R.id.history_fragment_title)
        val historyListRv = view.findViewById<RecyclerView>(R.id.history_fragment_list)
        historyListRv.setHasFixedSize(true)
        if (PrefManager.isHistoryEnabled(requireContext())) {
            Thread {
                val historyList = historyDatabase.historyDao().allSearchHistory as ArrayList<HistoryItem>
                if (historyList.size == 0) {
                    (context as Activity?)!!.runOnUiThread { fragmentTitle.setText(R.string.search_history_empty) }
                }
                historyList.reverse()
                val adapter = HistoryAdapter(requireContext(), historyList)
                (context as Activity?)!!.runOnUiThread { historyListRv.adapter = adapter }
            }.start()
        } else {
            fragmentTitle.setText(R.string.search_history_disabled)
        }
        val dragIcon = view.findViewById<ImageView>(R.id.history_fragment_drag_icon)
        val manager: RecyclerView.LayoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        val animator = DefaultItemAnimator()
        historyListRv.itemAnimator = animator
        historyListRv.layoutManager = manager
        if (PrefManager.isDarkTheme(requireContext())) {
            root.background = ResourcesCompat.getDrawable(resources, R.drawable.history_fragment_bg_dark, null)
            fragmentTitle.setTextColor(ResourcesCompat.getColor(resources,R.color.colorPrimary,null))
            dragIcon.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_round_drag_handle_24px_dark, null))
        }
        dialog.setContentView(view)
        (view.parent as View).background = null
    }

    companion object {
        const val HISTORY_DB_NAME = "history_db"
    }
}