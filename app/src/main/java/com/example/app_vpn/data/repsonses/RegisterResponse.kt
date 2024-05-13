package com.example.app_vpn.data.repsonses

data class RegisterResponse (
    val isSuccessful: Boolean,
    val httpStatus: String,
    val message: String,
    val user: User?
)