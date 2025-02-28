package com.example.app_vpn.ui.auth.verify

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.app_vpn.databinding.ActivityVerificationBinding
import com.example.app_vpn.ui.BaseActivity
import com.example.app_vpn.ui.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class VerificationActivity : BaseActivity() {

    private lateinit var binding: ActivityVerificationBinding
    private lateinit var btnVerify: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()

    }

    private fun setupUI() = with(binding) {
        enableEdgeToEdge()
        setupVerifyButton()
    }


    private fun VerificationActivity.setupVerifyButton() {
        btnVerify = binding.btnVerify
        btnVerify.setOnClickListener() {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                `package` = "com.google.android.gm"
            }
            this.startActivity(intent)
        }
    }

}