package com.example.app_vpn.data.repository.repoImpl

import android.util.Log
import com.example.app_vpn.data.entities.Payment
import com.example.app_vpn.data.network.api.PaymentApi
import com.example.app_vpn.data.network.safeCall
import javax.inject.Inject

class PaymentRepository @Inject constructor(
    private val paymentApi: PaymentApi,

) {
    suspend fun insertPayment(payment: Payment) = safeCall {
        val response = paymentApi.insertPayment(payment)
        response
    }

}