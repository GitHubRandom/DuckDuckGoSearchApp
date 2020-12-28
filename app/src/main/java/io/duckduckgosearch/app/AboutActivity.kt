package io.duckduckgosearch.app

import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        if (PrefManager.isDarkTheme(this)) {
            setTheme(R.style.AppTheme_Dark)
        }
        setContentView(R.layout.activity_about)
        var versionName: String? = ""
        try {
            versionName = this.packageManager.getPackageInfo(this.packageName, 0).versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        findViewById<TextView>(R.id.version_name_text).text = resources.getString(R.string.settings_about_version, versionName)
        if (PrefManager.isDarkTheme(this)) {
            val disclaimerMessage = findViewById<TextView>(R.id.disclaimer_message)
            disclaimerMessage.setTextColor(ResourcesCompat.getColor(resources,android.R.color.white,null))
            val disclaimerTitle = findViewById<TextView>(R.id.disclaimer_title)
            disclaimerTitle.setTextColor(ResourcesCompat.getColor(resources,R.color.darkThemeColorAccent,null))
            val disclaimerContainer = findViewById<CardView>(R.id.disclaimer_container)
            disclaimerContainer.setCardBackgroundColor(ResourcesCompat.getColor(resources,R.color.darkThemeColorSearchFieldBg,null))
        }
    }
}