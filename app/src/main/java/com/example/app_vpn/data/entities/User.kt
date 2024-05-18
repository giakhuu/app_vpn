package com.example.app_vpn.data.entities

data class User (
    val uid: Int,
    val username: String,
    val email: String,
    val password: String,
    val role: String,
    val premiumKey: String
)