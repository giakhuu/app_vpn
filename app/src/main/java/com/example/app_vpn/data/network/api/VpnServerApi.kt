package com.example.app_vpn.data.network.api

import com.example.app_vpn.data.entities.VpnServer
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url


interface VpnServerApi {
    @GET("vpnServer")
    suspend fun getAllVpnServer() : Response<List<VpnServer>>

    @GET
    suspend fun downloadConfigFile(@Url fileUrl: String): Response<String>
}