package io.duckduckgosearch.app;

import android.app.Activity;

import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SearchActivity extends AppCompatActivity {

    EditText searchBar;
    FragmentManager fragmentManager;
    ProgressBar progressBar;
    Activity activity;
    ImageView duckLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        duckLogo = findViewById(R.id.duck_logo);

        fragmentManager = getSupportFragmentManager();

        activity = this;

        progressBar = findViewById(R.id.search_progress);

        searchBar = findViewById(R.id.search_bar_edittext);
        if (savedInstanceState == null) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            searchBar.requestFocus();
        }
        searchBar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                progressBar.setVisibility(View.VISIBLE);
                searchBar.clearFocus();
                WebViewFragment webViewFragment = WebViewFragment.newInstance(v.getText().toString());
                fragmentManager.beginTransaction()
                        .replace(R.id.frame_layout, webViewFragment)
                        .commit();
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
    }
}
