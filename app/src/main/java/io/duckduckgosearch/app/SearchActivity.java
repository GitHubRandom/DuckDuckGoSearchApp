package io.duckduckgosearch.app;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class SearchActivity extends AppCompatActivity implements SearchTask.OnTaskFinish {

    EditText searchBar;
    FrameLayout frameLayout;
    ProgressBar progressBar;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        context = this;

        progressBar = findViewById(R.id.search_progress);

        searchBar = findViewById(R.id.search_bar_edittext);
        searchBar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                progressBar.setVisibility(View.VISIBLE);
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    SearchTask searchTask = new SearchTask(v.getText().toString(), context);
                    searchTask.execute();
                }
                return false;
            }
        });
    }

    @Override
    public void onTaskFinish(String finalHtmlCode) {
        Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
    }
}
