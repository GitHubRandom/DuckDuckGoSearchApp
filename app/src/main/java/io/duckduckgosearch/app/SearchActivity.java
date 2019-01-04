package io.duckduckgosearch.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebViewClient;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.room.Room;

public class SearchActivity extends AppCompatActivity implements WebViewFragment.OnSearchTermChange,
        AutoCompleteAdapter.OnItemClickListener, WebViewFragment.OnWebViewError, ErrorFragment.OnReloadButtonClick,
        WebViewFragment.OnPageFinish {

    AutoCompleteTextView searchBar;
    FragmentManager fragmentManager;
    ProgressBar progressBar;
    Activity activity;
    ImageView duckLogo;
    ImageButton eraseTextButton;
    RelativeLayout searchBarRoot;
    String latestTerm = "";
    String intentSearchTerm;
    WebViewFragment webViewFragment;
    AutoCompleteAdapter adapter;
    InputMethodManager manager;
    HistoryDatabase historyDatabase;
    boolean darkTheme;
    boolean fromIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (PrefManager.isDarkTheme(this)) {
            setTheme(R.style.AppTheme_Dark_NoActionBar);
            darkTheme = true;
        }
        setContentView(R.layout.activity_search);

        historyDatabase = Room.databaseBuilder(this, HistoryDatabase.class, HistoryFragment.HISTORY_DB_NAME)
                .build();

        manager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        duckLogo = findViewById(R.id.duck_logo);

        fragmentManager = getSupportFragmentManager();

        activity = this;

        searchBarRoot = findViewById(R.id.search_bar_edittext_root);

        progressBar = findViewById(R.id.search_progress);

        searchBar = findViewById(R.id.search_bar_edittext);

        adapterUpdate();

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
            findViewById(R.id.frame_layout).setBackgroundColor(
                    getResources().getColor(R.color.darkThemeColorPrimary));
            eraseTextButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_outline_close_24px_white));
        }

        if (savedInstanceState == null && !fromIntent) {
            searchBar.requestFocus();
        } else {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
            if (!searchBar.hasFocus()) {
                duckLogo.setVisibility(View.VISIBLE);
            }
        }

        searchBar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (!v.getText().toString().equals("") && actionId == EditorInfo.IME_ACTION_DONE) {
                    search(v.getText().toString());
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                    return true;
                }
                return false;
            }
        });
        searchBar.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    duckLogo.setVisibility(View.GONE);
                    if (getResources().getBoolean(R.bool.isTabletAndLandscape)) {
                        searchBarRoot.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT));
                    }
                    if (webViewFragment != null) {
                        fragmentManager.beginTransaction()
                                .hide(webViewFragment)
                                .commit();
                    }
                } else {
                    duckLogo.setVisibility(View.VISIBLE);
                    if (getResources().getBoolean(R.bool.isTabletAndLandscape)) {
                        searchBarRoot.setLayoutParams(new LinearLayout.LayoutParams(550, ViewGroup.LayoutParams.WRAP_CONTENT));
                    }
                    if (webViewFragment != null) {
                        fragmentManager.beginTransaction()
                                .show(webViewFragment)
                                .commit();
                    }
                }
            }
        });

        eraseTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchBar.setText("");
                searchBar.requestFocus();
                manager.showSoftInput(searchBar, 0);
            }
        });

        if (darkTheme) {
            findViewById(R.id.search_bar_root).setBackgroundColor(
                    getResources().getColor(R.color.darkThemeColorPrimary));
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

    void adapterUpdate() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<HistoryItem> historyArrayList = (ArrayList<HistoryItem>) historyDatabase.historyDao().getAllSearchHistory();
                Collections.reverse(historyArrayList);
                ArrayList<String> historyArrayListStrings = new ArrayList<>();
                for (HistoryItem item : historyArrayList) {
                    historyArrayListStrings.add(item.getSearchTerm());
                }
                String[] historyArray = Arrays.copyOf(historyArrayListStrings.toArray(), historyArrayList.size(), String[].class);
                adapter = new AutoCompleteAdapter(SearchActivity.this, R.layout.auto_complete_item
                        , historyArray, searchBar);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        searchBar.setAdapter(adapter);
                    }
                });
            }
        }).start();
    }

    void search(String searchTerm) {
        progressBar.setVisibility(View.VISIBLE);
        webViewFragment = WebViewFragment.newInstance(searchTerm, PrefManager.isHistoryEnabled(this));
        latestTerm = searchTerm;
        if (fromIntent) {
            searchBar.setText(latestTerm);
        }
        fragmentManager.beginTransaction()
                .replace(R.id.frame_layout, webViewFragment)
                .commit();
        searchBar.clearFocus();
        searchBar.setSelection(0);
        manager.hideSoftInputFromWindow(searchBar.getWindowToken(), 0);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    @Override
    public void onItemClickListener(String searchTerm) {
        search(searchTerm);
    }


    @Override
    public void onWebViewError(int errorCode) {
        if (errorCode == WebViewClient.ERROR_HOST_LOOKUP || errorCode == WebViewClient.ERROR_TIMEOUT || errorCode == WebViewClient.ERROR_CONNECT) {
            progressBar.setVisibility(View.GONE);
            fragmentManager.beginTransaction()
                    .replace(R.id.frame_layout, new ErrorFragment())
                    .commit();
        }
    }

    @Override
    public void onReloadButtonClick() {
        search(latestTerm);
    }

    @Override
    public void onPageFinish() {
        adapterUpdate();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}
