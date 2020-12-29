package io.duckduckgosearch.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment

class ErrorFragment : Fragment() {
    private var reloadButtonClick: OnReloadButtonClick? = null

    interface OnReloadButtonClick {
        fun onReloadButtonClick()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_search_error, container, false)
        val reload = view.findViewById<Button>(R.id.reload_button)
        reload.setOnClickListener {
            reloadButtonClick = context as OnReloadButtonClick?
            reloadButtonClick!!.onReloadButtonClick()
        }
        if (PrefManager.isDarkTheme(requireContext())) {
            view.findViewById<TextView>(R.id.error_text).setTextColor(ResourcesCompat.getColor(resources,android.R.color.white,null))
            reload.setTextColor(ResourcesCompat.getColor(resources, R.color.darkThemeColorAccent, null))
            reload.background = ResourcesCompat.getDrawable(resources, R.drawable.retry_button_bg_white, null)
        }
        return view
    }
}