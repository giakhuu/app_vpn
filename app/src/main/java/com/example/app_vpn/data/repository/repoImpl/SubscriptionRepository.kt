package com.example.app_vpn.data.repository.repoImpl

import android.content.Context
import com.example.app_vpn.data.network.api.SubscriptionApi
import com.example.app_vpn.data.network.safeCall
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import javax.inject.Inject

class SubscriptionRepository @Inject constructor(
    private val subscriptionApi : SubscriptionApi,
    @ApplicationContext private val context: Context,
    private val supabase: SupabaseClient
) {
    suspend fun getAllSubsciption() = safeCall {
        subscriptionApi.getAllSubscription()
    }
}
