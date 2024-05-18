package com.example.app_vpn.data.entities

data class Token (
    val accessToken: String?,
    val refreshToken: String?,
    val premiumKey: String?
)