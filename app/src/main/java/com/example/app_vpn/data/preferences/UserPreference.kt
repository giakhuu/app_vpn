package com.example.app_vpn.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.app_vpn.data.entities.PremiumStatus
import com.example.app_vpn.util.isPremium
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "my_data_store")

@Singleton
class UserPreference @Inject constructor(@ApplicationContext context: Context) {

    private val appContext = context.applicationContext
    private val gson = Gson()

    val premiumExpiredDate: Flow<String?>
        get() = appContext.dataStore.data.map { preferences ->
            preferences[PREMIUM_EXPIRED_DATE]
        }

    suspend fun getPremiumExpiredDate(): String? {
        return appContext.dataStore.data.map { preferences ->
            preferences[PREMIUM_EXPIRED_DATE]
        }.firstOrNull()
    }

    suspend fun getPremiumStatus(): Boolean {
        val expiredDate = getPremiumExpiredDate()
        return expiredDate!= null&& isPremium(expiredDate)
    }

    suspend fun savePremiumStatus(premiumStatus: PremiumStatus) {
        appContext.dataStore.edit { preferences ->
            preferences[PREMIUM_EXPIRED_DATE] = premiumStatus.expiredDate
        }
    }
    suspend fun clear() {
        appContext.dataStore.edit { preferences ->
            preferences.clear()
        }
    }


    companion object {
        private val PREMIUM_EXPIRED_DATE = stringPreferencesKey("premiumExpiredDate")
    }

}