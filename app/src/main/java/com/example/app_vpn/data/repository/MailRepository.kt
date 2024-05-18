package com.example.app_vpn.data.repository

import com.example.app_vpn.data.network.SafeApiCall
import com.example.app_vpn.data.network.api.MailApi
import javax.inject.Inject

class MailRepository @Inject constructor(
    private val mailApi: MailApi
) : SafeApiCall  {

    suspend fun sendVerifyCode(email: String) = safeApiCall {
        mailApi.sendVerifyCode(email)
    }

    suspend fun resendCode(email: String) = safeApiCall {
        mailApi.resendCode(email)
    }
}