package com.example.app_vpn.data.repsonses

data class DataResponse<T>(
    val isSuccessful: Boolean,
    val data: T,
    val httpStatus: String,
    val message: String
)
