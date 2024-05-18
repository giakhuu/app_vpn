package com.example.app_vpn.data.entities

data class Subscription(
    val number: Int,
    val duration: String,
    val price : String,
    var selected: Boolean = false
)
