package io.duckduckgosearch.app

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

internal object PrefManager {
    fun getTheme(context: Context): String {
        return getPreferences(context).getString("search_theme", "default") ?: "default"
    }

    fun getSearchWidgetTheme(context: Context): String {
        return getPreferences(context).getString("search_widget_theme", "light") ?: "light"
    }

    fun getSafeSearchLevel(context: Context): String {
        return getPreferences(context).getString("safe_search", "moderate") ?: "moderate"
    }

    fun isSearchWidgetDark(context: Context): Boolean {
        return getPreferences(context).getString("search_widget_theme", "light") == "dark"
    }

    private fun getPreferences(context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }

    fun isDarkTheme(context: Context): Boolean {
        return getTheme(context) == "dark"
    }

    fun isHistoryEnabled(context: Context): Boolean {
        return getPreferences(context).getBoolean("search_history", true)
    }
}