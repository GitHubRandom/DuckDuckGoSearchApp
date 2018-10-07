package io.duckduckgosearch.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


public class WebViewFragment extends Fragment {

    private static final String SEARCH_URL = "https://duckduckgo.com/?q=";
    private static final String BASIC_COOKIE = "ae=b";
    private static final String GRAY_COOKIE = "ae=g";
    private static final String DARK_COOKIE = "ae=d";
    String data = "";
    WebView webView;
    Context context;
    SharedPreferences preferences;
    OnSearchTermChange onSearchTermChange;

    public interface OnSearchTermChange {
        void onSearchTermChange(String searchTerm);
    }

    public WebViewFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    public static WebViewFragment newInstance(@NonNull String data) {
        WebViewFragment webViewFragment = new WebViewFragment();
        Bundle bundle = new Bundle();
        bundle.putString("data", data);
        webViewFragment.setArguments(bundle);
        return webViewFragment;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_web_view, container, false);
        if (getArguments() != null) {
            data = getArguments().getString("data");
        }
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        webView = view.findViewById(R.id.search_web_view);
        webView.setVisibility(View.INVISIBLE);
        webView.getSettings().setJavaScriptEnabled(true);
        CookieManager.getInstance().removeAllCookie();
        CookieManager.getInstance().setCookie("duckduckgo.com", "o=-1");
        switch (preferences.getString("app_theme", "default")) {
            case "basic":
                CookieManager.getInstance().setCookie("duckduckgo.com", BASIC_COOKIE);
                break;
            case "gray":
                CookieManager.getInstance().setCookie("duckduckgo.com", GRAY_COOKIE);
                break;
            case "dark":
                CookieManager.getInstance().setCookie("duckduckgo.com", DARK_COOKIE);
                break;
        }
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                webView.loadUrl(
                        "javascript:$(\".header--aside\").remove(); $(\"#header_wrapper\").css(\"padding-top\", \"0\")"
                );
                webView.setVisibility(View.VISIBLE);
                webView.requestFocus();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (!url.contains("duckduckgo.com/?q=")) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(browserIntent);
                    return true;
                } else {
                    onSearchTermChange = (OnSearchTermChange) context;
                    try {
                        onSearchTermChange.onSearchTermChange(
                                URLDecoder.decode(url.substring(url.indexOf("?q=") + 3, url.indexOf("&")), "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }
        });
        webView.loadUrl(SEARCH_URL + data);
        return view;
    }

    void requestFocusOnWebView() {
        webView.requestFocus();
    }

}
