package com.example.app_vpn.ui.splashscreen

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.example.app_vpn.ui.MainActivity

class SplashTheme : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Handler().postDelayed({
            var intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        },3000)
    }
}