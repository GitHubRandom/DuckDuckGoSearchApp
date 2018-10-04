package io.duckduckgosearch.app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
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
            preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            preference = findPreference("app_theme");
            switch (preferences.getString("app_theme", "default")) {
                case "default":
                    preference.setSummary("Default");
                    break;
                case "basic":
                    preference.setSummary("Basic");
                    break;
                case "gray":
                    preference.setSummary("Gray");
                    break;
                case "dark":
                    preference.setSummary("Dark");
                    break;
            }
            preference.setOnPreferenceChangeListener(new androidx.preference.Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(androidx.preference.Preference preference, Object newValue) {
                    switch (newValue.toString()) {
                        case "default":
                            preference.setSummary("Default");
                            break;
                        case "basic":
                            preference.setSummary("Basic");
                            break;
                        case "gray":
                            preference.setSummary("Gray");
                            break;
                        case "dark":
                            preference.setSummary("Dark");
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
