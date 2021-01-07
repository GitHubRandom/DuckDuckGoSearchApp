package io.duckduckgosearch.app

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager

internal object PrefManager {
    fun getTheme(context: Context): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            getPreferences(context).getString("search_theme", "follow") ?: "follow"
        } else {
            getPreferences(context).getString("search_theme", "default") ?: "default"
        }
    }

    fun getSearchTheme(context: Context): String {
        return if (getTheme(context) == "follow" || getTheme(context) == "battery") {
            when (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_YES -> "dark"
                else -> "default"
            }
        } else {
            getTheme(context)
        }
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

    fun getThemeInt(context: Context): Int {
        return when (getTheme(context)) {
            "dark" -> AppCompatDelegate.MODE_NIGHT_YES
            "follow" -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            "battery" -> AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
            else -> AppCompatDelegate.MODE_NIGHT_NO
        }
    }
}