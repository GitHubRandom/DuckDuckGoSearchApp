package io.duckduckgosearch.app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        if (PrefManager.isDarkTheme(this)) {
            setTheme(R.style.AppTheme_Dark);
        }
        setContentView(R.layout.activity_about);

        String versionName = "";
        try {
            versionName = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        ((TextView)findViewById(R.id.version_name_text)).setText(getResources().getString(R.string.settings_about_version, versionName));

        if (PrefManager.isDarkTheme(this)) {
            TextView disclaimerMessage = findViewById(R.id.disclaimer_message);
            disclaimerMessage.setTextColor(getResources().getColor(android.R.color.white));
            TextView disclaimerTitle = findViewById(R.id.disclaimer_title);
            disclaimerTitle.setTextColor(getResources().getColor(R.color.darkThemeColorAccent));
            CardView disclaimerContainer = findViewById(R.id.disclaimer_container);
            disclaimerContainer.setCardBackgroundColor(getResources().getColor(R.color.darkThemeColorSearchFieldBg));
        }
    }
}
