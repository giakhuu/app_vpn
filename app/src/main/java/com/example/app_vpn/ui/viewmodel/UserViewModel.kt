package com.example.app_vpn.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_vpn.data.entities.User
import com.example.app_vpn.data.network.Resource
import com.example.app_vpn.data.repository.UserRepository
import com.example.app_vpn.data.repsonses.DataResponse
import com.example.app_vpn.data.repsonses.OtherResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _user = MutableLiveData<Resource<DataResponse<User>>>()
    private val _changePwResponse = MutableLiveData<Resource<DataResponse<User>>>()
    private val _resetPasswordResponse = MutableLiveData<Resource<OtherResponse>>()

    val user: LiveData<Resource<DataResponse<User>>>
        get() = _user
    val changePwResponse: LiveData<Resource<DataResponse<User>>>
        get() = _changePwResponse
    val resetPasswordResponse: LiveData<Resource<OtherResponse>>
        get() = _resetPasswordResponse

    fun fetchData(accessToken: String) = viewModelScope.launch {
        _user.value = userRepository.fetchData(accessToken)
    }

    fun changePassword(
        accessToken: String,
        oldPassword: String,
        newPassword: String
    ) = viewModelScope.launch {
        _changePwResponse.value = Resource.Loading
        _changePwResponse.value =
            userRepository.changePassword(accessToken, oldPassword, newPassword)
    }

    fun resetPassword(
        email: String,
        password: String
    ) = viewModelScope.launch {
        _resetPasswordResponse.value = Resource.Loading
        _resetPasswordResponse.value = userRepository.resetPassword(email, password)
    }

    fun delete(accessToken: String) = viewModelScope.launch {
        userRepository.delete(accessToken)
    }
}