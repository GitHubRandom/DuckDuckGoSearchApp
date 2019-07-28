package io.duckduckgosearch.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;

@SuppressLint("AppCompatCustomView")
public class DuckAutoCompleteTextView extends AutoCompleteTextView {

    public DuckAutoCompleteTextView(Context context) {
        super(context);
    }

    public DuckAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DuckAutoCompleteTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public int getThreshold() {
        return 0;
    }

    @Override
    public boolean enoughToFilter() {
        return true;
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (focused && getAdapter() != null) {
            ((AutoCompleteAdapter)getAdapter()).getFilter().filter(getText());
        }
    }
}