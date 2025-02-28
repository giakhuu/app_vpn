package com.example.app_vpn.data.network.api

import com.example.app_vpn.data.entities.Payment
import com.example.app_vpn.util.SECRET_KEY
import com.google.android.gms.common.api.internal.ApiKey
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface PaymentApi {
    @POST("payment")
    suspend fun insertPayment(
        @Body body: Payment,
        @Header("Authorization") token: String = "Bearer $SECRET_KEY",
    ): Response<String>
}