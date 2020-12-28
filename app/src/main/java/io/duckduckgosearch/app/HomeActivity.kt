package io.duckduckgosearch.app

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.duckduckgosearch.app.HistoryAdapter.OnLastTermDeleted

class HomeActivity : AppCompatActivity(), View.OnClickListener, OnLastTermDeleted {

    lateinit var searchField: RelativeLayout
    lateinit var fragment: BottomSheetDialogFragment
    var darkTheme = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        if (PrefManager.isDarkTheme(this)) {
            setTheme(R.style.AppTheme_Dark_NoActionBar)
            darkTheme = true
        }
        setContentView(R.layout.activity_home)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true)
        }
        fragment = HistoryFragment()
        val manager = supportFragmentManager
        searchField = findViewById(R.id.search_field)
        searchField.setOnClickListener(this)
        findViewById<View>(R.id.search_field_edittext).setOnClickListener(this)
        val settingsButton = findViewById<ImageButton>(R.id.settings_button)
        settingsButton.setOnClickListener {
            val settingsIntent = Intent(this, SettingsActivity::class.java)
            startActivity(settingsIntent)
        }
        val historyButton = findViewById<LinearLayout>(R.id.search_history_button)
        historyButton.setOnClickListener {
            if (!fragment.isAdded) {
                fragment.show(manager, fragment.tag)
            }
        }
        if (darkTheme) {
            searchField.background = AppCompatResources.getDrawable(this,R.drawable.search_field_bg_dark)
            findViewById<View>(R.id.home_root).setBackgroundColor(ResourcesCompat.getColor(this.resources,R.color.darkThemeColorPrimary,null))
            settingsButton.setImageDrawable(AppCompatResources.getDrawable(this,R.drawable.ic_settings_24px_white))
            (findViewById<View>(R.id.search_history_button_text) as TextView).setTextColor(
                    ResourcesCompat.getColor(this.resources,android.R.color.white,null))
            (findViewById<View>(R.id.search_history_button_icon) as ImageView).setImageDrawable(
                    AppCompatResources.getDrawable(this,R.drawable.ic_outline_keyboard_arrow_up_24px_white))
        }
    }

    override fun onClick(v: View) {
        val intent = Intent(this, SearchActivity::class.java)
        val optionsCompat = ActivityOptionsCompat
                .makeSceneTransitionAnimation(this,
                        searchField, resources.getString(R.string.search_bar_transition_name))
        startActivity(intent, optionsCompat.toBundle())
    }

    override fun onLastTermDeleted() {
        fragment.dismiss()
    }
}