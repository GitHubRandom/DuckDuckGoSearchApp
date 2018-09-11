package io.duckduckgosearch.app;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;


public class WebViewFragment extends android.support.v4.app.Fragment {

    String data;
    WebView webView;

    public WebViewFragment() {
    }

    public WebViewFragment newInstance(String data) {
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
        webView.loadData(data, "text/html", "UTF-8");
        return view;
    }
}
