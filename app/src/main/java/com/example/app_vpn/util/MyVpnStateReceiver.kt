package com.example.app_vpn.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyVpnStateReceiver(private val listener: VpnStateListener) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "de.blinkt.openvpn.VPN_STATUS") {
            val state = intent.getStringExtra("status")
            Log.d("VPN_STATUS", "VPN status changed: $state")
            if (state != null) {
                listener.onVpnStateChanged(state)
            }
            if (state == "LEVEL_CONNECTED") {
                listener.updateIpAddress()
                listener.showInterstitial()
            }
        }
    }
}
