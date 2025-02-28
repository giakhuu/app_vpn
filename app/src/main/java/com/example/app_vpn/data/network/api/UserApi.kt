package com.example.app_vpn.data.network.api

import com.example.app_vpn.data.entities.PremiumStatus
import com.example.app_vpn.util.API_KEY
import com.example.app_vpn.util.SECRET_KEY
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST


interface UserApi {
    @GET("userPremium")
    suspend fun getPremiumUserByUuid(
        @Header("Authorization") accessToken: String,
        @retrofit2.http.Query("uuid") uuid: String
    ): Response<List<PremiumStatus>>
}

interface UserServiceApi {
    @POST("userPremium")
    suspend fun addPremiumUser(
        @Body userPremiumStatus: PremiumStatus,
        @Header("Authorization") accessToken: String = "Bearer $SECRET_KEY"
    ) : Response<String>

    @PATCH("userPremium")
    suspend fun updatePremiumUser(
        @Body userPremiumStatus: PremiumStatus,
        @retrofit2.http.Query("uuid") uuid: String,
        @Header("Authorization") accessToken: String = "Bearer $SECRET_KEY"
    ) : Response<String>

}
