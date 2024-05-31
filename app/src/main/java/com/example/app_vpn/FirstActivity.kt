package com.example.app_vpn

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.app_vpn.data.preferences.UserPreference
import com.example.app_vpn.ui.MainActivity
import com.example.app_vpn.ui.auth.login.LoginActivity
import com.example.app_vpn.util.JwtUtils
import com.example.app_vpn.util.startNewActivity
import dagger.hilt.android.AndroidEntryPoint
import io.jsonwebtoken.ExpiredJwtException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FirstActivity : AppCompatActivity() {

    private val jwtUtils = JwtUtils()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        val userPreference = UserPreference(this)

        lifecycleScope.launch {
            val token = userPreference.accessToken.first()
            try {
                if (token == null || jwtUtils.isTokenExpired(token)) {
                    startNewActivity(LoginActivity::class.java)
                } else {
                    startNewActivity(MainActivity::class.java)
                }
            } catch (e: ExpiredJwtException) {
                startNewActivity(LoginActivity::class.java)
            }
        }
    }
}