package io.duckduckgosearch.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

class PrefManager {

    static String getTheme(Context context) {
        return getPreferences(context).getString("app_theme", "default");
    }

    static boolean isSearchWidgetDark(Context context) {
        return getPreferences(context).getString("search_widget_theme", "light").equals("dark");
    }

    private static SharedPreferences getPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    static boolean isDarkTheme(Context context) {
        return getTheme(context).equals("dark");
    }

    static boolean isHistoryEnabled(Context context) {
        return getPreferences(context).getBoolean("search_history", true);
    }

}
