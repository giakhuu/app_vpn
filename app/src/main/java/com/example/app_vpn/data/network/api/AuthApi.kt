package com.example.app_vpn.data.network.api

import com.example.app_vpn.data.entities.Token
import com.example.app_vpn.data.entities.User
import com.example.app_vpn.data.repsonses.DataResponse
import com.example.app_vpn.data.repsonses.OtherResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface AuthApi {

    @FormUrlEncoded
    @POST("/auth/login")
    suspend fun login(
        @Field(value = "username") username: String,
        @Field(value = "password") password: String
    ) : DataResponse<Token>

    @FormUrlEncoded
    @POST("auth/register")
    suspend fun register(
        @Field(value = "username") username: String,
        @Field(value = "email") email: String,
        @Field(value = "password") password: String,
        @Field(value = "role") role: String,
    ) : DataResponse<User>

    @FormUrlEncoded
    @POST("auth/verify")
    suspend fun verify(
        @Field(value = "email") email: String,
        @Field(value = "code") code : String
    ) : OtherResponse

    @FormUrlEncoded
    @POST("auth/isValidUsernameEmail")
    suspend fun isValidUsernameEmail(
        @Field(value = "username") username: String,
        @Field(value = "email") email: String
    ) : OtherResponse
}