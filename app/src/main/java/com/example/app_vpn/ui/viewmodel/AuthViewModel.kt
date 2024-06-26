package com.example.app_vpn.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_vpn.data.entities.Token
import com.example.app_vpn.data.entities.User
import com.example.app_vpn.data.network.Resource
import com.example.app_vpn.data.repository.AuthRepository
import com.example.app_vpn.data.repsonses.DataResponse
import com.example.app_vpn.data.repsonses.OtherResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _loginResponse: MutableLiveData<Resource<DataResponse<Token>>> = MutableLiveData()
    private val _registerResponse: MutableLiveData<Resource<DataResponse<User>>> = MutableLiveData()
    private val _verifyResponse: MutableLiveData<Resource<OtherResponse>> = MutableLiveData()
    private val _isUsernameEmailExist: MutableLiveData<Resource<OtherResponse>> = MutableLiveData()
    private val _isEmailExist: MutableLiveData<Resource<OtherResponse>> = MutableLiveData()

    val loginResponse: LiveData<Resource<DataResponse<Token>>>
        get() = _loginResponse
    val registerResponse: LiveData<Resource<DataResponse<User>>>
        get() = _registerResponse
    val verifyResponse: LiveData<Resource<OtherResponse>>
        get() = _verifyResponse
    val isUsernameEmailExist: LiveData<Resource<OtherResponse>>
        get() = _isUsernameEmailExist
    val isEmailExist: LiveData<Resource<OtherResponse>>
        get() = _isEmailExist

    fun login(
        username: String, password: String
    ) = viewModelScope.launch {
        _loginResponse.value = Resource.Loading
        _loginResponse.value = authRepository.login(username, password)
    }

    fun register(
        username: String,
        email: String,
        password: String,
    ) = viewModelScope.launch {
        _registerResponse.value = Resource.Loading
        _registerResponse.value = authRepository.register(username, email, password)
    }

    fun verify(
        email: String, code: String
    ) = viewModelScope.launch {
        _verifyResponse.value = Resource.Loading
        _verifyResponse.value = authRepository.verify(email, code)
    }

    fun isUsernameEmailExist(
        username: String, email: String
    ) = viewModelScope.launch {
        _isUsernameEmailExist.value = Resource.Loading
        _isUsernameEmailExist.value = authRepository.isUsernameEmailExist(username, email)
    }

    fun isEmailExist(
        email: String
    ) = viewModelScope.launch {
        _isEmailExist.value = Resource.Loading
        _isEmailExist.value = authRepository.isEmailExist(email)
    }

    suspend fun saveAccessTokens(accessToken: String, refreshToken: String) {
        authRepository.saveAccessTokens(accessToken, refreshToken)
    }

    suspend fun savePremiumKey(premiumKey: String) {
        authRepository.savePremiumKey(premiumKey)
    }
}