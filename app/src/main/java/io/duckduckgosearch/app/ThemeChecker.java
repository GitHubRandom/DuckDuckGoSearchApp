package io.duckduckgosearch.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class ThemeChecker {

    public static String getTheme(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("app_theme", "default");
    }

    public static boolean isDarkTheme(Context context) {
        return getTheme(context).equals("dark");
    }

}
