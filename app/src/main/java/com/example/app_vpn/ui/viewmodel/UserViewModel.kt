package com.example.app_vpn.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.app_vpn.data.network.Resource
import com.example.app_vpn.data.repository.UserRepository
import com.example.app_vpn.data.repsonses.DataResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository
) : BaseViewModel(userRepository) {

    private val _user : MutableLiveData<Resource<DataResponse>> = MutableLiveData()

    val user : LiveData<Resource<DataResponse>>
        get() = _user

    fun fetchData(accessToken: String) = viewModelScope.launch {
        _user.value = userRepository.fetchData(accessToken)
    }

    fun changePassword(
        accessToken: String,
        oldPassword: String,
        newPassword: String
    ) = viewModelScope.launch {
        userRepository.changePassword(accessToken, oldPassword, newPassword)
    }

    fun delete(accessToken: String) = viewModelScope.launch {
        userRepository.delete(accessToken)
    }
}