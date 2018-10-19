package io.duckduckgosearch.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PrefManager {

    public static String getTheme(Context context) {
        return getPreferences(context).getString("app_theme", "default");
    }

    public static SharedPreferences getPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static boolean isDarkTheme(Context context) {
        return getTheme(context).equals("dark");
    }

    public static boolean isHistoryEnabled(Context context) {
        return getPreferences(context).getBoolean("search_history", false);
    }

}
