package com.example.app_vpn.ui.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_vpn.data.entities.VpnServer
import com.example.app_vpn.data.network.Resource
import com.example.app_vpn.data.repository.repoImpl.VpnServerRepository
import com.example.app_vpn.ui.auth.login.TAG
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class VpnServerViewModel @Inject constructor(
    private val vpnServerRepository: VpnServerRepository
) : ViewModel() {
    private val _allCountry = MutableStateFlow<Resource<List<VpnServer>>>(Resource.Loading)

    val allCountry : StateFlow<Resource<List<VpnServer>>> = _allCountry.asStateFlow()

    init {
        getAllCountry()
    }

    private fun getAllCountry() = viewModelScope.launch {
        _allCountry.value = Resource.Loading
        _allCountry.value = vpnServerRepository.getAllVpnServer()
        Log.d(TAG, "getAllCountry: ${_allCountry.value}")
    }
}