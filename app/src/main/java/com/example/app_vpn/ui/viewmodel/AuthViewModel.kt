package com.example.app_vpn.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.app_vpn.data.network.Resource
import com.example.app_vpn.data.repository.AuthRepository
import com.example.app_vpn.data.repsonses.LoginResponse
import com.example.app_vpn.data.repsonses.OtherResponse
import com.example.app_vpn.data.repsonses.RegisterResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : BaseViewModel(authRepository) {
    private val _loginResponse: MutableLiveData<Resource<LoginResponse>> = MutableLiveData()
    private val _registerResponse: MutableLiveData<Resource<RegisterResponse>> = MutableLiveData()
    private val _verifyResponse: MutableLiveData<Resource<OtherResponse>> = MutableLiveData()
    private val _isValidUsernameEmailResponse: MutableLiveData<Resource<OtherResponse>> = MutableLiveData()

    val loginResponse: LiveData<Resource<LoginResponse>>
        get() = _loginResponse
    val registerResponse : LiveData<Resource<RegisterResponse>>
        get() = _registerResponse
    val verifyResponse : LiveData<Resource<OtherResponse>>
        get() = _verifyResponse
    val isValidUsernameEmailResponse : LiveData<Resource<OtherResponse>>
        get() = _isValidUsernameEmailResponse

    fun login(
        username: String,
        password: String
    ) = viewModelScope.launch {
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
        email: String,
        code: String
    ) = viewModelScope.launch {
        _verifyResponse.value = Resource.Loading
        _verifyResponse.value = authRepository.verify(email, code)
    }

    fun isValidUsernameEmailResponse(
        username: String,
        email: String
    ) = viewModelScope.launch {
        _isValidUsernameEmailResponse.value = Resource.Loading
        _isValidUsernameEmailResponse.value = authRepository.isValidUsernameEmail(username, email)
    }

    suspend fun saveAccessTokens(accessToken: String, refreshToken: String) {
        authRepository.saveAccessTokens(accessToken, refreshToken)
    }

}