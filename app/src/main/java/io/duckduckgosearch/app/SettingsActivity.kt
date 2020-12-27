package io.duckduckgosearch.app

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.room.Room
import java.util.*

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (PrefManager.isDarkTheme(this)) {
            setTheme(R.style.AppTheme_Dark)
        }
        supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, SettingsFragment())
                .commit()
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        var themePreference: ListPreference? = null
        var searchWidgetTheme: ListPreference? = null
        var safeSearch: ListPreference? = null
        var deleteHistoryPreference: Preference? = null
        var aboutPreference: Preference? = null
        var historyDatabase: HistoryDatabase? = null

        override fun onCreatePreferences(savedInstanceState: Bundle, rootKey: String) {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
            setPreferencesFromResource(R.xml.settings_screen, rootKey)
            historyDatabase = Room.databaseBuilder(requireContext(), HistoryDatabase::class.java, HistoryFragment.HISTORY_DB_NAME)
                    .build()
            aboutPreference = findPreference("about_app")
            if (aboutPreference != null) {
                aboutPreference!!.intent = Intent(activity, AboutActivity::class.java)
            }
            themePreference = findPreference("search_theme")
            when (PrefManager.getTheme(context)) {
                "default" -> {
                    themePreference!!.summary = "Default"
                    themePreference!!.setIcon(R.drawable.search_theme_drawable_default)
                }
                "basic" -> {
                    themePreference!!.summary = "Basic"
                    themePreference!!.setIcon(R.drawable.search_theme_drawable_basic)
                }
                "gray" -> {
                    themePreference!!.summary = "Gray"
                    themePreference!!.setIcon(R.drawable.search_theme_drawable_gray)
                }
                "dark" -> {
                    themePreference!!.summary = "Dark"
                    themePreference!!.setIcon(R.drawable.search_theme_drawable_dark)
                }
            }
            themePreference!!.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
                when (newValue.toString()) {
                    "default" -> {
                        preference.summary = "Default"
                        preference.setIcon(R.drawable.search_theme_drawable_default)
                    }
                    "basic" -> {
                        preference.summary = "Basic"
                        preference.setIcon(R.drawable.search_theme_drawable_basic)
                    }
                    "gray" -> {
                        preference.summary = "Gray"
                        preference.setIcon(R.drawable.search_theme_drawable_gray)
                    }
                    "dark" -> {
                        preference.summary = "Dark"
                        preference.setIcon(R.drawable.search_theme_drawable_dark)
                    }
                }
                if (newValue.toString() == "dark" && themePreference!!.value != "dark" ||
                        newValue.toString() != "dark" && themePreference!!.value == "dark") {
                    val intent = Intent(context, SettingsActivity::class.java)
                    (context as Activity?)!!.finish()
                    startActivity(intent)
                }
                true
            }
            deleteHistoryPreference = findPreference("delete_search_history")
            if (deleteHistoryPreference != null) {
                deleteHistoryPreference!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                    val dialog = AlertDialog.Builder(requireContext())
                    dialog.setTitle(requireContext().resources.getString(R.string.settings_delete_search_history_dialog_title))
                    dialog.setMessage(requireContext().resources.getString(R.string.settings_delete_search_history_dialog_message))
                    dialog.setPositiveButton(R.string.settings_delete_search_history_dialog_positive_btn
                    ) { _, _ -> Thread { historyDatabase!!.clearAllTables() }.start() }
                    dialog.setNegativeButton(R.string.settings_delete_search_history_dialog_negative_btn
                    ) { dialog, _ -> dialog.dismiss() }
                    dialog.show()
                    true
                }
            }
            searchWidgetTheme = findPreference("search_widget_theme")
            val searchWidgetThemeValue = PrefManager.getSearchWidgetTheme(context)
            searchWidgetTheme!!.summary = searchWidgetThemeValue.substring(0, 1).toUpperCase(Locale.getDefault()) + searchWidgetThemeValue.substring(1)
            searchWidgetTheme!!.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                val newValueC = newValue.toString().substring(0, 1).toUpperCase(Locale.getDefault()) +
                        newValue.toString().substring(1)
                searchWidgetTheme!!.summary = newValueC
                val intent = Intent(requireContext(), SearchWidget::class.java)
                intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                val ids = AppWidgetManager.getInstance(requireContext())
                        .getAppWidgetIds(ComponentName(requireContext(), SearchWidget::class.java))
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                requireContext().sendBroadcast(intent)
                true
            }
            safeSearch = findPreference("safe_search")
            val safeSearchValue = PrefManager.getSafeSearchLevel(context)
            safeSearch!!.summary = safeSearchValue.substring(0, 1).toUpperCase(Locale.getDefault()) + safeSearchValue.substring(1)
            safeSearch!!.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                val newValueC = newValue.toString().substring(0, 1).toUpperCase(Locale.getDefault()) +
                        newValue.toString().substring(1)
                safeSearch!!.summary = newValueC
                true
            }
        }
    }

    override fun onBackPressed() {
        super.onNavigateUp()
    }
}