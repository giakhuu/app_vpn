package com.example.app_vpn.data.repository

import com.example.app_vpn.data.network.SafeApiCall
import com.example.app_vpn.data.preferences.UserPreference
import com.example.app_vpn.data.network.api.UserApi
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val userApi: UserApi
) : SafeApiCall {

    suspend fun fetchData(accessToken : String) = safeApiCall {
        userApi.fetchData(accessToken)
    }

    suspend fun changePassword(
        accessToken: String,
        oldPassword: String,
        newPassword: String
    ) = safeApiCall {
        userApi.changePassword(accessToken, oldPassword, newPassword)
    }

    suspend fun delete(accessToken: String) = safeApiCall {
        userApi.delete(accessToken)
    }
}