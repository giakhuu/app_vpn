package com.example.app_vpn.data.network.api

import com.example.app_vpn.data.entities.PaypalAccessToken
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path


interface PaypalPaymentApi {
    @FormUrlEncoded
    @POST("v1/oauth2/token")
    suspend fun getAccessToken(
        @Header("Authorization") auth: String,
        @Header("Content-Type") type: String = "application/x-www-form-urlencoded",
        @Field("grant_type") grantType: String = "client_credentials"
    ): Response<PaypalAccessToken>

    @POST("v2/checkout/orders")
    suspend fun createOrder(
        @Header("Authorization") auth: String,
        @Header("PayPal-Request-Id") requestId: String,
        @Header("Content-Type") type: String = "application/json",
        @Body orderRequestJson: RequestBody
    ): Response<ResponseBody>

    @POST("v2/checkout/orders/{order_id}/capture")
    suspend fun captureOrder(
        @Path("order_id") orderId: String,
        @Header("Authorization") auth: String,
        @Header("Content-Type") type: String = "application/json"
    ): Response<ResponseBody>
}