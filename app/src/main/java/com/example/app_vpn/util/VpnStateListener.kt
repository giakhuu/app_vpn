package com.example.app_vpn.util

interface VpnStateListener {
    fun onVpnStateChanged(state: String)
    fun updateIpAddress()
    fun showInterstitial()
}