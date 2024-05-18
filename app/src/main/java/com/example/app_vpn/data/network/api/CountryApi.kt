package com.example.app_vpn.data.network.api

import com.example.app_vpn.data.entities.Country
import com.example.app_vpn.data.repsonses.DataResponse
import retrofit2.http.GET


interface CountryApi {
    @GET("/country/all")
    suspend fun getAllCountry() : DataResponse<List<Country>>
}