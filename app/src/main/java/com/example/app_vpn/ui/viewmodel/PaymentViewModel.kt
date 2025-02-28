package com.example.app_vpn.ui.viewmodel

import android.content.Context
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.example.app_vpn.data.entities.Payment
import com.example.app_vpn.data.entities.PaypalAccessToken
import com.example.app_vpn.data.entities.Subscription
import com.example.app_vpn.data.network.Resource
import com.example.app_vpn.data.repository.repoImpl.PaymentRepository
import com.example.app_vpn.data.repository.repoImpl.PaypalPaymentRepository
import com.example.app_vpn.data.repository.repoImpl.SubscriptionRepository
import com.example.app_vpn.data.repository.repoImpl.UserRepository
import com.example.app_vpn.util.PAYPAL_CLIENT_ID
import com.example.app_vpn.util.PAYPAL_SECRECT_ID
import com.example.app_vpn.util.REDIRECT_URL
import com.example.app_vpn.util.toast
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PaymentViewModel @Inject constructor (
    private val paypalPaymentRepository: PaypalPaymentRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val paymentRepository: PaymentRepository,
    private val userRepository: UserRepository,
    @ApplicationContext private val context: Context,
    private val supabaseClient: SupabaseClient
) : ViewModel() {

    // Giả sử accessToken đã có dưới dạng StateFlow<Resource<PaypalAccessToken>>
    private val _accessToken = MutableStateFlow<PaypalAccessToken?>(null)
    val accessToken: StateFlow<PaypalAccessToken?> = _accessToken.asStateFlow()

    // Lưu lựa chọn của người dùng (ví dụ: lựa chọn từ list)
    val selectedChoice = MutableStateFlow<Int?>(null)
    val orderID = MutableStateFlow<String?>(null)

    private val _orderResponse = MutableStateFlow<ResponseBody?>(null)
    val orderResponse: StateFlow<ResponseBody?> = _orderResponse.asStateFlow()

    private val _captureResponse = MutableStateFlow<ResponseBody?>(null)
    val captureResponse: StateFlow<ResponseBody?> = _captureResponse

    private val _subsciption = MutableStateFlow<List<Subscription>?> (emptyList())
    val subscription : StateFlow<List<Subscription>?> = _subsciption.asStateFlow()

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage = _toastMessage.asSharedFlow()


    fun getAccessToken(
    ) = viewModelScope.launch {
        val authString = "$PAYPAL_CLIENT_ID:$PAYPAL_SECRECT_ID"
        val encodedAuthString = Base64.encodeToString(authString.toByteArray(), Base64.NO_WRAP)
        when(val response = paypalPaymentRepository.getAccessToken(encodedAuthString)) {
            is Resource.Error -> {
                _toastMessage.emit("Get Access Token Failed")
            }
            Resource.Loading -> {}
            is Resource.Success -> {
                _accessToken.value = response.data
                _toastMessage.emit("Get Access Token Success")
            }
        }
    }


    private fun createOrder(accessToken: String, uniqueId: String, orderRequestJson: JSONObject) {
        viewModelScope.launch {
            val response = paypalPaymentRepository.createOrder(accessToken, uniqueId, orderRequestJson)
            when(response) {
                is Resource.Error -> {
                    _toastMessage.emit("Create Order Failed")
                    Log.d("Payment", "createOrder: ${response.error.message}")
                }
                Resource.Loading -> {}
                is Resource.Success -> {
                    _toastMessage.emit("Create Order Success")
                    _orderResponse.value = response.data
                }
            }
        }
    }


    fun captureOrder(orderId: String, accessToken: String) {
        viewModelScope.launch {
            when(val response = paypalPaymentRepository.captureOrder(orderId, accessToken)) {
                is Resource.Error -> {
                    _toastMessage.emit("Capture Order Failed")
                }
                Resource.Loading -> TODO()
                is Resource.Success -> {
                    insertPayment(orderId)
                    updatePremiumData()
                    _toastMessage.emit("Capture Order Success")
                    _captureResponse.value = response.data

                }
            }
        }
    }

    fun getAllSubsciption() = viewModelScope.launch {
        when(val response = subscriptionRepository.getAllSubsciption()) {
            is Resource.Error -> {
                _toastMessage.emit("Create Order failed")
            }
            Resource.Loading -> TODO()
            is Resource.Success -> {
                _toastMessage.emit("Create Order Success")
                _subsciption.value = response.data
            }
        }
    }

    fun startOrder() {
        val uniqueId = UUID.randomUUID().toString()
        val value = selectedChoice.value?.let { subscription.value?.get(it)?.price ?: 0 }

        val orderRequestJson = JSONObject().apply {
            put("intent", "CAPTURE")
            put("purchase_units", JSONArray().apply {
                put(JSONObject().apply {
                    put("reference_id", uniqueId)
                    put("amount", JSONObject().apply {
                        put("currency_code", "USD")
                        put("value", "$value.00")
                    })
                })
            })
            put("payment_source", JSONObject().apply {
                put("paypal", JSONObject().apply {
                    put("experience_context", JSONObject().apply {
                        put("payment_method_preference", "IMMEDIATE_PAYMENT_REQUIRED")
                        put("brand_name", "SH Developer")
                        put("locale", "en-US")
                        put("landing_page", "LOGIN")
                        put("shipping_preference", "NO_SHIPPING")
                        put("user_action", "PAY_NOW")
                        put("return_url", "$REDIRECT_URL/payment/return")
                        put("cancel_url", "https://example.com/cancelUrl")
                    })
                })
            })
        }
        _accessToken.value?.let {
            createOrder(
                it.accessToken,
                uniqueId,
                orderRequestJson
            )
        }
    }

    private fun insertPayment(orderId: String) = viewModelScope.launch {
        val payment = Payment(
            id = orderId,
            userId = supabaseClient.auth.currentUserOrNull()!!.id,
            confirm = false,
            subscriptionId = subscription.value!![selectedChoice.value!!].id
        )
        when(val response = paymentRepository.insertPayment(payment)) {
            is Resource.Error -> {
                Log.d("insertPayment", "insertPayment: ${response.error}")
            }
            Resource.Loading -> {}
            is Resource.Success -> {}
        }
    }


    private fun updatePremiumData() = viewModelScope.launch {
        userRepository.updatePremiumData(subscription = subscription.value!![selectedChoice.value!!])
    }


}
