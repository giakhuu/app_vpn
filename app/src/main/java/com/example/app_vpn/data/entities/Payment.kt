package com.example.app_vpn.data.entities

data class Payment(
    val id: String,
    val userId: String,
    val confirm: Boolean,
    val subscriptionId: Int
)
