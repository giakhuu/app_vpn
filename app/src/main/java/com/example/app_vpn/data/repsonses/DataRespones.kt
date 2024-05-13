package com.example.app_vpn.data.repsonses

data class DataRespones<T>(
    val isSuccess : Boolean,
    val data : T,
    val httpStatus : String,
    val message : String,
)
