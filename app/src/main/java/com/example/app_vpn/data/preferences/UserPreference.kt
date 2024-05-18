package com.example.app_vpn.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "my_data_store")

@Singleton
class UserPreference @Inject constructor(@ApplicationContext context: Context) {

    private val appContext = context.applicationContext

    val accessToken: Flow<String?>
        get() = appContext.dataStore.data.map { preferences ->
            preferences[ACCESS_TOKEN]
        }

    val refreshToken: Flow<String?>
        get() = appContext.dataStore.data.map { preferences ->
            preferences[REFRESH_TOKEN]
        }

    val premiumKey: Flow<String?>
        get() = appContext.dataStore.data.map { preferences ->
            preferences[PREMIUM_KEY]
        }

    suspend fun saveAccessTokens(accessToken: String, refreshToken: String) {
        appContext.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN] = accessToken
            preferences[REFRESH_TOKEN] = refreshToken
        }
    }

    suspend fun savePremiumKey(premiumKey: String) {
        appContext.dataStore.edit { preferences ->
            preferences[PREMIUM_KEY] = premiumKey
        }
    }

    suspend fun clear() {
        appContext.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    suspend fun getAccessTokenAsString(): String? {
        return appContext.dataStore.data.map { preferences ->
            preferences[ACCESS_TOKEN]
        }.firstOrNull()
    }

    companion object {
        private val ACCESS_TOKEN = stringPreferencesKey("accessToken")
        private val REFRESH_TOKEN = stringPreferencesKey("refreshToken")
        private val PREMIUM_KEY = stringPreferencesKey("premiumKey")
    }

}