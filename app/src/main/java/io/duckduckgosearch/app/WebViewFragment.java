package io.duckduckgosearch.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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
        webView.setVisibility(View.INVISIBLE);
        webView.getSettings().setJavaScriptEnabled(true);
        CookieManager.getInstance().removeAllCookie();
        CookieManager.getInstance().setCookie("duckduckgo.com", "o=-1");
        switch (PrefManager.getTheme(getContext())) {
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
                                Log.i("History", "Add term success");
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
                    return false;
                }
            });
        } else {
            webView.setWebViewClient(new WebViewClient() {
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

}
