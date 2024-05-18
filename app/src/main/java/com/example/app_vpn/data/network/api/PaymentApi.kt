package com.example.app_vpn.data.network.api

import com.example.app_vpn.data.entities.Payment
import com.example.app_vpn.data.repsonses.DataResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST

interface PaymentApi {

    @FormUrlEncoded
    @POST("/payment/create_payment")
    suspend fun createPayment(
        @Header("Authorization") token: String,
        @Field(value = "ipAddress") ipAddress: String,
        @Field(value = "amount") amount : String
    ) : DataResponse<Payment>

}