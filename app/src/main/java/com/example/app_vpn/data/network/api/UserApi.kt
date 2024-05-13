package com.example.app_vpn.data.network.api

import com.example.app_vpn.data.repsonses.DataResponse
import com.example.app_vpn.data.repsonses.OtherResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface UserApi : BaseApi {
    @GET("user/detail")
    suspend fun fetchData(
        @Header("Authorization") accessToken: String
    ): DataResponse

    @FormUrlEncoded
    @POST("user/changepw")
    suspend fun changePassword(
        @Header("Authorization") accessToken: String,
        @Field(value = "oldPassword") oldPassword: String,
        @Field(value = "newPassword") newPassword: String,
    ): DataResponse

    suspend fun delete(
        @Header("Authorization") accessToken: String
    ) : OtherResponse
}