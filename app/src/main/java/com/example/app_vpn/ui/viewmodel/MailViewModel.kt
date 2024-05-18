package com.example.app_vpn.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_vpn.data.network.Resource
import com.example.app_vpn.data.repository.MailRepository
import com.example.app_vpn.data.repsonses.OtherResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MailViewModel @Inject constructor(
    private val mailRepository: MailRepository
) : ViewModel() {

    private val _sendVerifyResponse = MutableLiveData<Resource<OtherResponse>>()
    private val _resendCodeResponse = MutableLiveData<Resource<OtherResponse>>()

    val sendVerifyResponse : MutableLiveData<Resource<OtherResponse>>
        get() = _sendVerifyResponse

    val resendCodeResponse : MutableLiveData<Resource<OtherResponse>>
        get() = _resendCodeResponse

    fun sendVerifyCode(email : String) = viewModelScope.launch {
        _sendVerifyResponse.value = Resource.Loading
        _sendVerifyResponse.value = mailRepository.sendVerifyCode(email)
    }

    fun resendCode(email: String) = viewModelScope.launch {
        _resendCodeResponse.value = Resource.Loading
        _resendCodeResponse.value = mailRepository.resendCode(email)
    }
}