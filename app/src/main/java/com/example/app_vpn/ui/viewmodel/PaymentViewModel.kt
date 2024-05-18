package com.example.app_vpn.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_vpn.data.entities.Payment
import com.example.app_vpn.data.network.Resource
import com.example.app_vpn.data.repository.PaymentRepository
import com.example.app_vpn.data.repsonses.DataResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaymentViewModel @Inject constructor (
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    private val _createPaymentResponse = MutableLiveData<Resource<DataResponse<Payment>>>()

    val createPaymentResponse : LiveData<Resource<DataResponse<Payment>>>
        get() = _createPaymentResponse

    fun createPayment(
        token : String,
        ipAddress : String,
        amount : String
    ) = viewModelScope.launch {
        _createPaymentResponse.value = Resource.Loading
        _createPaymentResponse.value = paymentRepository.createPayment(token, ipAddress, amount)
    }
}
