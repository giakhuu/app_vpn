package com.example.app_vpn.util

import android.util.Log
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.net.URL

suspend fun getMyPublicIpAsync(): Deferred<String> = coroutineScope {
    async(Dispatchers.IO) {
        var result = ""
        result = try {
            val url = URL("https://api.ipify.org")
            val httpsURLConnection = url.openConnection()
            val iStream = httpsURLConnection.getInputStream()
            val buff = ByteArray(1024)
            val read = iStream.read(buff)
            String(buff, 0, read)
        } catch (e: Exception) {
            Log.d("mytag", "getIpAsyncError: " + e.message)
            "Loading..."
        }
        return@async result
    }
}
