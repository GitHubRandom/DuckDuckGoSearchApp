package io.duckduckgosearch.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.room.Room;


public class WebViewFragment extends Fragment {

    private static final String SEARCH_URL = "https://duckduckgo.com/?q=";
    private static final String DEFAULT_COOKIE = "ae=''";
    private static final String BASIC_COOKIE = "ae=b";
    private static final String GRAY_COOKIE = "ae=g";
    private static final String DARK_COOKIE = "ae=d";
    String data = "";
    private boolean addHistory = false;
    private WebView webView;
    private Context context;
    private OnSearchTermChange onSearchTermChange;
    private OnWebViewError onWebViewError;
    private HistoryDatabase historyDatabase;
    private OnPageFinish onPageFinish;

    public interface OnPageFinish {
        void onPageFinish();
    }

    public interface OnSearchTermChange {
        void onSearchTermChange(String searchTerm);
    }

    public interface OnWebViewError {
        void onWebViewError(int errorDescription);
    }

    public WebViewFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    static WebViewFragment newInstance(@NonNull String data, boolean addHistory) {
        WebViewFragment webViewFragment = new WebViewFragment();
        Bundle bundle = new Bundle();
        bundle.putString("data", data);
        bundle.putBoolean("add_history", addHistory);
        webViewFragment.setArguments(bundle);
        return webViewFragment;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_web_view, container, false);
        historyDatabase = Room.databaseBuilder(getContext(), HistoryDatabase.class, HistoryFragment.HISTORY_DB_NAME)
                .build();
        if (getArguments() != null) {
            data = getArguments().getString("data");
            addHistory = getArguments().getBoolean("add_history");
        }
        webView = view.findViewById(R.id.search_web_view);
        if (PrefManager.isDarkTheme(getContext())) {
            webView.setBackgroundColor(getResources().getColor(R.color.darkThemeColorPrimary));
        }
        webView.setVisibility(View.INVISIBLE);
        webView.getSettings().setJavaScriptEnabled(true);
        setCookies();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                    onWebViewError = (OnWebViewError) context;
                    onWebViewError.onWebViewError(error.getErrorCode());
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    webView.loadUrl(
                            "javascript:$(\".header--aside\").remove(); $(\"#header_wrapper\").css(\"padding-top\", \"0\")"
                    );
                    if (addHistory) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                historyDatabase.historyDao().insertAll(new HistoryItem(data.trim(), Calendar.getInstance().getTime()));
                                onPageFinish = (OnPageFinish) context;
                                onPageFinish.onPageFinish();
                            }
                        }).start();
                    }
                    webView.setVisibility(View.VISIBLE);
                }

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                    String url = request.getUrl().toString();
                    if (!url.contains("duckduckgo.com/?q=")) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(browserIntent);
                        return true;
                    } else {
                        webView.setVisibility(View.GONE);
                        onSearchTermChange = (OnSearchTermChange) context;
                        try {
                            if (url.contains("&")) {
                                onSearchTermChange.onSearchTermChange(
                                        URLDecoder.decode(url.substring(url.indexOf("?q=") + 3, url.indexOf("&")), "UTF-8"));
                            } else {
                                onSearchTermChange.onSearchTermChange(
                                        URLDecoder.decode(url.substring(url.indexOf("?q=") + 3), "UTF-8"));
                            }
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        updateSafeSearchValues(CookieManager.getInstance().getCookie("duckduckgo.com"));
                    }
                    return false;
                }
            });
        } else {
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                    onWebViewError = (OnWebViewError) context;
                    onWebViewError.onWebViewError(errorCode);
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    webView.loadUrl(
                            "javascript:$(\".header--aside\").remove(); $(\"#header_wrapper\").css(\"padding-top\", \"0\")"
                    );
                    if (addHistory) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                historyDatabase.historyDao().insertAll(new HistoryItem(data.trim(), Calendar.getInstance().getTime()));
                                onPageFinish = (OnPageFinish) context;
                                onPageFinish.onPageFinish();
                            }
                        }).start();
                    }
                    webView.setVisibility(View.VISIBLE);
                }

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    if (!url.contains("duckduckgo.com/?q=")) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(browserIntent);
                        return true;
                    } else {
                        webView.setVisibility(View.GONE);
                        onSearchTermChange = (OnSearchTermChange) context;
                        try {
                            if (url.contains("&")) {
                                onSearchTermChange.onSearchTermChange(
                                        URLDecoder.decode(url.substring(url.indexOf("?q=") + 3, url.indexOf("&")), "UTF-8"));
                            } else {
                                onSearchTermChange.onSearchTermChange(
                                        URLDecoder.decode(url.substring(url.indexOf("?q=") + 3), "UTF-8"));
                            }
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                    updateSafeSearchValues(CookieManager.getInstance().getCookie("duckduckgo.com"));
                    return false;
                }
            });
        }
        try {
            webView.loadUrl(SEARCH_URL + URLEncoder.encode(data, "utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return view;
    }

    void requestFocusOnWebView() {
        webView.requestFocus();
    }

    private void setCookies() {
        CookieManager.getInstance().setCookie("duckduckgo.com", "o=-1");
        switch (PrefManager.getTheme(getContext())) {
            case "default":
                CookieManager.getInstance().setCookie("duckduckgo.com", DEFAULT_COOKIE);
                break;
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
        switch (PrefManager.getSafeSearchLevel(getContext())) {
            case "off":
                CookieManager.getInstance().setCookie("duckduckgo.com", "p=-2");
                break;
            case "moderate":
                CookieManager.getInstance().setCookie("duckduckgo.com", "p=");
                break;
            case "strict":
                CookieManager.getInstance().setCookie("duckduckgo.com", "p=1");
                break;
        }
    }

    private void updateSafeSearchValues(String cookies) {
        if (cookies.contains(" p=-2") && !PrefManager.getSafeSearchLevel(getContext()).equals("off")) {
            PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putString("safe_search", "off").apply();
        } else if (!cookies.contains(" p=") || (cookies.contains(" p=")
                && !PrefManager.getSafeSearchLevel(getContext()).equals("moderate"))) {
            PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putString("safe_search", "moderate").apply();
        } else if (cookies.contains(" p=1") && !PrefManager.getSafeSearchLevel(getContext()).equals("strict")) {
            PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putString("safe_search", "strict").apply();
        }
    }

}
