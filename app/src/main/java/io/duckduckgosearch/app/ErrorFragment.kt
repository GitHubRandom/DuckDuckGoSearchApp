package io.duckduckgosearch.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class ErrorFragment : Fragment() {
    private lateinit var reloadButtonClick: OnReloadButtonClick

    interface OnReloadButtonClick {
        fun onReloadButtonClick()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_search_error, container, false)
        val reload = view.findViewById<Button>(R.id.reload_button)

        reloadButtonClick = context as OnReloadButtonClick
        reload.setOnClickListener {
            reloadButtonClick.onReloadButtonClick()
        }
        return view
    }

    companion object {
        val TAG: String = ErrorFragment::class.simpleName.toString()
    }
}