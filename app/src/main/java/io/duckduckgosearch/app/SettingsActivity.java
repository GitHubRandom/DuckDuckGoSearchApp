package io.duckduckgosearch.app;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class SettingsActivity extends AppCompatActivity {

    public static final String SETTINGS_URL = "https://duckduckgo.com/settings";
    WebView settingsWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        CookieManager.getInstance().setAcceptCookie(true);

        settingsWebView = findViewById(R.id.settings_web_view);
        settingsWebView.loadUrl(SETTINGS_URL);
    }
}
