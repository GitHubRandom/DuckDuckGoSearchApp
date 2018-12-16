package io.duckduckgosearch.app;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.room.Room;

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

        ListPreference themePreference, searchWidgetTheme, safeSearch;
        Preference deleteHistoryPreference;
        Preference aboutPreference;
        HistoryDatabase historyDatabase;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.settings_screen);
            historyDatabase = Room.databaseBuilder(getContext(), HistoryDatabase.class, HistoryFragment.HISTORY_DB_NAME)
                    .build();
            aboutPreference = findPreference("about_app");
            aboutPreference.setIntent(new Intent(getActivity(), AboutActivity.class));

            themePreference = (ListPreference) findPreference("search_theme");
            switch (PrefManager.getTheme(getContext())) {
                case "default":
                    themePreference.setSummary("Default");
                    themePreference.setIcon(R.drawable.search_theme_drawable_default);
                    break;
                case "basic":
                    themePreference.setSummary("Basic");
                    themePreference.setIcon(R.drawable.search_theme_drawable_basic);
                    break;
                case "gray":
                    themePreference.setSummary("Gray");
                    themePreference.setIcon(R.drawable.search_theme_drawable_gray);
                    break;
                case "dark":
                    themePreference.setSummary("Dark");
                    themePreference.setIcon(R.drawable.search_theme_drawable_dark);
                    break;
            }
            themePreference.setOnPreferenceChangeListener(new androidx.preference.Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(androidx.preference.Preference preference, Object newValue) {
                    switch (newValue.toString()) {
                        case "default":
                            preference.setSummary("Default");
                            preference.setIcon(R.drawable.search_theme_drawable_default);
                            break;
                        case "basic":
                            preference.setSummary("Basic");
                            preference.setIcon(R.drawable.search_theme_drawable_basic);
                            break;
                        case "gray":
                            preference.setSummary("Gray");
                            preference.setIcon(R.drawable.search_theme_drawable_gray);
                            break;
                        case "dark":
                            preference.setSummary("Dark");
                            preference.setIcon(R.drawable.search_theme_drawable_dark);
                            break;
                    }
                    if ((newValue.toString().equals("dark") && !themePreference.getValue().equals("dark")) ||
                            (!newValue.toString().equals("dark") && themePreference.getValue().equals("dark"))) {
                        Intent intent = new Intent(getContext(), SettingsActivity.class);
                        getActivity().finish();
                        startActivity(intent);
                    }
                    return true;
                }
            });

            deleteHistoryPreference = findPreference("delete_search_history");
            deleteHistoryPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                    dialog.setTitle(getContext().getResources().getString(R.string.settings_delete_search_history_dialog_title));
                    dialog.setMessage(getContext().getResources().getString(R.string.settings_delete_search_history_dialog_message));
                    dialog.setPositiveButton(R.string.settings_delete_search_history_dialog_positive_btn,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            historyDatabase.clearAllTables();
                                        }
                                    }).start();
                                }
                    });
                    dialog.setNegativeButton(R.string.settings_delete_search_history_dialog_negative_btn,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    dialog.show();
                    return true;
                }
            });

            searchWidgetTheme = (ListPreference) findPreference("search_widget_theme");
            String searchWidgetThemeValue = PrefManager.getSearchWidgetTheme(getContext());
            searchWidgetTheme.setSummary(searchWidgetThemeValue.substring(0,1).toUpperCase() + searchWidgetThemeValue.substring(1));
            searchWidgetTheme.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String newValueC = newValue.toString().substring(0, 1).toUpperCase() +
                            newValue.toString().substring(1);
                    searchWidgetTheme.setSummary(newValueC);
                    Intent intent = new Intent(getContext(), SearchWidget.class);
                    intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                    int[] ids = AppWidgetManager.getInstance(getContext())
                            .getAppWidgetIds(new ComponentName(getContext(), SearchWidget.class));
                    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
                    getContext().sendBroadcast(intent);
                    return true;
                }
            });

            safeSearch = (ListPreference) findPreference("safe_search");
            String safeSearchValue = PrefManager.getSafeSearchLevel(getContext());
            safeSearch.setSummary(safeSearchValue.substring(0,1).toUpperCase() + safeSearchValue.substring(1));
            safeSearch.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String newValueC = newValue.toString().substring(0, 1).toUpperCase() +
                            newValue.toString().substring(1);
                    safeSearch.setSummary(newValueC);
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
