package com.example.app_vpn.data.repository

import com.example.app_vpn.data.network.api.BaseApi
import com.example.app_vpn.data.network.SafeApiCall

abstract class BaseRepository(private val api: BaseApi) : SafeApiCall {
    suspend fun logout() = safeApiCall {
        api.logout()
    }
}