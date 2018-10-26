package io.duckduckgosearch.app;

import android.os.Bundle;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (PrefManager.isDarkTheme(this)) {
            setTheme(R.style.AppTheme_Dark);
        }
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        androidx.preference.Preference themePreference;
        androidx.preference.Preference deleteHistoryPreference;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.settings_screen);
            themePreference = findPreference("app_theme");
            switch (PrefManager.getTheme(getContext())) {
                case "default":
                    themePreference.setSummary("Default");
                    themePreference.setIcon(R.drawable.app_theme_drawable_default);
                    break;
                case "basic":
                    themePreference.setSummary("Basic");
                    themePreference.setIcon(R.drawable.app_theme_drawable_basic);
                    break;
                case "gray":
                    themePreference.setSummary("Gray");
                    themePreference.setIcon(R.drawable.app_theme_drawable_gray);
                    break;
                case "dark":
                    themePreference.setSummary("Dark");
                    themePreference.setIcon(R.drawable.app_theme_drawable_dark);
                    break;
            }
            themePreference.setOnPreferenceChangeListener(new androidx.preference.Preference.OnPreferenceChangeListener() {
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

            deleteHistoryPreference = findPreference("delete_search_history");
            deleteHistoryPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    boolean delete = HistoryManager.deleteSearchHistory(getContext());
                    if (delete) {
                        Toast.makeText(getContext(),
                                getResources().getString(R.string.settings_delete_search_history_success),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(),
                                getResources().getString(R.string.settings_delete_search_history_already_done),
                                Toast.LENGTH_SHORT).show();
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
