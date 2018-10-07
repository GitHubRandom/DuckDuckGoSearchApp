package io.duckduckgosearch.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener{

    RelativeLayout searchField;
    Context context;
    ImageButton settingsButton;
    SharedPreferences preferences;
    boolean darkTheme = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (preferences.getString("app_theme", "default").equals("dark")) {
            setTheme(R.style.AppTheme_Dark_NoActionBar);
            darkTheme = true;
        }
        setContentView(R.layout.activity_home);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        context = this;

        searchField = findViewById(R.id.search_field);
        searchField.setOnClickListener(this);
        findViewById(R.id.search_field_edittext).setOnClickListener(this);

        settingsButton = findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent settingsIntent = new Intent(HomeActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
            }
        });

        if (darkTheme) {
            searchField.setBackground(getResources().getDrawable(R.drawable.search_field_bg_dark));
            findViewById(R.id.home_root).setBackgroundColor(getResources().getColor(R.color.darkThemeColorPrimary));
            ((ImageView)findViewById(R.id.duck_logo)).setImageResource(R.drawable.ic_duckduckgo_white_logo);
            settingsButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_settings_24px_white));
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(HomeActivity.this, SearchActivity.class);
        ActivityOptionsCompat optionsCompat = ActivityOptionsCompat
                .makeSceneTransitionAnimation(HomeActivity.this,
                        searchField, context.getResources().getString(R.string.search_bar_transition_name));
        startActivity(intent, optionsCompat.toBundle());
    }
}
