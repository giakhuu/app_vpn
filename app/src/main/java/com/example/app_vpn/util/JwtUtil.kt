package com.example.app_vpn.util

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Base64
import java.util.Date
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

class JwtUtils {

    private val secretKey: SecretKey
    init {
        val secretString = "843567893696976453275974432697R634976R738467TR678T34865R6834R8763T478378637664538745673865783678548735687R3"
        val keyBytes = Base64.getDecoder().decode(secretString.toByteArray(StandardCharsets.UTF_8))
        this.secretKey = SecretKeySpec(keyBytes, "HmacSHA256")
    }

    private fun <T> extractClaims(token: String, claimsFunction: (Claims) -> T): T {
        val claims = Jwts.parser()
            .setSigningKey(secretKey)
            .parseClaimsJws(token)
            .body
        return claimsFunction(claims)
    }

    fun extractPremiumType(token: String): String {
        return extractClaims(token) { claims -> claims["premium_type", String::class.java] }
    }

    fun extractExpirationDate(token: String): String? {
        val expirationDate = extractClaims(token, Claims::getExpiration)
        val instant = expirationDate.toInstant()
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).format(formatter)
    }
}
