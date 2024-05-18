package com.example.app_vpn.util

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import java.net.URL
import java.util.Enumeration

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
            "error : $e"
        }
        return@async result
    }
}
