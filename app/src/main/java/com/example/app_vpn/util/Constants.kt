package com.example.app_vpn.util

import com.example.app_vpn.data.entities.Benefit
import com.example.app_vpn.data.entities.Subscription

//const val BASE_URL = "http://192.168.193.138:8080/"
const val BASE_URL = "http://192.168.1.12:8080/"

val listSubscriptions = listOf(
    Subscription(1, "Month", "23000"),
    Subscription(6, "Month", "109000"),
    Subscription(1, "Year", "209000"),
    Subscription(2, "Year", "359000")
)

val listBenefit = listOf(
    Benefit("Multi-Device", "Use on Multiple Devices."),
    Benefit("Faster", "Unlimited bandwidth."),
    Benefit("All Server", "All servers in 100+ countries.")
)

