package com.example.app_vpn.data.repsonses

data class DataResponse(
    val isSuccessful: Boolean,
    val data: User,
    val httpStatus: String,
    val message: String
)
