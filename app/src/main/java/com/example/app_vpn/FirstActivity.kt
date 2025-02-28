package com.example.app_vpn

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.app_vpn.ui.MainActivity
import com.example.app_vpn.ui.auth.login.LoginActivity
import com.example.app_vpn.util.startNewActivity
import dagger.hilt.android.AndroidEntryPoint
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.jsonwebtoken.ExpiredJwtException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@AndroidEntryPoint
class FirstActivity : AppCompatActivity() {
    @Inject
    lateinit var supabase: SupabaseClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        lifecycleScope.launch {
            supabase.auth.loadFromStorage() // 🔥 Load session đã lưu trước khi kiểm tra
            checkUserSession()
        }
    }

    private suspend fun checkUserSession() {
        val session = supabase.auth.currentSessionOrNull()

        if (session == null) {
            startNewActivity(LoginActivity::class.java)
            return
        }

        val expiresAtInstant = Instant.ofEpochSecond(session.expiresAt.epochSeconds)
        val nowInstant = Instant.now()

        if (expiresAtInstant.isBefore(nowInstant)) {

            try {
                supabase.auth.refreshSession(session.refreshToken)
            } catch (e: Exception) {
                startNewActivity(LoginActivity::class.java)
                return
            }
        }

        startNewActivity(MainActivity::class.java)
    }

    private fun startNewActivity(activity: Class<*>) {
        startActivity(Intent(this, activity))
        finish()
    }
}
