package com.example.app_vpn.data.network.api

import com.example.app_vpn.data.entities.Subscription
import com.example.app_vpn.data.entities.VpnServer
import retrofit2.Response
import retrofit2.http.GET


interface SubscriptionApi {
    @GET("subscription")
    suspend fun getAllSubscription() : Response<List<Subscription>>
}