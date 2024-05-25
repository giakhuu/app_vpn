package com.example.app_vpn.data.entities

data class Country(
    val id : Int,
    val name : String,
    val flag : String,
    val config : String,
    val prenium :Boolean,
    val vpnName : String,
    val vpnPassword : String,
)
