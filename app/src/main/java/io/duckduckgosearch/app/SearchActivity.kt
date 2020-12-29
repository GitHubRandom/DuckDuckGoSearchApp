package io.duckduckgosearch.app

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.WebView
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
import io.duckduckgosearch.app.WebViewFragment.Companion.newInstance
import java.util.*

class SearchActivity : AppCompatActivity(), OnSearchTermChange, AutoCompleteAdapter.OnItemClickListener, OnWebViewError, OnReloadButtonClick, OnPageFinish {

    // Views initialization
    private lateinit var searchBar: DuckAutoCompleteTextView
    private lateinit var fragmentManager: FragmentManager
    private lateinit var progressBar: ProgressBar
    private lateinit var activity: Activity
    private lateinit var duckLogo: ImageView
    private lateinit var eraseTextButton: ImageButton
    private lateinit var searchBarRoot: RelativeLayout
    private var latestTerm: String = ""
    private lateinit var intentSearchTerm: String
    private var webViewFragment: WebViewFragment? = null
    private var adapter: AutoCompleteAdapter? = null
    private lateinit var manager: InputMethodManager
    private lateinit var historyDatabase: HistoryDatabase
    private var darkTheme = false
    private var fromIntent = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        if (PrefManager.isDarkTheme(this)) {
            setTheme(R.style.AppTheme_Dark_NoActionBar)
            darkTheme = true
        }

        // Enable WebView debugging from PC (Chrome)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true)
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
        progressBar.visibility = View.GONE
        searchBar = findViewById(R.id.search_bar_edittext)
        adapterUpdate()
        eraseTextButton = findViewById(R.id.erase_button)
        val bundle = intent.extras
        if (bundle != null && bundle.containsKey("search_term")) {
            intentSearchTerm = bundle.getString("search_term").toString()
            fromIntent = true
            search(intentSearchTerm)
        }
        if (savedInstanceState == null && !fromIntent) {
            searchBar.requestFocus()
            eraseTextButton.visibility = View.GONE
        } else {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
            if (!searchBar.hasFocus()) {
                duckLogo.visibility = View.VISIBLE
            }
        }
        searchBar.setOnEditorActionListener(OnEditorActionListener { v, actionId, _ ->
            if (v.text.toString() != "" && actionId == EditorInfo.IME_ACTION_DONE) {
                search(v.text.toString())
                if (adapter != null) {
                    adapter!!.notifyDataSetChanged()
                }
                return@OnEditorActionListener true
            }
            false
        })
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {
                if (p0.toString() == "") {
                    eraseTextButton.visibility = View.GONE
                } else if (eraseTextButton.visibility == View.GONE) {
                    eraseTextButton.visibility = View.VISIBLE
                }
            }
        })
        searchBar.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                duckLogo.visibility = View.GONE
                if (resources.getBoolean(R.bool.isTabletAndLandscape)) {
                    searchBarRoot.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT)
                }
                if (webViewFragment != null) {
                    fragmentManager.beginTransaction()
                            .hide(webViewFragment!!)
                            .commit()
                }
            } else {
                duckLogo.visibility = View.VISIBLE
                if (resources.getBoolean(R.bool.isTabletAndLandscape)) {
                    searchBarRoot.layoutParams = LinearLayout.LayoutParams(550, ViewGroup.LayoutParams.WRAP_CONTENT)
                }
                if (webViewFragment != null) {
                    fragmentManager.beginTransaction()
                            .show(webViewFragment!!)
                            .commit()
                }
            }
        }
        eraseTextButton.setOnClickListener {
            searchBar.setText("")
            searchBar.requestFocus()
            manager.showSoftInput(searchBar, 0)
        }
        if (darkTheme) {
            findViewById<View>(R.id.search_bar_root).setBackgroundColor(
                    ResourcesCompat.getColor(resources,R.color.darkThemeColorPrimary,null))
            searchBarRoot.background = AppCompatResources.getDrawable(this, R.drawable.search_field_bg_dark)
            findViewById<View>(R.id.frame_layout).setBackgroundColor(
                    ResourcesCompat.getColor(resources,R.color.darkThemeColorPrimary,null))
            eraseTextButton.setImageDrawable(
                    AppCompatResources.getDrawable(this, R.drawable.ic_outline_close_24px_white))
            findViewById<ImageView>(R.id.duck_logo).setImageDrawable(
                    AppCompatResources.getDrawable(this,R.drawable.duckduckgo_white_logo))
        }
    }

    override fun onSearchTermChange(searchTerm: String?) {
        searchBar.setText(searchTerm)
    }

    override fun onBackPressed() {
        if (searchBar.hasFocus() && webViewFragment != null) {
            webViewFragment!!.requestFocusOnWebView()
            searchBar.setText(latestTerm)
        } else {
            super.onBackPressed()
        }
    }

    private fun adapterUpdate() {
        Thread {
            val historyArrayList = historyDatabase.historyDao().allSearchHistory as ArrayList<HistoryItem>
            historyArrayList.reverse()
            val historyArrayListStrings = ArrayList<String>()
            for (item in historyArrayList) {
                historyArrayListStrings.add(item.searchTerm)
            }
            val historyArray = Arrays.copyOf<String, Any>(historyArrayListStrings.toTypedArray(), historyArrayList.size, Array<String>::class.java)
            adapter = AutoCompleteAdapter(this@SearchActivity, R.layout.auto_complete_item, historyArray, searchBar)
            activity.runOnUiThread {
                searchBar.setAdapter(adapter)
                (searchBar.adapter as AutoCompleteAdapter).filter.filter("")
            }
        }.start()
    }

    private fun search(searchTerm: String) {
        progressBar.visibility = View.VISIBLE
        webViewFragment = newInstance(searchTerm, PrefManager.isHistoryEnabled(this))
        latestTerm = searchTerm
        if (fromIntent) {
            searchBar.setText(latestTerm)
        }
        fragmentManager.beginTransaction()
                .replace(R.id.frame_layout, webViewFragment!!)
                .commit()
        searchBar.clearFocus()
        searchBar.setSelection(0)
        manager.hideSoftInputFromWindow(searchBar.windowToken, 0)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
    }

    override fun onItemClickListener(searchTerm: String) {
        search(searchTerm)
    }

    override fun onWebViewError(errorCode: Int) {
        if (errorCode == WebViewClient.ERROR_HOST_LOOKUP || errorCode == WebViewClient.ERROR_TIMEOUT || errorCode == WebViewClient.ERROR_CONNECT) {
            progressBar.visibility = View.GONE
            fragmentManager.beginTransaction()
                    .replace(R.id.frame_layout, ErrorFragment())
                    .commit()
        }
    }

    override fun onReloadButtonClick() {
        if (latestTerm == "") {
            latestTerm = searchBar.text.toString()
        }
        search(latestTerm)
    }

    override fun onPageFinish() {
        adapterUpdate()
        runOnUiThread { progressBar.visibility = View.GONE }
    }
}