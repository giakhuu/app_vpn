package com.example.app_vpn.data.network.api

import androidx.lifecycle.LiveData
import com.example.app_vpn.data.repsonses.Country
import com.example.app_vpn.data.repsonses.DataRespones
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET


interface CountryApi : BaseApi {
//    @FormUrlEncoded
    @GET("/country/all")
    suspend fun getAllCountry() : DataRespones<List<Country>>
}