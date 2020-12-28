package io.duckduckgosearch.app

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.room.Room
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.*

class WebViewFragment : Fragment() {
    private var data: String? = ""
    private var addHistory = false
    private lateinit var webView: WebView
    private lateinit var onSearchTermChange: OnSearchTermChange
    private lateinit var onWebViewError: OnWebViewError
    private lateinit var historyDatabase: HistoryDatabase
    private lateinit var onPageFinish: OnPageFinish
    private lateinit var cookieManager: CookieManager

    interface OnPageFinish {
        fun onPageFinish()
    }

    interface OnSearchTermChange {
        fun onSearchTermChange(searchTerm: String?)
    }

    interface OnWebViewError {
        fun onWebViewError(errorCode: Int)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_web_view, container, false)
        historyDatabase = Room.databaseBuilder(requireContext(), HistoryDatabase::class.java, HistoryFragment.HISTORY_DB_NAME)
                .build()
        if (arguments != null) {
            data = requireArguments().getString("data")
            addHistory = requireArguments().getBoolean("add_history")
        }

        onPageFinish = requireActivity() as OnPageFinish
        onSearchTermChange = requireActivity() as OnSearchTermChange
        onWebViewError = requireActivity() as OnWebViewError

        cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)

        webView = view.findViewById(R.id.search_web_view)
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        if (PrefManager.isDarkTheme(requireContext())) {
            webView.setBackgroundColor(ResourcesCompat.getColor(resources,R.color.darkThemeColorPrimary,null))
        }
        webView.visibility = View.INVISIBLE
        webView.settings.javaScriptEnabled = true
        setCookies()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            webView.webViewClient = object : WebViewClient() {
                override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                    onWebViewError.onWebViewError(error.errorCode)
                }

                override fun onPageFinished(view: WebView, url: String) {
                    webView.loadUrl(
                            "javascript:$(\".header--aside\").remove(); $(\"#header_wrapper\").css(\"padding-top\", \"0\"); $(\"#duckbar_dropdowns\").remove();"
                    )
                    if (addHistory) {
                        Thread {
                            historyDatabase.historyDao().insertAll(HistoryItem(data!!.trim { it <= ' ' }, Calendar.getInstance().time))
                            onPageFinish.onPageFinish()
                        }.start()
                    }
                    webView.visibility = View.VISIBLE
                }

                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                    val url = request.url.toString()
                    if (!url.contains("duckduckgo.com/?q=")) {
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        startActivity(browserIntent)
                        return true
                    } else {
                        webView.visibility = View.GONE
                        try {
                            if (url.contains("&")) {
                                onSearchTermChange.onSearchTermChange(
                                        URLDecoder.decode(url.substring(url.indexOf("?q=") + 3, url.indexOf("&")), "UTF-8"))
                            } else {
                                onSearchTermChange.onSearchTermChange(
                                        URLDecoder.decode(url.substring(url.indexOf("?q=") + 3), "UTF-8"))
                            }
                        } catch (e: UnsupportedEncodingException) {
                            e.printStackTrace()
                        }
                        updateSafeSearchValues(cookieManager.getCookie("duckduckgo.com"))
                    }
                    return false
                }
            }
        } else {
            webView.webViewClient = object : WebViewClient() {
                override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
                    onWebViewError.onWebViewError(errorCode)
                }

                override fun onPageFinished(view: WebView, url: String) {
                    webView.loadUrl(
                            "javascript:$(\".header--aside\").remove(); $(\"#header_wrapper\").css(\"padding-top\", \"0\"); $(\"#duckbar_dropdowns\").remove();"
                    )
                    if (addHistory) {
                        Thread {
                            historyDatabase.historyDao().insertAll(HistoryItem(data!!.trim { it <= ' ' }, Calendar.getInstance().time))
                            onPageFinish.onPageFinish()
                        }.start()
                    }
                    webView.visibility = View.VISIBLE
                }

                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                    if (!url.contains("duckduckgo.com/?q=")) {
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        startActivity(browserIntent)
                        return true
                    } else {
                        webView.visibility = View.GONE
                        try {
                            if (url.contains("&")) {
                                onSearchTermChange.onSearchTermChange(
                                        URLDecoder.decode(url.substring(url.indexOf("?q=") + 3, url.indexOf("&")), "UTF-8"))
                            } else {
                                onSearchTermChange.onSearchTermChange(
                                        URLDecoder.decode(url.substring(url.indexOf("?q=") + 3), "UTF-8"))
                            }
                        } catch (e: UnsupportedEncodingException) {
                            e.printStackTrace()
                        }
                    }
                    updateSafeSearchValues(cookieManager.getCookie("duckduckgo.com"))
                    return false
                }
            }
        }
        try {
            webView.loadUrl(SEARCH_URL + URLEncoder.encode(data, "utf-8"))
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        return view
    }

    fun requestFocusOnWebView() {
        webView.requestFocus()
    }

    private fun setCookies() {
        cookieManager.setCookie("duckduckgo.com", "o=-1")
        when (PrefManager.getTheme(context)) {
            "default" -> cookieManager.setCookie("duckduckgo.com", DEFAULT_COOKIE)
            "basic" -> cookieManager.setCookie("duckduckgo.com", BASIC_COOKIE)
            "gray" -> cookieManager.setCookie("duckduckgo.com", GRAY_COOKIE)
            "dark" -> cookieManager.setCookie("duckduckgo.com", DARK_COOKIE)
        }
        when (PrefManager.getSafeSearchLevel(context)) {
            "off" -> cookieManager.setCookie("duckduckgo.com", "p=-2")
            "moderate" -> cookieManager.setCookie("duckduckgo.com", "p=")
            "strict" -> cookieManager.setCookie("duckduckgo.com", "p=1")
        }
    }

    private fun updateSafeSearchValues(cookies: String) {
        if (cookies.contains(" p=-2") && PrefManager.getSafeSearchLevel(context) != "off") {
            PreferenceManager.getDefaultSharedPreferences(context)
                    .edit()
                    .putString("safe_search", "off")
                    .apply()
        } else if (!cookies.contains(" p=") || ((cookies.contains(" p=")
                        && PrefManager.getSafeSearchLevel(context) != "moderate"))) {
            PreferenceManager.getDefaultSharedPreferences(context)
                    .edit()
                    .putString("safe_search", "moderate")
                    .apply()
        } else if (cookies.contains(" p=1") && PrefManager.getSafeSearchLevel(context) != "strict") {
            PreferenceManager.getDefaultSharedPreferences(context)
                    .edit()
                    .putString("safe_search", "strict")
                    .apply()
        }
    }

    companion object {
        private const val SEARCH_URL = "https://duckduckgo.com/?q="
        private const val DEFAULT_COOKIE = "ae=-1"
        private const val BASIC_COOKIE = "ae=b"
        private const val GRAY_COOKIE = "ae=g"
        private const val DARK_COOKIE = "ae=d"
        fun newInstance(data: String, addHistory: Boolean): WebViewFragment {
            val webViewFragment = WebViewFragment()
            val bundle = Bundle()
            bundle.putString("data", data)
            bundle.putBoolean("add_history", addHistory)
            webViewFragment.arguments = bundle
            return webViewFragment
        }
    }
}