package com.example.app_vpn.ui.auth.resetpw

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.app_vpn.R
import com.example.app_vpn.data.network.Resource
import com.example.app_vpn.databinding.ActivityForgotPasswordBinding
import com.example.app_vpn.ui.viewmodel.MailViewModel
import com.example.app_vpn.util.handleApiError
import com.example.app_vpn.util.isValid
import com.example.app_vpn.util.isValidEmail
import com.example.app_vpn.util.onLoad
import com.example.app_vpn.util.setUp

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding

    private val mailViewModel by viewModels<MailViewModel>()

    private lateinit var btnSubmit : Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnSubmit = binding.btnSubmit
        btnSubmit.setUp()

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.iplyForgotEmail.isValid(
            binding.txtForgotEmail,
            binding.btnSubmit,
            invalidHelperText = "Invalid Email",
            validate = {this.isValidEmail()}
        )

        btnSubmit.setOnClickListener {
            sendVerify()
        }

        mailViewModel.sendVerifyResponse.observe(this) {
            when (it) {
                is Resource.Success -> {

                }
                is Resource.Failure -> {
                    handleApiError(it)
                }
                is Resource.Loading -> {
                    btnSubmit.onLoad()
                }
            }
        }
    }

    private fun sendVerify() {
        val email = binding.txtForgotEmail.text?.trim().toString()
        mailViewModel.sendVerifyCode(email)
    }
}