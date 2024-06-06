package com.example.app_vpn.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.app_vpn.data.entities.Country
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceManager @Inject constructor(@ApplicationContext context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = sharedPreferences.edit()
    private val gson = Gson()

    fun saveCountry(country: Country) {
        val json = gson.toJson(country)
        editor.putString("country", json)
        editor.apply()
    }

    fun getCountry(): Country? {
        val json = sharedPreferences.getString("country", null) ?: return null
        return gson.fromJson(json, Country::class.java)
    }

    fun saveLanguage(lang: String) {
        editor.putString("language", lang)
        editor.apply()
    }

    fun getLanguage(): String? {
        return sharedPreferences.getString("language", null)
    }

    fun saveStatus(status: String) {
        editor.putString("status", status)
        editor.apply()
    }

    fun getStatus(): String? {
        return sharedPreferences.getString("status", null)
    }
}
