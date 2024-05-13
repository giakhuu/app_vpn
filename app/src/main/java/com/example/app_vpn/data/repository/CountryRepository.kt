package com.example.app_vpn.data.repository

import com.example.app_vpn.data.network.SafeApiCall
import com.example.app_vpn.data.network.api.CountryApi
import javax.inject.Inject

class CountryRepository @Inject constructor(
    private val countryApi : CountryApi
) : SafeApiCall {
    suspend fun getAllCountry() = safeApiCall {
        countryApi.getAllCountry()
    }
}