package com.example.app_vpn.data.network.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET

interface IpifyApi {

    @GET("/")
    suspend fun getPublicIp(): Response<ResponseBody>
}