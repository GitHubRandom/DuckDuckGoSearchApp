package io.duckduckgosearch.app

import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityOptionsCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.duckduckgosearch.app.HistoryAdapter.OnLastTermDeleted

class HomeActivity : AppCompatActivity(), View.OnClickListener, OnLastTermDeleted {

    lateinit var searchField: RelativeLayout
    lateinit var fragment: BottomSheetDialogFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        AppCompatDelegate.setDefaultNightMode(PrefManager.getThemeInt(this))
        Log.i("Theme Debug",PrefManager.getThemeInt(this).toString())
        setContentView(R.layout.activity_home)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true)
        }
        this.transparentStatusBar()
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
    }

    override fun onClick(v: View) {
        val intent = Intent(this, SearchActivity::class.java)
        val optionsCompat = ActivityOptionsCompat
                .makeSceneTransitionAnimation(this,
                        searchField, resources.getString(R.string.search_bar_transition_name))
        startActivity(intent, optionsCompat.toBundle())
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        finish()
        startActivity(intent)
    }

    override fun onLastTermDeleted() {
        fragment.dismiss()
    }
}