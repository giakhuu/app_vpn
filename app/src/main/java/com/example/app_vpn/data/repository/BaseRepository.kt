package com.example.app_vpn.data.repository

import com.example.app_vpn.data.network.SafeApiCall
import com.example.app_vpn.data.network.api.BaseApi

abstract class BaseRepository(private val api: BaseApi) : SafeApiCall {
    suspend fun logout() = safeApiCall {
        api.logout()
    }
}