package com.example.app_vpn.data.entities

import com.google.gson.annotations.SerializedName

data class PaypalAccessToken (
    @SerializedName("access_token") val accessToken: String
)