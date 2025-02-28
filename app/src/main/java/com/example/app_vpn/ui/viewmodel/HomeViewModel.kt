package com.example.app_vpn.ui.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_vpn.data.entities.VpnServer
import com.example.app_vpn.data.network.Resource
import com.example.app_vpn.data.preferences.PreferenceManager
import com.example.app_vpn.data.repository.repoImpl.VpnServerRepository
import com.example.app_vpn.util.VPN_SERVER_BUCKET
import dagger.hilt.android.internal.Contexts.getApplication
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.io.File
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val supabase: SupabaseClient,
    private val vpnServerRepository: VpnServerRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel(){
    var config: String = ""


    init {
        preferenceManager.getVpnServer()?.let { vpnServer ->
            getConfig(vpnServer)
        }
    }

    fun getConfig(vpnServer: VpnServer) {
        viewModelScope.launch {
            val cachedConfig = vpnServerRepository.readConfigFromStorage(vpnServer.name)
            if (!cachedConfig.isNullOrEmpty()) {
                config = cachedConfig
            }
            else {
                try {
                    val result = vpnServerRepository.downloadConfigFile(vpnServer.config)
                    if (result is Resource.Success) {
                        vpnServerRepository.saveConfigToStorage(vpnServer.name, result.data ?: "")
                        config = result.data ?: ""
                        Log.d("getConfig", "getConfig: $result")

                    }
                    if (result is Resource.Error) {
                        Log.d("getConfig", "getConfigError: ${result.error}")
                    }

                }
                catch (e: Exception) {
                    Log.d("getConfig", "getConfigError: ${e}")
                }
            }
        }
    }
}
