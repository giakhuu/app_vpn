package com.example.app_vpn.data.network.api

import com.example.app_vpn.data.entities.User
import com.example.app_vpn.data.repsonses.DataResponse
import com.example.app_vpn.data.repsonses.OtherResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT

interface UserApi {
    @GET("user/detail")
    suspend fun fetchData(
        @Header("Authorization") accessToken: String
    ): DataResponse<User>

    @FormUrlEncoded
    @POST("user/changepw")
    suspend fun changePassword(
        @Header("Authorization") accessToken: String,
        @Field(value = "oldPassword") oldPassword: String,
        @Field(value = "newPassword") newPassword: String,
    ): DataResponse<User>

    suspend fun delete(
        @Header("Authorization") accessToken: String
    ) : OtherResponse

    @FormUrlEncoded
    @PUT("user/resetPassword")
    suspend fun resetPassword(
        @Field(value = "email") email: String,
        @Field(value = "password") password: String
    ): OtherResponse
}