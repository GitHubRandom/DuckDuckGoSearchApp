package io.duckduckgosearch.app

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.WebViewClient
import android.widget.*
import android.widget.TextView.OnEditorActionListener
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentManager
import androidx.room.Room
import io.duckduckgosearch.app.ErrorFragment.OnReloadButtonClick
import io.duckduckgosearch.app.WebViewFragment.*
import java.util.*

class SearchActivity : AppCompatActivity(), OnSearchTermChange, AutoCompleteAdapter.OnItemClickListener, OnWebViewError, OnReloadButtonClick, OnPageFinish {
    var searchBar: DuckAutoCompleteTextView? = null
    var fragmentManager: FragmentManager? = null
    var progressBar: ProgressBar? = null
    var activity: Activity? = null
    var duckLogo: ImageView? = null
    var eraseTextButton: ImageButton? = null
    var searchBarRoot: RelativeLayout? = null
    var latestTerm: String? = ""
    var intentSearchTerm: String? = null
    var webViewFragment: WebViewFragment? = null
    var adapter: AutoCompleteAdapter? = null
    var manager: InputMethodManager? = null
    var historyDatabase: HistoryDatabase? = null
    var darkTheme = false
    var fromIntent = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        if (PrefManager.isDarkTheme(this)) {
            setTheme(R.style.AppTheme_Dark_NoActionBar)
            darkTheme = true
        }
        setContentView(R.layout.activity_search)
        historyDatabase = Room.databaseBuilder(this, HistoryDatabase::class.java, HistoryFragment.HISTORY_DB_NAME)
                .build()
        manager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        duckLogo = findViewById(R.id.duck_logo)
        fragmentManager = supportFragmentManager
        activity = this
        searchBarRoot = findViewById(R.id.search_bar_edittext_root)
        progressBar = findViewById(R.id.search_progress)
        progressBar?.visibility = View.GONE
        searchBar = findViewById(R.id.search_bar_edittext)
        adapterUpdate()
        eraseTextButton = findViewById(R.id.erase_button)
        val bundle = intent.extras
        if (bundle != null && bundle.containsKey("search_term")) {
            intentSearchTerm = bundle.getString("search_term")
            fromIntent = true
            search(intentSearchTerm)
        }
        if (darkTheme) {
            findViewById<View>(R.id.frame_layout).setBackgroundColor(
                    ResourcesCompat.getColor(resources,R.color.darkThemeColorPrimary,null))
            eraseTextButton?.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_outline_close_24px_white))
        }
        if (savedInstanceState == null && !fromIntent) {
            searchBar?.requestFocus()
        } else {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
            if (!searchBar?.hasFocus()!!) {
                duckLogo?.visibility = View.VISIBLE
            }
        }
        searchBar?.setOnEditorActionListener(OnEditorActionListener { v, actionId, _ ->
            if (v.text.toString() != "" && actionId == EditorInfo.IME_ACTION_DONE) {
                search(v.text.toString())
                if (adapter != null) {
                    adapter!!.notifyDataSetChanged()
                }
                return@OnEditorActionListener true
            }
            false
        })
        searchBar?.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                duckLogo?.visibility = View.GONE
                if (resources.getBoolean(R.bool.isTabletAndLandscape)) {
                    searchBarRoot?.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT)
                }
                if (webViewFragment != null) {
                    fragmentManager!!.beginTransaction()
                            .hide(webViewFragment!!)
                            .commit()
                }
            } else {
                duckLogo?.visibility = View.VISIBLE
                if (resources.getBoolean(R.bool.isTabletAndLandscape)) {
                    searchBarRoot?.layoutParams = LinearLayout.LayoutParams(550, ViewGroup.LayoutParams.WRAP_CONTENT)
                }
                if (webViewFragment != null) {
                    fragmentManager!!.beginTransaction()
                            .show(webViewFragment!!)
                            .commit()
                }
            }
        }
        eraseTextButton?.setOnClickListener {
            searchBar?.setText("")
            searchBar?.requestFocus()
            manager!!.showSoftInput(searchBar, 0)
        }
        if (darkTheme) {
            findViewById<View>(R.id.search_bar_root).setBackgroundColor(
                    ResourcesCompat.getColor(resources,R.color.darkThemeColorPrimary,null))
            searchBarRoot?.background = AppCompatResources.getDrawable(this, R.drawable.search_field_bg_dark)
        }
    }

    override fun onSearchTermChange(searchTerm: String) {
        searchBar!!.setText(searchTerm)
    }

    override fun onBackPressed() {
        if (searchBar!!.hasFocus() && webViewFragment != null) {
            webViewFragment!!.requestFocusOnWebView()
            searchBar!!.setText(latestTerm)
        } else {
            super.onBackPressed()
        }
    }

    private fun adapterUpdate() {
        Thread {
            val historyArrayList = historyDatabase!!.historyDao().allSearchHistory as ArrayList<HistoryItem?>
            historyArrayList.reverse()
            val historyArrayListStrings = ArrayList<String>()
            for (item in historyArrayList) {
                historyArrayListStrings.add(item!!.getSearchTerm())
            }
            val historyArray = Arrays.copyOf<String, Any>(historyArrayListStrings.toTypedArray(), historyArrayList.size, Array<String>::class.java)
            adapter = AutoCompleteAdapter(this@SearchActivity, R.layout.auto_complete_item, historyArray, searchBar)
            activity!!.runOnUiThread {
                searchBar!!.setAdapter(adapter)
                (searchBar!!.adapter as AutoCompleteAdapter).filter.filter("")
            }
        }.start()
    }

    private fun search(searchTerm: String?) {
        progressBar!!.visibility = View.VISIBLE
        webViewFragment = newInstance(searchTerm!!, PrefManager.isHistoryEnabled(this))
        latestTerm = searchTerm
        if (fromIntent) {
            searchBar?.setText(latestTerm)
        }
        fragmentManager!!.beginTransaction()
                .replace(R.id.frame_layout, webViewFragment!!)
                .commit()
        searchBar!!.clearFocus()
        searchBar!!.setSelection(0)
        manager!!.hideSoftInputFromWindow(searchBar!!.windowToken, 0)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
    }

    override fun onItemClickListener(searchTerm: String) {
        search(searchTerm)
    }

    override fun onWebViewError(errorCode: Int) {
        if (errorCode == WebViewClient.ERROR_HOST_LOOKUP || errorCode == WebViewClient.ERROR_TIMEOUT || errorCode == WebViewClient.ERROR_CONNECT) {
            progressBar!!.visibility = View.GONE
            fragmentManager!!.beginTransaction()
                    .replace(R.id.frame_layout, ErrorFragment())
                    .commit()
        }
    }

    override fun onReloadButtonClick() {
        if (latestTerm == "") {
            latestTerm = searchBar!!.text.toString()
        }
        search(latestTerm)
    }

    override fun onPageFinish() {
        adapterUpdate()
        runOnUiThread { progressBar!!.visibility = View.GONE }
    }
}