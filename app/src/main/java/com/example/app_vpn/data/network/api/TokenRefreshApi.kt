package com.example.app_vpn.data.network.api

import com.example.app_vpn.data.network.api.BaseApi
import com.example.app_vpn.data.repsonses.TokenResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface TokenRefreshApi : BaseApi {
    @FormUrlEncoded
    @POST("auth/refreshToken")
    suspend fun refreshAccessToken(
        @Field("refreshToken") refreshToken: String?
    ): TokenResponse
}