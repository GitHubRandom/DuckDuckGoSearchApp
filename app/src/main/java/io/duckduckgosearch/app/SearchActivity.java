package io.duckduckgosearch.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

public class SearchActivity extends AppCompatActivity implements WebViewFragment.OnSearchTermChange {

    EditText searchBar;
    FragmentManager fragmentManager;
    ProgressBar progressBar;
    Activity activity;
    ImageView duckLogo;
    ImageButton eraseTextButton;
    RelativeLayout searchBarRoot;
    String latestTerm = "";
    WebViewFragment webViewFragment;
    boolean darkTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ThemeChecker.isDarkTheme(this)) {
            setTheme(R.style.AppTheme_Dark_NoActionBar);
            darkTheme = true;
        }
        setContentView(R.layout.activity_search);

        Bundle bundle = getIntent().getExtras();

        eraseTextButton = findViewById(R.id.erase_button);

        if (darkTheme) {
            findViewById(R.id.frame_layout).setBackgroundColor(getResources().getColor(R.color.darkThemeColorPrimary));
            eraseTextButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_outline_close_24px_white));
        }

        duckLogo = findViewById(R.id.duck_logo);

        fragmentManager = getSupportFragmentManager();

        activity = this;

        progressBar = findViewById(R.id.search_progress);

        searchBarRoot = findViewById(R.id.search_bar_edittext_root);

        searchBar = findViewById(R.id.search_bar_edittext);
        if (savedInstanceState == null) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            searchBar.requestFocus();
        }
        searchBar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (!v.getText().toString().equals("")) {
                    progressBar.setVisibility(View.VISIBLE);
                    webViewFragment = WebViewFragment.newInstance(v.getText().toString());
                    latestTerm = v.getText().toString();
                    fragmentManager.beginTransaction()
                            .replace(R.id.frame_layout, webViewFragment)
                            .commit();
                    HistoryManager.addTerm(latestTerm, "Today", SearchActivity.this);
                }
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                return false;
            }
        });
        searchBar.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    duckLogo.setVisibility(View.GONE);
                } else {
                    duckLogo.setVisibility(View.VISIBLE);
                }
            }
        });

        eraseTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchBar.setText("");
                searchBar.requestFocus();
            }
        });

        if (darkTheme) {
            duckLogo.setImageResource(R.drawable.ic_duckduckgo_white_logo);
            findViewById(R.id.search_bar_root).setBackgroundColor(getResources().getColor(R.color.darkThemeColorPrimary));
            searchBarRoot.setBackground(getResources().getDrawable(R.drawable.search_field_bg_dark));
        }
    }

    @Override
    public void onSearchTermChange(String searchTerm) {
        searchBar.setText(searchTerm);
    }

    @Override
    public void onBackPressed() {
        if (searchBar.hasFocus() && webViewFragment != null) {
            webViewFragment.requestFocusOnWebView();
            searchBar.setText(latestTerm);
        } else {
            super.onBackPressed();
        }
    }
}
