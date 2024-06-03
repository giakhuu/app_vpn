package com.example.app_vpn.ui.auth.verify

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import com.example.app_vpn.databinding.ActivitySuccessBinding
import com.example.app_vpn.ui.BaseActivity
import com.example.app_vpn.ui.auth.login.LoginActivity
import com.example.app_vpn.util.startNewActivity

class SuccessActivity : BaseActivity() {
    private lateinit var binding: ActivitySuccessBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySuccessBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.btnOkay.setOnClickListener {
            startNewActivity(LoginActivity::class.java)
        }
    }
}