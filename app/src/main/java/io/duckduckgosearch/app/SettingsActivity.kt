package io.duckduckgosearch.app

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
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

        private lateinit var themesEntries:Array<String>
        private lateinit var themesValues:Array<String>
        private lateinit var themesIcons:List<Int>

        override fun onAttach(context: Context) {
            super.onAttach(context)
            themesEntries = resources.getStringArray(R.array.settings_theme_entries)
            themesValues = resources.getStringArray(R.array.settings_theme_values)
            themesIcons = listOf(
                    R.drawable.search_theme_drawable_default,
                    R.drawable.search_theme_drawable_basic,
                    R.drawable.search_theme_drawable_gray,
                    R.drawable.search_theme_drawable_dark
            )
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
            setPreferencesFromResource(R.xml.settings_screen, rootKey)
            val historyDatabase = Room.databaseBuilder(requireContext(), HistoryDatabase::class.java, HistoryFragment.HISTORY_DB_NAME)
                    .build()
            val aboutPreference = findPreference<Preference>("about_app")
            if (aboutPreference != null) {
                aboutPreference.intent = Intent(activity, AboutActivity::class.java)
            }
            val themePreference = findPreference<ListPreference>("search_theme")
            val themeIndex = themesValues.indexOf(PrefManager.getTheme(requireContext()))
            themePreference?.summary = themesEntries[themeIndex]
            themePreference?.setIcon(themesIcons[themeIndex])
            themePreference?.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                val newThemeIndex = themesValues.indexOf(PrefManager.getTheme(requireContext()))
                themePreference?.summary = themesEntries[newThemeIndex]
                themePreference?.setIcon(themesIcons[newThemeIndex])
                if (newValue.toString() == "dark" && themePreference?.value != "dark" ||
                        newValue.toString() != "dark" && themePreference?.value == "dark") {
                    val intent = Intent(context, SettingsActivity::class.java)
                    (context as Activity?)!!.finish()
                    startActivity(intent)
                }
                true
            }
            val deleteHistoryPreference = findPreference<Preference>("delete_search_history")
            if (deleteHistoryPreference != null) {
                deleteHistoryPreference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                    val dialog = AlertDialog.Builder(requireContext())
                    dialog.setTitle(requireContext().resources.getString(R.string.settings_delete_search_history_dialog_title))
                    dialog.setMessage(requireContext().resources.getString(R.string.settings_delete_search_history_dialog_message))
                    dialog.setPositiveButton(R.string.settings_delete_search_history_dialog_positive_btn
                    ) { _, _ -> Thread { historyDatabase.clearAllTables() }.start() }
                    dialog.setNegativeButton(R.string.settings_delete_search_history_dialog_negative_btn
                    ) { d, _ -> d.dismiss() }
                    dialog.show()
                    true
                }
            }
            val searchWidgetTheme = findPreference<ListPreference>("search_widget_theme")
            val searchWidgetThemeValue = PrefManager.getSearchWidgetTheme(requireContext())
            searchWidgetTheme?.summary = searchWidgetThemeValue.substring(0, 1).toUpperCase(Locale.getDefault()) + searchWidgetThemeValue.substring(1)
            searchWidgetTheme?.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                val newValueC = newValue.toString().substring(0, 1).toUpperCase(Locale.getDefault()) +
                        newValue.toString().substring(1)
                searchWidgetTheme?.summary = newValueC
                val intent = Intent(requireContext(), SearchWidget::class.java)
                intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                val ids = AppWidgetManager.getInstance(requireContext())
                        .getAppWidgetIds(ComponentName(requireContext(), SearchWidget::class.java))
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                requireContext().sendBroadcast(intent)
                true
            }
            val safeSearch = findPreference<ListPreference>("safe_search")
            val safeSearchValue = PrefManager.getSafeSearchLevel(requireContext())
            safeSearch?.summary = safeSearchValue.substring(0, 1).toUpperCase(Locale.getDefault()) + safeSearchValue.substring(1)
            safeSearch?.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
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