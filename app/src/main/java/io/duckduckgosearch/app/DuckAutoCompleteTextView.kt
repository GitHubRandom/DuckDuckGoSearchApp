package io.duckduckgosearch.app

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.widget.AutoCompleteTextView

@SuppressLint("AppCompatCustomView")
class DuckAutoCompleteTextView : AutoCompleteTextView {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun getThreshold(): Int {
        return 0
    }

    override fun enoughToFilter(): Boolean {
        return true
    }

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)
        if (focused && adapter != null) {
            (adapter as AutoCompleteAdapter).filter.filter(text)
        }
    }
}