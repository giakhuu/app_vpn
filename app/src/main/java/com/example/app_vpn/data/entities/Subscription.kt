package com.example.app_vpn.data.entities

data class Subscription(
    val id: Int,
    val duration: Int,
    val price : Int,
    val description: String,
    var isSelected: Boolean
)
