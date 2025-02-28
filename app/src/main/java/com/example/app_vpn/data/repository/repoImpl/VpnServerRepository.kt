package com.example.app_vpn.data.repository.repoImpl

import android.content.Context
import android.util.Log
import com.example.app_vpn.data.network.api.VpnServerApi
import com.example.app_vpn.data.network.safeCall
import com.example.app_vpn.util.VPN_SERVER_BUCKET
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import retrofit2.http.Url
import java.io.File
import javax.inject.Inject

class VpnServerRepository @Inject constructor(
    private val vpnServerApi : VpnServerApi,
    @ApplicationContext private val context: Context,
    private val supabase: SupabaseClient
)  {
    suspend fun getAllVpnServer() = safeCall {
        vpnServerApi.getAllVpnServer()
    }

    suspend fun downloadConfigFile(config: String) = safeCall {
        val bucketName = VPN_SERVER_BUCKET
        val fileUrl = supabase.storage.from(bucketName).publicUrl(config)
        vpnServerApi.downloadConfigFile(fileUrl)
    }

    fun saveConfigToStorage(name: String, configContent: String) {
        try {
            val dir = File(context.filesDir, "vpn")
            if (!dir.exists()) dir.mkdirs()

            val file = File(dir, "$name.ovpn")
            file.writeText(configContent)

            Log.d("saveConfigToStorage", "File saved successfully: ${file.absolutePath}")
        } catch (e: Exception) {
            Log.e("saveConfigToStorage", "Error saving file: ${e.message}")
        }
    }

    fun readConfigFromStorage(name: String): String? {
        return try {
            val file = File(context.filesDir, "vpn/$name.ovpn")
            if (file.exists()) file.readText() else null
        } catch (e: Exception) {
            Log.e("readConfigFromStorage", "Error reading file: ${e.message}")
            null
        }
    }
}