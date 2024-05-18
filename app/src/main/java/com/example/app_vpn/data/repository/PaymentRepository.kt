package com.example.app_vpn.data.repository

import com.example.app_vpn.data.network.SafeApiCall
import com.example.app_vpn.data.network.api.PaymentApi
import javax.inject.Inject

class PaymentRepository @Inject constructor (
    private val paymentApi: PaymentApi,
) : SafeApiCall {

    suspend fun createPayment(
        token : String,
        ipAddress: String,
        amount : String
    ) = safeApiCall {
        paymentApi.createPayment(token, ipAddress, amount)
    }
}