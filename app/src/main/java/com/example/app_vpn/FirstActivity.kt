package com.example.app_vpn

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.app_vpn.ui.MainActivity
import com.example.app_vpn.ui.auth.login.LoginActivity
import dagger.hilt.android.AndroidEntryPoint
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
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
        checkAndRequestNotificationPermission()
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

    private val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001

    @SuppressLint("InlinedApi")
    private fun checkAndRequestNotificationPermission() {
        Log.d("checkNotification", "checkNotification ${Build.VERSION.SDK_INT}")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }
}
