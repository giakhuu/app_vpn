package com.example.app_vpn.data.network.api

import com.example.app_vpn.data.repsonses.OtherResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface MailApi {

    @FormUrlEncoded
    @POST("/mail/sendVerifyCode")
    suspend fun sendVerifyCode(
        @Field(value = "email") email : String
    ) : OtherResponse

    @FormUrlEncoded
    @POST("/mail/resendCode")
    suspend fun resendCode(
        @Field(value = "email") email : String
    ) : OtherResponse
}