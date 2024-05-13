package com.example.app_vpn.data.repsonses

data class LoginResponse (
    val isSuccessful: Boolean,
    val httpStatus: String,
    val message: String,
    val accessToken: String?,
    val refreshToken: String?
)