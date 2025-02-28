package com.example.app_vpn.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_vpn.data.entities.PremiumStatus
import com.example.app_vpn.data.network.Resource
import com.example.app_vpn.data.preferences.UserPreference
import com.example.app_vpn.data.repository.repoImpl.UserRepository
import com.example.app_vpn.ui.auth.login.TAG
import com.example.app_vpn.util.VPN_SERVER_BUCKET
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val userPreference: UserPreference,
    private val supabase: SupabaseClient
) : ViewModel() {

    private val _premiumStatus = MutableStateFlow<Resource<List<PremiumStatus>>>(Resource.Loading)
    val premiumStatus: StateFlow<Resource<List<PremiumStatus>>>
        get() = _premiumStatus.asStateFlow()
    private val _updatePasswordResponse = MutableStateFlow<Resource<Nothing>>(Resource.Loading)
    val updatePasswordResponse: StateFlow<Resource<Nothing>> = _updatePasswordResponse.asStateFlow()
    /**
     * Gọi API để lấy premiumStatus và lưu vào UserPreference
     * Dùng ở **HomeFragment**
     */
    fun fetchPremiumData() = viewModelScope.launch {
        val result = userRepository.fetchPremiumData()
        _premiumStatus.value = result

        // Kiểm tra nếu API trả về thành công và có dữ liệu
        if (result is Resource.Success) {
            result.data?.firstOrNull()?.let { premiumStatus ->
                savePremiumStatus(premiumStatus)
            }
        }
    }

    /**
     * Lưu `premiumStatus` vào UserPreference
     */
    fun savePremiumStatus(premiumStatus: PremiumStatus) {
        viewModelScope.launch {
            userPreference.savePremiumStatus(premiumStatus)
        }
    }


}