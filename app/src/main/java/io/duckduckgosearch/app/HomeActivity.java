package io.duckduckgosearch.app;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.FragmentManager;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener, HistoryAdapter.OnLastTermDeleted {

    RelativeLayout searchField;
    Context context;
    FragmentManager manager;
    ImageButton settingsButton;
    LinearLayout historyButton;
    BottomSheetDialogFragment fragment;
    boolean darkTheme = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (PrefManager.isDarkTheme(this)) {
            setTheme(R.style.AppTheme_Dark_NoActionBar);
            darkTheme = true;
        }
        setContentView(R.layout.activity_home);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        fragment = new HistoryFragment();
        manager = getSupportFragmentManager();
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

        historyButton = findViewById(R.id.search_history_button);
        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!fragment.isAdded()) {
                    fragment.show(manager, fragment.getTag());
                }
            }
        });

        if (darkTheme) {
            searchField.setBackground(getResources().getDrawable(R.drawable.search_field_bg_dark));
            findViewById(R.id.home_root).setBackgroundColor(getResources().getColor(R.color.darkThemeColorPrimary));
            settingsButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_settings_24px_white));
            ((TextView)findViewById(R.id.search_history_button_text)).setTextColor(
                    getResources().getColor(android.R.color.white));
            ((ImageView)findViewById(R.id.search_history_button_icon)).setImageDrawable(
                    getResources().getDrawable(R.drawable.ic_outline_keyboard_arrow_up_24px_white));
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

    @Override
    public void onLastTermDeleted() {
        fragment.dismiss();
    }
}
