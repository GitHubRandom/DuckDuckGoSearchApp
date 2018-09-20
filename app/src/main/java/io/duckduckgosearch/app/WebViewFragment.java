package io.duckduckgosearch.app;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class WebViewFragment extends android.support.v4.app.Fragment {

    private static final String SEARCH_URL = "https://duckduckgo.com/?q=";
    private static final String SEARCH_URL_END = "&ia=web";
    String data;
    WebView webView;

    public WebViewFragment() {
    }

    public static WebViewFragment newInstance(@NonNull String data) {
        WebViewFragment webViewFragment = new WebViewFragment();
        Bundle bundle = new Bundle();
        bundle.putString("data", data);
        webViewFragment.setArguments(bundle);
        return webViewFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_web_view, container, false);
        data = getArguments().getString("data");
        webView = view.findViewById(R.id.search_web_view);
        webView.setVisibility(View.INVISIBLE);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                webView.loadUrl("javascript:$(\".header__search-wrap\").remove(); " +
                        "$(\".header\").css(\"min-height\", \"auto\"); " +
                        "$(\"#header_wrapper\").css(\"padding-top\", \"0\");" +
                        "$(\".header--aside\").remove();");
                webView.setVisibility(View.VISIBLE);
            }
        });
        webView.loadUrl(SEARCH_URL + data + SEARCH_URL_END);
        return view;
    }
}
