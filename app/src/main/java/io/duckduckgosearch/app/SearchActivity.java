package io.duckduckgosearch.app;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SearchActivity extends AppCompatActivity {

    EditText searchBar;
    FragmentManager fragmentManager;
    ProgressBar progressBar;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        fragmentManager = getSupportFragmentManager();

        context = this;

        progressBar = findViewById(R.id.search_progress);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        searchBar = findViewById(R.id.search_bar_edittext);
        searchBar.requestFocus();
        searchBar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                progressBar.setVisibility(View.VISIBLE);
                searchBar.clearFocus();
                WebViewFragment webViewFragment = WebViewFragment.newInstance(v.getText().toString());
                fragmentManager.beginTransaction()
                        .replace(R.id.frame_layout, webViewFragment)
                        .commit();
                return false;
            }
        });
    }
}
