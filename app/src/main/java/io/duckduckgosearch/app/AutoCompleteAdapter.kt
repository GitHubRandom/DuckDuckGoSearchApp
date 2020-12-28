package io.duckduckgosearch.app

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import io.duckduckgosearch.app.OnlineACParser.OnParsed
import java.util.*

/**
 * This is a custom adapter for search suggestions list
 */
class AutoCompleteAdapter constructor(context: Context, private val resId: Int,
                                               objects: Array<String>, searchBar: DuckAutoCompleteTextView) : ArrayAdapter<String>(context, resId, objects) {
    private val searchBar: DuckAutoCompleteTextView
    private var clickListener: OnItemClickListener? = null
    private var filteredList: Array<String>?
    private val list: Array<String>?
    private var suggestions: ArrayList<String>? = null
    private var historyCount: Int
    private var parser: OnlineACParser? = null

    interface OnItemClickListener {
        fun onItemClickListener(searchTerm: String?)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val results = FilterResults()
                if (list == null || list.isEmpty()) {
                    synchronized(this) {
                        results.values = null
                        results.count = 0
                    }
                } else {
                    if (charSequence.isEmpty()) {
                        if (list.size < 5) {
                            synchronized(this) {
                                results.values = Arrays.copyOf(list, list.size, Array<String>::class.java)
                                results.count = list.size
                            }
                        } else {
                            synchronized(this) {
                                results.values = Arrays.copyOf(list, 5, Array<String>::class.java)
                                results.count = 5
                            }
                        }
                    } else {
                        val matchingTerms: ArrayList<String> = ArrayList()
                        for (item: String in list) {
                            if (item.toLowerCase(Locale.getDefault()).startsWith(charSequence.toString().toLowerCase(Locale.getDefault()))) {
                                matchingTerms.add(item)
                            }
                        }
                        if (matchingTerms.size < 5) {
                            synchronized(this) {
                                results.values = Arrays.copyOf<String, Any>(matchingTerms.toTypedArray(), matchingTerms.size, Array<String>::class.java)
                                results.count = matchingTerms.size
                            }
                        } else {
                            synchronized(this) {
                                results.values = Arrays.copyOf<String, Any>(matchingTerms.toTypedArray(), 5, Array<String>::class.java)
                                results.count = 5
                            }
                        }
                    }
                }
                return results
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                if (parser != null && !parser!!.isCancelled) {
                    parser!!.cancel(true)
                }
                parser = OnlineACParser(filterResults.values as Array<String>?)
                parser!!.setOnParseListener(object : OnParsed {
                    override fun onParsed(list: ArrayList<String>?) {
                        suggestions = list
                        if (filterResults.values == null) {
                            if (suggestions != null && suggestions!!.size != 0) {
                                filteredList = if (suggestions!!.size < 5) {
                                    Arrays.copyOf<String, Any>(suggestions!!.toTypedArray(), suggestions!!.size, Array<String>::class.java)
                                } else {
                                    Arrays.copyOf<String, Any>(suggestions!!.toTypedArray(), 5, Array<String>::class.java)
                                }
                                notifyDataSetChanged()
                            } else {
                                filteredList = arrayOf()
                                notifyDataSetInvalidated()
                            }
                        } else {
                            if (suggestions != null && suggestions!!.size != 0) {
                                val finalList: ArrayList<String> = ArrayList(listOf(*filterResults.values as Array<String?>))
                                var count: Int = 5 - filterResults.count
                                if (count > suggestions!!.size) {
                                    count = suggestions!!.size
                                }
                                Log.d("Debug : ", "Suggestions size : " + suggestions!!.size)
                                Log.d("Debug : ", "Difference : $count")
                                if (filterResults.count < 5) {
                                    for (i in 0 until count) {
                                        finalList.add(suggestions!![i])
                                    }
                                }
                                filteredList = Arrays.copyOf<String, Any>(finalList.toTypedArray(), finalList.size, Array<String>::class.java)
                            } else {
                                filteredList = filterResults.values as Array<String>?
                            }
                            if (filteredList!!.isNotEmpty()) {
                                notifyDataSetChanged()
                            } else {
                                notifyDataSetInvalidated()
                            }
                        }
                    }
                })
                parser!!.execute(charSequence.toString())
                historyCount = filterResults.count
            }
        }
    }

    override fun getItem(position: Int): String {
        return filteredList!![position]
    }

    override fun getCount(): Int {
        return if (filteredList != null) {
            filteredList!!.size
        } else {
            0
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var cView: View? = convertView
        if (cView == null) {
            val inflater: LayoutInflater = (context as Activity).layoutInflater
            cView = inflater.inflate(resId, parent, false)
        }
        val pos: Int = position
        val appendButton: ImageButton = cView!!.findViewById(R.id.auto_complete_item_button)
        appendButton.setOnClickListener {
            searchBar.setText("")
            searchBar.append(getItem(pos) + " ")
        }
        val root: RelativeLayout = cView.findViewById(R.id.auto_complete_item_root)
        root.setOnClickListener {
            try {
                clickListener = context as OnItemClickListener?
                clickListener!!.onItemClickListener(getItem(pos))
                searchBar.setText(getItem(pos))
            } catch (e: ClassCastException) {
                Log.e("AutoCompleteAdapter", "You must implement the clickListenerInterface")
            }
        }
        val icon: ImageView = cView.findViewById(R.id.auto_complete_item_icon)
        if (position >= historyCount) {
            icon.setImageDrawable(ResourcesCompat.getDrawable(context.resources,R.drawable.ic_search_24px,null))
        } else {
            icon.setImageDrawable(ResourcesCompat.getDrawable(context.resources,R.drawable.ic_outline_history_24px,null))
        }
        if (PrefManager.isDarkTheme(context)) {
            appendButton.setImageDrawable(ResourcesCompat.getDrawable(context.resources,R.drawable.ic_outline_top_left_arrow_white,null))
            if (position >= historyCount) {
                icon.setImageDrawable(ResourcesCompat.getDrawable(context.resources,R.drawable.ic_search_24px_white,null))
            } else {
                icon.setImageDrawable(ResourcesCompat.getDrawable(context.resources,R.drawable.ic_outline_history_24px_white,null))
            }
            root.setBackgroundColor(ResourcesCompat.getColor(context.resources,R.color.darkThemeColorPrimary,null))
        }
        val textItem: TextView = cView.findViewById(R.id.auto_complete_item_text)
        textItem.text = getItem(position)
        return (cView)
    }

    init {
        list = objects
        filteredList = if (list.size >= 5) {
            Arrays.copyOf(list, 5, Array<String>::class.java)
        } else {
            Arrays.copyOf(list, list.size, Array<String>::class.java)
        }
        this.searchBar = searchBar
        historyCount = (filteredList as Array<String>).size
    }
}