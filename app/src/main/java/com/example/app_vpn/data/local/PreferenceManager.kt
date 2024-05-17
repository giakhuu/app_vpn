package com.example.app_vpn.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.app_vpn.data.repsonses.Country
import com.google.gson.Gson

class PreferenceManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = sharedPreferences.edit()
    private val gson = Gson()

    fun saveCountry(country: com.example.app_vpn.data.repsonses.Country) {
        val json = gson.toJson(country)
        editor.putString("country", json)
        editor.apply()
    }

    fun getCountry(): Country? {
        val json = sharedPreferences.getString("country", null) ?: return null
        return gson.fromJson(json, Country::class.java)
    }
}
