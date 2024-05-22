package com.example.app_vpn.ui.auth.resetpw

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.app_vpn.R
import com.example.app_vpn.databinding.ActivityResetPasswordSuccessBinding
import com.example.app_vpn.ui.auth.login.LoginActivity
import com.example.app_vpn.util.startNewActivity

class ResetPasswordSuccessActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResetPasswordSuccessBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPasswordSuccessBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnResetSuccessfull.setOnClickListener {
            startNewActivity(LoginActivity::class.java)
        }
    }
}