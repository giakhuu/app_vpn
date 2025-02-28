package com.example.app_vpn.data.entities

data class VpnServer(
    val id : Int,
    val name : String,
    val flag : String,
    val config : String,
    val premium: Boolean,
    val vpnName : String,
    val vpnPassword : String,
)
