package com.example.app_vpn.data.repository

import com.example.app_vpn.data.network.SafeApiCall
import com.example.app_vpn.data.network.api.AuthApi
import com.example.app_vpn.data.preferences.UserPreference
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val api: AuthApi,
    private val preferences: UserPreference
) : SafeApiCall {
    suspend fun login(
        username: String,
        password: String
    )  = safeApiCall {
        api.login(username, password)
    }

    suspend fun register(
        username: String,
        email: String,
        password: String,
        role: String = "user"
    ) = safeApiCall {
        api.register(username, email, password, role)
    }

    suspend fun verify(
        email : String,
        code : String
    ) = safeApiCall {
        api.verify(email, code)
    }

    suspend fun isUsernameEmailExist(
        username: String,
        email: String
    ) = safeApiCall {
        api.isUsernameEmailExist(username, email)
    }

    suspend fun isEmailExist(
        email: String
    ) = safeApiCall {
        api.isEmailExist(email)
    }

    suspend fun saveAccessTokens(accessToken: String, refreshToken: String) {
        preferences.saveAccessTokens(accessToken, refreshToken)
    }

    suspend fun savePremiumKey(premiumKey: String) {
        preferences.savePremiumKey(premiumKey)
    }
}