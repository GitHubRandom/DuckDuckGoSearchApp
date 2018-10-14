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
    String intentSearchTerm;
    WebViewFragment webViewFragment;
    boolean darkTheme;
    boolean fromIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ThemeChecker.isDarkTheme(this)) {
            setTheme(R.style.AppTheme_Dark_NoActionBar);
            darkTheme = true;
        }
        setContentView(R.layout.activity_search);

        duckLogo = findViewById(R.id.duck_logo);

        fragmentManager = getSupportFragmentManager();

        activity = this;

        searchBarRoot = findViewById(R.id.search_bar_edittext_root);

        progressBar = findViewById(R.id.search_progress);

        searchBar = findViewById(R.id.search_bar_edittext);
        
        eraseTextButton = findViewById(R.id.erase_button);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (bundle.containsKey("search_term")) {
                intentSearchTerm = bundle.getString("search_term");
                fromIntent = true;
                search(intentSearchTerm);
            }
        }

        if (darkTheme) {
            findViewById(R.id.frame_layout).setBackgroundColor(getResources().getColor(R.color.darkThemeColorPrimary));
            eraseTextButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_outline_close_24px_white));
        }

        if (savedInstanceState == null && !fromIntent) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            searchBar.requestFocus();
        }

        searchBar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (!v.getText().toString().equals("")) {
                    search(v.getText().toString());
                }
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

    void search(String searchTerm) {
        progressBar.setVisibility(View.VISIBLE);
        webViewFragment = WebViewFragment.newInstance(searchTerm);
        latestTerm = searchTerm;
        if (fromIntent) {
            searchBar.setText(latestTerm);
        }
        fragmentManager.beginTransaction()
                .replace(R.id.frame_layout, webViewFragment)
                .commit();
        HistoryManager.addTerm(latestTerm, "Today", SearchActivity.this);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }
}
