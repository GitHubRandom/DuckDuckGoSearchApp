package io.duckduckgosearch.app

import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        AppCompatDelegate.setDefaultNightMode(PrefManager.getThemeInt(this))
        this.transparentStatusBar()
        setContentView(R.layout.activity_about)
        var versionName = ""
        try {
            versionName = this.packageManager.getPackageInfo(this.packageName, 0).versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        findViewById<TextView>(R.id.version_name_text).text = resources.getString(R.string.settings_about_version, versionName)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        finish()
        startActivity(intent)
    }
}