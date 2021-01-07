package io.duckduckgosearch.app

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
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
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import io.duckduckgosearch.app.ErrorFragment.OnReloadButtonClick
import io.duckduckgosearch.app.WebViewFragment.*
import io.duckduckgosearch.app.WebViewFragment.Companion.newInstance
import java.util.*

class SearchActivity : AppCompatActivity(), OnSearchTermChange, AutoCompleteAdapter.OnItemClickListener, OnWebViewError, OnReloadButtonClick, OnPageFinish {

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
    private var errorFragment: ErrorFragment? = null
    private var adapter: AutoCompleteAdapter? = null
    private lateinit var manager: InputMethodManager
    private var historyDatabase: HistoryDatabase? = null
    private var fromIntent = false

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        AppCompatDelegate.setDefaultNightMode(PrefManager.getThemeInt(this))

        this.transparentStatusBar()
        // Enable WebView debugging from PC (Chrome)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true)
        }
        setContentView(R.layout.activity_search)
        historyDatabase = HistoryDatabase.getHistoryDatabase(this)
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
                adapter?.notifyDataSetChanged()
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
                webViewFragment?.let {
                    fragmentManager.commit {
                        hide(it)
                    }
                }
                errorFragment?.let {
                    fragmentManager.commit {
                        hide(it)
                    }
                }
            } else {
                duckLogo.visibility = View.VISIBLE
                if (resources.getBoolean(R.bool.isTabletAndLandscape)) {
                    searchBarRoot.layoutParams = LinearLayout.LayoutParams(550, ViewGroup.LayoutParams.WRAP_CONTENT)
                }
                webViewFragment?.let {
                    fragmentManager.commit {
                        show(it)
                    }
                }
                errorFragment?.let {
                    fragmentManager.commit {
                        show(it)
                    }
                }
            }
        }
        eraseTextButton.setOnClickListener {
            searchBar.setText("")
            searchBar.requestFocus()
            manager.showSoftInput(searchBar, 0)
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        webViewFragment = fragmentManager.getFragment(savedInstanceState,WebViewFragment.TAG) as WebViewFragment?
        errorFragment = fragmentManager.getFragment(savedInstanceState,ErrorFragment.TAG) as ErrorFragment?
        latestTerm = savedInstanceState.getString("latest_term","")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("latest_term",latestTerm)
        webViewFragment?.let {
            if (it.isAdded) {
                fragmentManager.putFragment(outState, WebViewFragment.TAG, it)
            }
        }
        errorFragment?.let {
            if (it.isAdded) {
                fragmentManager.putFragment(outState, ErrorFragment.TAG, it)
            }
        }
    }

    override fun onSearchTermChange(searchTerm: String?) {
        searchBar.setText(searchTerm)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (webViewFragment == null) {
            finish()
            startActivity(intent)
        } else {
            val relaunchIntent = Intent(this, SearchActivity::class.java)
            relaunchIntent.putExtras(bundleOf("search_term" to latestTerm))
            finish()
            startActivity(relaunchIntent)
        }
    }

    override fun onBackPressed() {
        if (searchBar.hasFocus() && (webViewFragment != null || errorFragment != null)) {
            searchBar.clearFocus()
            webViewFragment?.requestFocusOnWebView()
            searchBar.setText(latestTerm)
        } else {
            super.onBackPressed()
        }
    }

    private fun adapterUpdate() {
        Thread {
            val historyArrayList = historyDatabase?.historyDao()?.allSearchHistory as ArrayList<HistoryItem>
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
        webViewFragment?.let {
            fragmentManager.commit {
                replace(R.id.frame_layout, it)
            }
        }
        errorFragment = null
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
            errorFragment = ErrorFragment()
            fragmentManager.commit {
                replace(R.id.frame_layout, errorFragment!!)
            }
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