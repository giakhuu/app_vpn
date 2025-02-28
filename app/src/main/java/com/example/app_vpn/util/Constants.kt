package com.example.app_vpn.util

import com.example.app_vpn.data.entities.Benefit
import com.example.app_vpn.data.entities.Subscription

const val BASE_URL = "https://ykusrytqgiijklolfkeg.supabase.co/rest/v1/"
const val PAYPAL_URL = "https://api-m.sandbox.paypal.com/"
const val SUPABASE_URL = "https://ykusrytqgiijklolfkeg.supabase.co"
const val REDIRECT_URL = "com.example.appvpn://appvpn"
const val API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InlrdXNyeXRxZ2lpamtsb2xma2VnIiwicm9sZSI6ImFub24iLCJpYXQiOjE3Mzc3MTM4NjEsImV4cCI6MjA1MzI4OTg2MX0.od8jFdZQJ6SupSqYMHNFZ5iRE7AA_DRyY-aScpuWrt8"
const val SECRET_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InlrdXNyeXRxZ2lpamtsb2xma2VnIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTczNzcxMzg2MSwiZXhwIjoyMDUzMjg5ODYxfQ.x6fuVqaNo6_tBOjef2kK_1NaFTNMgLyqQTkGYeJvlVc"
const val VPN_SERVER_BUCKET = "vpnServer"
const val PAYPAL_CLIENT_ID = "AZtbe4tCYAMUYhb0ViqPFBlVza7xdUNuXRCMw2wrCSjHPuPFytRNIY9K7-j0EMwJmDT1_hJsK0gZbIIe"
const val PAYPAL_SECRECT_ID = "ED0u81dVyZXN1t6cvShDd4Kek6qUxsyKgcB2gR31jkra1ci-7IF2W9vAnE8oxXIu2BE3V0Ic05Tx-bdi"
val listSubscriptions = listOf(
    Subscription(1, 1, 5, "1 Month", false),
    Subscription(2, 6, 25, "6 Months", false),
    Subscription(3, 12, 45, "1 Year", false),
    Subscription(4, 24, 80, "2 Years", false)
)

val listBenefit = listOf(
    Benefit("Multi-Device", "Use on Multiple Devices."),
    Benefit("Faster", "Unlimited bandwidth."),
    Benefit("All Server", "All servers in 100+ countries.")
)

