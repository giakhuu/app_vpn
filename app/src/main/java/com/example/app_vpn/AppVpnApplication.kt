package com.example.app_vpn

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import de.blinkt.openvpn.core.ICSOpenVPNApplication

@HiltAndroidApp
class AppVpnApplication : ICSOpenVPNApplication() {

}