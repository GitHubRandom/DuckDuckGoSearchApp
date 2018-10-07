package io.duckduckgosearch.app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsActivity extends AppCompatActivity {

    SharedPreferences preferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (preferences.getString("app_theme", "default").equals("dark")) {
            setTheme(R.style.AppTheme_Dark);
        }
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        androidx.preference.Preference preference;
        SharedPreferences preferences;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.settings_screen);
            preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            preference = findPreference("app_theme");
            preference.setIcon(R.drawable.ic_duckduckgo_logo);
            switch (preferences.getString("app_theme", "default")) {
                case "default":
                    preference.setSummary("Default");
                    preference.setIcon(R.drawable.app_theme_drawable_default);
                    break;
                case "basic":
                    preference.setSummary("Basic");
                    preference.setIcon(R.drawable.app_theme_drawable_basic);
                    break;
                case "gray":
                    preference.setSummary("Gray");
                    preference.setIcon(R.drawable.app_theme_drawable_gray);
                    break;
                case "dark":
                    preference.setSummary("Dark");
                    preference.setIcon(R.drawable.app_theme_drawable_dark);
                    break;
            }
            preference.setOnPreferenceChangeListener(new androidx.preference.Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(androidx.preference.Preference preference, Object newValue) {
                    switch (newValue.toString()) {
                        case "default":
                            preference.setSummary("Default");
                            preference.setIcon(R.drawable.app_theme_drawable_default);
                            break;
                        case "basic":
                            preference.setSummary("Basic");
                            preference.setIcon(R.drawable.app_theme_drawable_basic);
                            break;
                        case "gray":
                            preference.setSummary("Gray");
                            preference.setIcon(R.drawable.app_theme_drawable_gray);
                            break;
                        case "dark":
                            preference.setSummary("Dark");
                            preference.setIcon(R.drawable.app_theme_drawable_dark);
                            break;
                    }
                    return true;
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        super.onNavigateUp();
    }
}
