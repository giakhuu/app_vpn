package com.example.app_vpn.ui

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import com.example.app_vpn.data.preferences.PreferenceManager
import java.util.Locale

open class BaseActivity : AppCompatActivity() {

    private lateinit var preferenceManager: PreferenceManager

    override fun attachBaseContext(newBase: Context) {
        val language = getPreferredLanguage(newBase)
        val context = updateLocale(newBase, language)
        super.attachBaseContext(context)
    }

    private fun updateLocale(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        return context.createConfigurationContext(config)
    }

    private fun getPreferredLanguage(context: Context): String {
        preferenceManager = PreferenceManager(context)
        return preferenceManager.getLanguage() ?: "en"
    }
}
