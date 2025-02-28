package com.example.app_vpn.data.repository.repoImpl

import com.example.app_vpn.data.network.api.PaypalPaymentApi
import com.example.app_vpn.data.network.safeCall
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import javax.inject.Inject

class PaypalPaymentRepository @Inject constructor (
    private val paypalPaymentApi: PaypalPaymentApi,
)  {
    suspend fun getAccessToken(
        authString: String,
    ) = safeCall {
        paypalPaymentApi.getAccessToken("Basic $authString")
    }

    suspend fun createOrder(
        accessToken: String,
        uniqueId: String,
        orderRequestJson: JSONObject
    ) = safeCall {
        val requestBody = orderRequestJson.toString()
            .toRequestBody("application/json".toMediaTypeOrNull())
        paypalPaymentApi.createOrder("Bearer $accessToken", uniqueId, "application/json", requestBody)
    }

    suspend fun captureOrder(orderId: String, accessToken: String) = safeCall {
            paypalPaymentApi.captureOrder(orderId, "Bearer $accessToken")
    }
}